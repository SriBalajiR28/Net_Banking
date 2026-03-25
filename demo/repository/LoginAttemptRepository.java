package com.cmrit.demo.repository;

import com.cmrit.demo.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginAttemptRepository
        extends JpaRepository<LoginAttempt, Long> {

    // All attempts for a specific user
    List<LoginAttempt> findByUserId(Long userId);

    // All attempts by status — OTP_SENT, OTP_SUCCESS etc
    List<LoginAttempt> findByStatus(String status);

    // All attempts from a specific IP
    List<LoginAttempt> findByIpAddress(String ipAddress);

    // All attempts for a user filtered by status
    List<LoginAttempt> findByUserIdAndStatus(
            Long userId, String status);
}