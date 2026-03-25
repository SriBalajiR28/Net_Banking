package com.cmrit.demo.controller;

import com.cmrit.demo.exception.InvalidOTPException;
import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.Session;
import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.UserRepository;
import com.cmrit.demo.security.JwtUtil;
import com.cmrit.demo.service.AuditService;
import com.cmrit.demo.service.AuthService;
import com.cmrit.demo.service.OTPService;
import com.cmrit.demo.service.SecurityAlertService;
import com.cmrit.demo.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OTPService otpService;
    private final AuditService auditService;
    private final SessionService sessionService;
    private final AuthService authService;
    private final SecurityAlertService alertService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            OTPService otpService,
            AuditService auditService,
            SessionService sessionService,
            AuthService authService,
            SecurityAlertService alertService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.otpService = otpService;
        this.auditService = auditService;
        this.sessionService = sessionService;
        this.authService = authService;
        this.alertService = alertService;
    }

    // ✅ Register new user
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestBody User user) {
        if (userRepository.findByUsername(
                user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Username already exists: "
                            + user.getUsername());
        }
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null
                || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }
        userRepository.save(user);
        return ResponseEntity.ok(
                "User registered successfully!");
    }

    // ✅ Step 1 — Login with password → send OTP
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody User user,
            HttpServletRequest request) {
        String ip = getClientIp(request);

        // Check if already exceeded attempts
        if (authService.hasExceededLoginAttempts(
                user.getUsername())) {
            return ResponseEntity.status(429)
                    .body("Too many failed attempts. "
                            + "Please contact admin.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()));

            // Reset failed attempts on success
            authService.resetFailedLoginAttempts(
                    user.getUsername());

            User dbUser = userRepository
                    .findByUsername(user.getUsername())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found: "
                                            + user.getUsername()));

            // Generate and send OTP
            String otp = otpService.generateOtp(
                    dbUser.getUsername());
            otpService.sendOtpByEmail(dbUser.getEmail(), otp);
            auditService.recordLoginAttempt(
                    dbUser.getUserId(), "OTP_SENT", ip);

            return ResponseEntity.ok(
                    "OTP sent to registered email.");

        } catch (Exception e) {
            authService.recordFailedLogin(user.getUsername());
            int attempts = authService
                    .getFailedLoginAttempts(user.getUsername());

            // Raise alert after 3 failed attempts
            if (authService.hasExceededLoginAttempts(
                    user.getUsername())) {
                userRepository.findByUsername(user.getUsername())
                        .ifPresent(dbUser -> alertService
                                .raiseAlert(
                                        dbUser.getUserId(),
                                        "MULTIPLE_FAILED_LOGINS",
                                        "HIGH",
                                        "User exceeded " + attempts
                                        + " failed login attempts",
                                        ip));
            }

            return ResponseEntity.status(401)
                    .body("Invalid username or password. Attempt "
                            + attempts + " of 3");
        }
    }

    // ✅ Step 2 — Validate OTP → issue JWT + create session
    @PostMapping("/validate-otp")
    public ResponseEntity<Map<String, String>> validateOtp(
            @RequestParam String username,
            @RequestParam String otp,
            HttpServletRequest request) {

        String ip = getClientIp(request);

        try {
            if (!otpService.validateOTP(username, otp)) {
                auditService.recordOtpAttempt(0L, "FAILURE", ip);
                throw new InvalidOTPException(
                        "Invalid OTP for user: " + username);
            }
        } catch (InvalidOTPException e) {
            // Raise alert after 3 failed OTP attempts
            if (otpService.hasExceededOtpAttempts(username)) {
                userRepository.findByUsername(username)
                        .ifPresent(dbUser -> alertService
                                .raiseAlert(
                                        dbUser.getUserId(),
                                        "OTP_ABUSE",
                                        "HIGH",
                                        "User exceeded 3 failed"
                                        + " OTP attempts",
                                        ip));
            }
            throw e;
        }

        User dbUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        String role = dbUser.getRole();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(
                dbUser.getUsername(), List.of(role));

        // Create session in DB
        String deviceInfo = request.getHeader("User-Agent");
        Session session = sessionService.startSession(
                dbUser.getUserId(), token, ip, deviceInfo);

        // Clear OTP after successful use
        otpService.clearOTP(username);
        auditService.recordOtpAttempt(
                dbUser.getUserId(), "SUCCESS", ip);

        // Reset failed login attempts
        authService.resetFailedLoginAttempts(username);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", dbUser.getUsername());
        response.put("role", role);
        response.put("sessionId",
                String.valueOf(session.getId()));
        response.put("userId",
                String.valueOf(dbUser.getUserId()));

        return ResponseEntity.ok(response);
    }

    // ✅ Register first admin
    @PostMapping("/register-admin")
    public ResponseEntity<String> registerFirstAdmin(
            @RequestBody User user) {
        if (userRepository.existsByRole("ROLE_ADMIN")) {
            return ResponseEntity.status(403)
                    .body("Admin already exists. "
                            + "Use /admin/users to create more.");
        }
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);
        return ResponseEntity.ok(
                "First admin registered successfully!");
    }

    // ✅ Logout — end session + clear context
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader) {
        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessionService.endSessionByToken(token);
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully!");
    }

    // ✅ Profile
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> profile() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        Map<String, String> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("role",
                auth.getAuthorities().toString());
        return ResponseEntity.ok(response);
    }

    // ✅ Helper — extract real client IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}