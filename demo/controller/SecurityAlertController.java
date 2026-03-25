package com.cmrit.demo.controller;

import com.cmrit.demo.model.SecurityAlert;
import com.cmrit.demo.service.SecurityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class SecurityAlertController {

    private final SecurityAlertService alertService;

    public SecurityAlertController(
            SecurityAlertService alertService) {
        this.alertService = alertService;
    }

    // ✅ Get all alerts
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    // ✅ Get open alerts
    @GetMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getOpenAlerts() {
        return ResponseEntity.ok(alertService.getOpenAlerts());
    }

    // ✅ Get alerts by user ID
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAlertsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                alertService.getAlertsByUser(userId));
    }

    // ✅ Get alerts by severity
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>>
            getAlertsBySeverity(@PathVariable String severity) {
        return ResponseEntity.ok(
                alertService.getAlertsBySeverity(severity));
    }

    // ✅ Get alerts by type
    @GetMapping("/type/{alertType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAlertsByType(
            @PathVariable String alertType) {
        return ResponseEntity.ok(
                alertService.getAlertsByType(alertType));
    }

    // ✅ Get alerts by IP
    @GetMapping("/ip/{ipAddress}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAlert>> getAlertsByIp(
            @PathVariable String ipAddress) {
        return ResponseEntity.ok(
                alertService.getAlertsByIp(ipAddress));
    }

    // ✅ Raise alert manually
    @PostMapping("/raise/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityAlert> raiseAlert(
            @PathVariable Long userId,
            @RequestParam String alertType,
            @RequestParam String severity,
            @RequestParam String message,
            @RequestParam String ipAddress) {
        return ResponseEntity.ok(alertService.raiseAlert(
                userId, alertType, severity,
                message, ipAddress));
    }

    // ✅ Resolve alert
    @PutMapping("/resolve/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityAlert> resolveAlert(
            @PathVariable Long alertId) {
        String adminUsername = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                alertService.resolveAlert(
                        alertId, adminUsername));
    }

    // ✅ Dismiss alert
    @PutMapping("/dismiss/{alertId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityAlert> dismissAlert(
            @PathVariable Long alertId) {
        String adminUsername = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(
                alertService.dismissAlert(
                        alertId, adminUsername));
    }
}