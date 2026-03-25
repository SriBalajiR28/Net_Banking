package com.cmrit.demo.service;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.Session;
import com.cmrit.demo.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int MAX_CONCURRENT_SESSIONS = 2;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    // ✅ Start session with concurrent control
    public Session startSession(Long userId, String token,
                                String ipAddress,
                                String deviceInfo) {

        // Fetch all sessions for this user
        List<Session> allSessions =
                sessionRepository.findByUserId(userId);

        // Filter only active non-expired sessions
        List<Session> activeSessions = allSessions.stream()
                .filter(s -> s.getEndTime() == null
                        && s.getExpiresAt() != null
                        && LocalDateTime.now()
                        .isBefore(s.getExpiresAt()))
                .collect(Collectors.toList());

        // Invalidate oldest if at max limit
        if (activeSessions.size() >= MAX_CONCURRENT_SESSIONS) {
            Session oldest = activeSessions.stream()
                    .min((a, b) -> a.getStartTime()
                            .compareTo(b.getStartTime()))
                    .orElse(activeSessions.get(0));
            oldest.setEndTime(LocalDateTime.now());
            sessionRepository.save(oldest);
            System.out.println("⚠️ Max sessions reached — "
                    + "oldest invalidated for userId: " + userId);
        }

        // Create new session
        Session session = new Session();
        session.setUserId(userId);
        session.setToken(token);
        session.setIpAddress(ipAddress);
        session.setDeviceInfo(deviceInfo);
        session.setStartTime(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now()
                .plusMinutes(SESSION_TIMEOUT_MINUTES));

        Session saved = sessionRepository.save(session);
        System.out.println("✅ Session started for userId: "
                + userId);
        return saved;
    }

    // ✅ End session by ID
    public void endSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session not found with ID: " + sessionId));
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);
        System.out.println("✅ Session ended: " + sessionId);
    }

    // ✅ End session by token — used during logout
    public void endSessionByToken(String token) {
        sessionRepository
                .findFirstByTokenOrderByStartTimeDesc(token)
                .ifPresent(session -> {
                    session.setEndTime(LocalDateTime.now());
                    sessionRepository.save(session);
                    System.out.println("✅ Session ended by token");
                });
    }

    // ✅ Validate — checks endTime and expiresAt
    public boolean validateSession(String token) {
        return sessionRepository
                .findFirstByTokenOrderByStartTimeDesc(token)
                .map(s -> s.getEndTime() == null
                        && s.getExpiresAt() != null
                        && LocalDateTime.now()
                        .isBefore(s.getExpiresAt()))
                .orElse(false);
    }

    // ✅ Get all sessions for a user
    public List<Session> getSessionByUser(Long userId) {
        return sessionRepository.findByUserId(userId);
    }

    // ✅ Get only active sessions for a user
    public List<Session> getActiveSessionsByUser(Long userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .filter(s -> s.getEndTime() == null
                        && s.getExpiresAt() != null
                        && LocalDateTime.now()
                        .isBefore(s.getExpiresAt()))
                .collect(Collectors.toList());
    }

    // ✅ End all sessions — force logout all devices
    public void endAllSessionsForUser(Long userId) {
        getActiveSessionsByUser(userId).forEach(s -> {
            s.setEndTime(LocalDateTime.now());
            sessionRepository.save(s);
        });
        System.out.println("✅ All sessions ended for userId: "
                + userId);
    }
}