package com.cmrit.demo.service;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Track failed login attempts per username
    private final ConcurrentHashMap<String, Integer>
            failedLoginAttempts = new ConcurrentHashMap<>();

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Register new user
    public User registerUser(String username,
                             String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException(
                    "Username already taken: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role != null ? role : "ROLE_USER");
        return userRepository.save(user);
    }

    // ✅ Validate login credentials
    public boolean validateLogin(String username,
                                 String rawPassword) {
        return userRepository.findByUsername(username)
                .map(u -> passwordEncoder.matches(
                        rawPassword, u.getPassword()))
                .orElse(false);
    }

    // ✅ Record failed login attempt
    public void recordFailedLogin(String username) {
        int attempts = failedLoginAttempts
                .getOrDefault(username, 0) + 1;
        failedLoginAttempts.put(username, attempts);
        System.out.println("❌ Failed login attempt "
                + attempts + " for: " + username);
    }

    // ✅ Check if exceeded max attempts
    public boolean hasExceededLoginAttempts(String username) {
        return failedLoginAttempts
                .getOrDefault(username, 0) >= MAX_LOGIN_ATTEMPTS;
    }

    // ✅ Get current failed count
    public int getFailedLoginAttempts(String username) {
        return failedLoginAttempts.getOrDefault(username, 0);
    }

    // ✅ Reset on successful login
    public void resetFailedLoginAttempts(String username) {
        failedLoginAttempts.remove(username);
    }

    // ✅ Get user by username
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found: "
                                                + username)));
    }

    // ✅ Change password
    public void changePassword(String username,
                               String newRawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }
}