package com.cmrit.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(unique = true)
    private String token;

    private String ipAddress;

    private String deviceInfo;

    private LocalDateTime startTime;

    private LocalDateTime endTime;    // null = still active

    private LocalDateTime expiresAt;  // auto expire after 30 mins
}