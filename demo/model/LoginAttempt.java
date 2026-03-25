package com.cmrit.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    private Long userId;

    private Long targetId;

    private String status; // OTP_SENT, OTP_SUCCESS, OTP_FAILURE

    private String ipAddress;

    private LocalDateTime timestamp;
}