package com.cmrit.demo.service;

import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        // ✅ Null/blank role fallback
        String role = user.getRole();
        if (role == null || role.isBlank()) {
            role = "ROLE_USER";
        }

        // ✅ Ensure ROLE_ prefix
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        // ✅ Use authorities() not roles()
        // roles() adds ROLE_ prefix automatically
        // causing ROLE_ROLE_ADMIN double prefix bug
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}