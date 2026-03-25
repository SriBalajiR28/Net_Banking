package com.cmrit.demo.controller;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.AdminAction;
import com.cmrit.demo.model.LoginAttempt;
import com.cmrit.demo.model.Session;
import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.SessionRepository;
import com.cmrit.demo.repository.UserRepository;
import com.cmrit.demo.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(
            UserRepository userRepository,
            SessionRepository sessionRepository,
            AuditService auditService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.auditService = auditService;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Create new user
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @RequestBody User user,
            HttpServletRequest request) {
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null
                || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }
        if (!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole());
        }
        User savedUser = userRepository.save(user);
        auditService.recordAdminAction(
                getCurrentAdminId(), "CREATE_USER",
                savedUser.getUserId(), getClientIp(request));
        return ResponseEntity.ok(savedUser);
    }

    // ✅ Get all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(
            HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(), "VIEW_USERS",
                null, getClientIp(request));
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ✅ Get user by ID
    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        auditService.recordAdminAction(
                getCurrentAdminId(), "VIEW_USER",
                id, getClientIp(request));
        return ResponseEntity.ok(user);
    }

    // ✅ Update user role
    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        user.setRole(role);
        userRepository.save(user);
        auditService.recordAdminAction(
                getCurrentAdminId(), "UPDATE_ROLE",
                id, getClientIp(request));
        return ResponseEntity.ok("Role updated to "
                + role + " for user: " + user.getUsername());
    }

    // ✅ Delete user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Long id,
            HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        userRepository.delete(user);
        auditService.recordAdminAction(
                getCurrentAdminId(), "DELETE_USER",
                id, getClientIp(request));
        return ResponseEntity.ok("User deleted successfully!");
    }

    // ✅ Get all sessions
    @GetMapping("/sessions")
    public ResponseEntity<List<Session>> getAllSessions(
            HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(), "VIEW_SESSIONS",
                null, getClientIp(request));
        return ResponseEntity.ok(sessionRepository.findAll());
    }

    // ✅ End session by ID
    @PutMapping("/sessions/{id}/end")
    public ResponseEntity<String> endSession(
            @PathVariable Long id,
            HttpServletRequest request) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with ID: " + id));
        session.setEndTime(java.time.LocalDateTime.now());
        sessionRepository.save(session);
        auditService.recordAdminAction(
                getCurrentAdminId(), "END_SESSION",
                id, getClientIp(request));
        return ResponseEntity.ok("Session ended successfully!");
    }

    // ✅ View audit logs with optional filters
    @GetMapping("/audit")
    public ResponseEntity<List<AdminAction>> getAuditLogs(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String actionType,
            HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(), "VIEW_AUDIT_LOGS",
                null, getClientIp(request));
        List<AdminAction> logs;
        if (adminId != null && actionType != null) {
            logs = auditService.findByAdminIdAndActionType(
                    adminId, actionType);
        } else if (adminId != null) {
            logs = auditService.findByAdminId(adminId);
        } else if (actionType != null) {
            logs = auditService.findByActionType(actionType);
        } else {
            logs = auditService.getAllActions();
        }
        return ResponseEntity.ok(logs);
    }

    // ✅ Get all login attempts
    @GetMapping("/login-attempts")
    public ResponseEntity<List<LoginAttempt>>
            getAllLoginAttempts(HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(), "VIEW_LOGIN_ATTEMPTS",
                null, getClientIp(request));
        return ResponseEntity.ok(
                auditService.getAllLoginAttempts());
    }

    // ✅ Get login attempts by user ID
    @GetMapping("/login-attempts/user/{userId}")
    public ResponseEntity<List<LoginAttempt>>
            getLoginAttemptsByUser(
                    @PathVariable Long userId,
                    HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(),
                "VIEW_LOGIN_ATTEMPTS_USER",
                userId, getClientIp(request));
        return ResponseEntity.ok(
                auditService.getLoginAttemptsByUser(userId));
    }

    // ✅ Get login attempts by status
    @GetMapping("/login-attempts/status/{status}")
    public ResponseEntity<List<LoginAttempt>>
            getLoginAttemptsByStatus(
                    @PathVariable String status,
                    HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(),
                "VIEW_LOGIN_ATTEMPTS_STATUS",
                null, getClientIp(request));
        return ResponseEntity.ok(
                auditService.getLoginAttemptsByStatus(status));
    }

    // ✅ Get login attempts by IP
    @GetMapping("/login-attempts/ip/{ipAddress}")
    public ResponseEntity<List<LoginAttempt>>
            getLoginAttemptsByIp(
                    @PathVariable String ipAddress,
                    HttpServletRequest request) {
        auditService.recordAdminAction(
                getCurrentAdminId(),
                "VIEW_LOGIN_ATTEMPTS_IP",
                null, getClientIp(request));
        return ResponseEntity.ok(
                auditService.getLoginAttemptsByIp(ipAddress));
    }

    // ✅ Helper — get current admin ID
    private Long getCurrentAdminId() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getUserId).orElse(-1L);
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