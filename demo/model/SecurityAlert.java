package com.cmrit.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "security_alerts")
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    // ✅ Linked to User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // SUSPICIOUS_IP, OTP_ABUSE,
    // CONCURRENT_SESSION, MULTIPLE_FAILED_LOGINS
    @Column(nullable = false)
    private String alertType;

    // LOW, MEDIUM, HIGH, CRITICAL
    @Column(nullable = false)
    private String severity;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String ipAddress;

    // OPEN, RESOLVED, DISMISSED
    @Column(nullable = false)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;  // null if still open

    private String resolvedBy;         // admin username

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "OPEN";
    }
}