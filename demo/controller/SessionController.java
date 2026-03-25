package com.cmrit.demo.controller;

import com.cmrit.demo.exception.ResourceNotFoundException;
import com.cmrit.demo.model.Session;
import com.cmrit.demo.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ✅ Start a new session
    @PostMapping("/start")
    public ResponseEntity<Session> startSession(
            @RequestParam Long userId,
            @RequestParam String token,
            HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String deviceInfo = request.getHeader("User-Agent");
        Session session = sessionService.startSession(
                userId, token, ipAddress, deviceInfo);
        return ResponseEntity.ok(session);
    }

    // ✅ End session by ID
    @PutMapping("/end/{id}")
    public ResponseEntity<String> endSession(
            @PathVariable Long id) {
        sessionService.endSession(id);
        return ResponseEntity.ok("Session ended successfully!");
    }

    // ✅ End session by token
    @PutMapping("/end/token")
    public ResponseEntity<String> endSessionByToken(
            @RequestParam String token) {
        sessionService.endSessionByToken(token);
        return ResponseEntity.ok("Session ended successfully!");
    }

    // ✅ Validate session token
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateSession(
            @RequestParam String token) {
        boolean isValid = sessionService.validateSession(token);
        if (!isValid) {
            throw new ResourceNotFoundException(
                    "Session not valid or expired for token: "
                            + token);
        }
        return ResponseEntity.ok(true);
    }

    // ✅ Get all sessions for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Session>> getSessionsByUser(
            @PathVariable Long userId) {
        List<Session> sessions =
                sessionService.getSessionByUser(userId);
        if (sessions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No sessions found for userId: " + userId);
        }
        return ResponseEntity.ok(sessions);
    }

    // ✅ Get active sessions for a user
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Session>> getActiveSessions(
            @PathVariable Long userId) {
        List<Session> sessions =
                sessionService.getActiveSessionsByUser(userId);
        if (sessions.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No active sessions found for userId: "
                            + userId);
        }
        return ResponseEntity.ok(sessions);
    }

    // ✅ End all sessions for a user
    @PutMapping("/user/{userId}/end-all")
    public ResponseEntity<String> endAllSessions(
            @PathVariable Long userId) {
        sessionService.endAllSessionsForUser(userId);
        return ResponseEntity.ok(
                "All sessions ended for userId: " + userId);
    }

    // ✅ Helper — extract real client IP
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}