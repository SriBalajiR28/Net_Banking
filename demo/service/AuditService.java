package com.cmrit.demo.service;

import com.cmrit.demo.model.AdminAction;
import com.cmrit.demo.model.LoginAttempt;
import com.cmrit.demo.repository.AdminActionRepository;
import com.cmrit.demo.repository.LoginAttemptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AdminActionRepository adminActionRepository;
    private final LoginAttemptRepository loginAttemptRepository;

    public AuditService(
            AdminActionRepository adminActionRepository,
            LoginAttemptRepository loginAttemptRepository) {
        this.adminActionRepository = adminActionRepository;
        this.loginAttemptRepository = loginAttemptRepository;
    }

    // ✅ Record admin actions — goes to admin_action table
    public void recordAdminAction(Long adminId,
                                   String actionType,
                                   Long targetId,
                                   String ipAddress) {
        AdminAction action = new AdminAction();
        action.setAdminId(adminId);
        action.setActionType(actionType);
        action.setTargetId(targetId);
        action.setIpAddress(ipAddress);
        action.setTimestamp(LocalDateTime.now());
        adminActionRepository.save(action);
        System.out.println("✅ Admin action recorded: "
                + actionType + " by adminId: " + adminId);
    }

    // ✅ Record login attempts — goes to login_attempts table
    public void recordLoginAttempt(Long userId,
                                    String status,
                                    String ipAddress) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUserId(userId);
        attempt.setStatus(status);
        attempt.setIpAddress(ipAddress);
        attempt.setTimestamp(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
        System.out.println("✅ Login attempt recorded: "
                + status + " for userId: " + userId);
    }

    // ✅ Record OTP attempts — goes to login_attempts table
    public void recordOtpAttempt(Long userId,
                                  String status,
                                  String ipAddress) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUserId(userId);
        attempt.setStatus("OTP_" + status);
        attempt.setIpAddress(ipAddress);
        attempt.setTimestamp(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
        System.out.println("✅ OTP attempt recorded: "
                + status + " for userId: " + userId);
    }

    // ✅ Admin action queries
    public List<AdminAction> getAllActions() {
        return adminActionRepository.findAll();
    }

    public List<AdminAction> findByAdminId(Long adminId) {
        return adminActionRepository.findByAdminId(adminId);
    }

    public List<AdminAction> findByActionType(String actionType) {
        return adminActionRepository.findByActionType(actionType);
    }

    public List<AdminAction> findByAdminIdAndActionType(
            Long adminId, String actionType) {
        return adminActionRepository
                .findByAdminIdAndActionType(adminId, actionType);
    }

    // ✅ Login attempt queries
    public List<LoginAttempt> getAllLoginAttempts() {
        return loginAttemptRepository.findAll();
    }

    public List<LoginAttempt> getLoginAttemptsByUser(Long userId) {
        return loginAttemptRepository.findByUserId(userId);
    }

    public List<LoginAttempt> getLoginAttemptsByStatus(
            String status) {
        return loginAttemptRepository.findByStatus(status);
    }

    public List<LoginAttempt> getLoginAttemptsByIp(
            String ipAddress) {
        return loginAttemptRepository.findByIpAddress(ipAddress);
    }
}