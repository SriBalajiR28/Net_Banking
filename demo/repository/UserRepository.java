package com.cmrit.demo.repository;

import com.cmrit.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username — used in login + JWT filter
    Optional<User> findByUsername(String username);

    // Check if admin already exists — used in register-admin
    boolean existsByRole(String role);
}