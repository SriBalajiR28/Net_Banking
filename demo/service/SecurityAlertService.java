package com.cmrit.demo.service;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.SecurityAlert;
import com.cmrit.demo.model.User;
import com.cmrit.demo.repository.SecurityAlertRepository;
import com.cmrit.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAlertService {

    private final SecurityAlertRepository alertRepository;
    private final UserRepository userRepository;

    public SecurityAlertService(
            SecurityAlertRepository alertRepository,
            UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }

    // ✅ Raise a new security alert
    public SecurityAlert raiseAlert(Long userId,
                                     String alertType,
                                     String severity,
                                     String message,
                                     String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId));

        SecurityAlert alert = new SecurityAlert();
        alert.setUser(user);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setIpAddress(ipAddress);
        // status defaults to OPEN via @PrePersist

        SecurityAlert saved = alertRepository.save(alert);
        System.out.println("🚨 Security alert raised: "
                + alertType + " | Severity: " + severity
                + " | User: " + user.getUsername());
        return saved;
    }

    // ✅ Resolve alert
    public SecurityAlert resolveAlert(Long alertId,
                                       String resolvedBy) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with ID: " + alertId));
        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        System.out.println("✅ Alert resolved: "
                + alertId + " by: " + resolvedBy);
        return alertRepository.save(alert);
    }

    // ✅ Dismiss alert
    public SecurityAlert dismissAlert(Long alertId,
                                       String resolvedBy) {
        SecurityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alert not found with ID: " + alertId));
        alert.setStatus("DISMISSED");
        alert.setResolvedAt(LocalDateTime.now());
        alert.setResolvedBy(resolvedBy);
        return alertRepository.save(alert);
    }

    // ✅ Get all alerts
    public List<SecurityAlert> getAllAlerts() {
        return alertRepository.findAll();
    }

    // ✅ Get alerts by user ID
    public List<SecurityAlert> getAlertsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userId));
        return alertRepository.findByUser(user);
    }

    // ✅ Get open alerts only
    public List<SecurityAlert> getOpenAlerts() {
        return alertRepository.findByStatus("OPEN");
    }

    // ✅ Get alerts by severity
    public List<SecurityAlert> getAlertsBySeverity(
            String severity) {
        return alertRepository.findBySeverity(severity);
    }

    // ✅ Get alerts by type
    public List<SecurityAlert> getAlertsByType(String alertType) {
        return alertRepository.findByAlertType(alertType);
    }

    // ✅ Get alerts by IP
    public List<SecurityAlert> getAlertsByIp(String ipAddress) {
        return alertRepository.findByIpAddress(ipAddress);
    }

    // ✅ Auto raise — OTP abuse
    public void raiseOtpAbuseAlert(Long userId,
                                    String ipAddress) {
        raiseAlert(userId, "OTP_ABUSE", "HIGH",
                "Multiple failed OTP attempts detected",
                ipAddress);
    }

    // ✅ Auto raise — concurrent session
    public void raiseConcurrentSessionAlert(Long userId,
                                             String ipAddress) {
        raiseAlert(userId, "CONCURRENT_SESSION", "MEDIUM",
                "Maximum concurrent sessions limit reached",
                ipAddress);
    }

    // ✅ Auto raise — suspicious IP
    public void raiseSuspiciousIpAlert(Long userId,
                                        String ipAddress) {
        raiseAlert(userId, "SUSPICIOUS_IP", "CRITICAL",
                "Login attempt from suspicious IP: " + ipAddress,
                ipAddress);
    }
}