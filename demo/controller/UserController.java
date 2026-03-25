package com.cmrit.demo.controller;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Get profile by ID
    @GetMapping("/profile/{id}")
    public ResponseEntity<User> getProfile(
            @PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + id));
        return ResponseEntity.ok(user);
    }

    // ✅ Get own profile from token
    @GetMapping("/profile/me")
    public ResponseEntity<User> getMyProfile() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));
        return ResponseEntity.ok(user);
    }
}