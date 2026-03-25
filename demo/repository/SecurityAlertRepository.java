package com.cmrit.demo.repository;

import com.cmrit.demo.model.SecurityAlert;
import com.cmrit.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, Long> {

    // All alerts for a specific user
    List<SecurityAlert> findByUser(User user);

    // All alerts by severity — HIGH, CRITICAL etc
    List<SecurityAlert> findBySeverity(String severity);

    // All alerts by type
    List<SecurityAlert> findByAlertType(String alertType);

    // All alerts by status — OPEN, RESOLVED, DISMISSED
    List<SecurityAlert> findByStatus(String status);

    // All open alerts for a specific user
    List<SecurityAlert> findByUserAndStatus(
            User user, String status);

    // All alerts from a specific IP
    List<SecurityAlert> findByIpAddress(String ipAddress);
}