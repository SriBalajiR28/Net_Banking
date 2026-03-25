package com.cmrit.demo.repository;

import com.cmrit.demo.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    // Find session by JWT token
    Optional<Session> findFirstByTokenOrderByStartTimeDesc(String token);

    // Find all sessions for a user
    List<Session> findByUserId(Long userId);

    // Find only active sessions — endTime is null
    List<Session> findByUserIdAndEndTimeIsNull(Long userId);
}