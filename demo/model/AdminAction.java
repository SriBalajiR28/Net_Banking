package com.cmrit.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "admin_action")
public class AdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    // CREATE_USER, DELETE_USER, VIEW_USERS,
    // END_SESSION, VIEW_AUDIT_LOGS etc.
    private String actionType;

    private Long targetId;   // affected user/session ID

    private String ipAddress;

    private LocalDateTime timestamp;
}