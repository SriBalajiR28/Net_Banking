package com.cmrit.demo.service;

import com.cmrit.demo.exception.InvalidOTPException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private final JavaMailSender mailSender;

    // ✅ Thread safe OTP store
    private final ConcurrentHashMap<String, OtpEntry>
            otpStore = new ConcurrentHashMap<>();

    // ✅ Track failed OTP attempts per user
    private final ConcurrentHashMap<String, Integer>
            failedOtpAttempts = new ConcurrentHashMap<>();

    private static final int OTP_VALIDITY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 3;

    public OTPService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ Generate OTP with expiry
    public String generateOtp(String username) {
        otpStore.remove(username);
        failedOtpAttempts.remove(username);

        String otp = String.format("%06d",
                new Random().nextInt(999999));
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(OTP_VALIDITY_MINUTES);

        otpStore.put(username, new OtpEntry(otp, expiresAt));
        System.out.println("✅ OTP generated for: " + username
                + " | Expires at: " + expiresAt);
        return otp;
    }

    // ✅ Send OTP via Gmail SMTP
    public void sendOtpByEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Net Banking OTP");
        message.setText(
                "Dear Customer,\n\n" +
                "Your OTP for Net Banking login is: "
                + otp + "\n\n" +
                "Valid for " + OTP_VALIDITY_MINUTES
                + " minutes.\n" +
                "Do NOT share this OTP with anyone.\n\n" +
                "Regards,\nNet Banking Security Team"
        );
        mailSender.send(message);
        System.out.println("✅ OTP email sent to: " + email);
    }

    // ✅ Validate OTP — checks existence, expiry, value
    public boolean validateOTP(String username, String otp) {

        OtpEntry entry = otpStore.get(username);

        // Check 1 — OTP exists
        if (entry == null) {
            throw new InvalidOTPException(
                    "No OTP found for user: " + username
                    + ". Please request a new OTP.");
        }

        // Check 2 — OTP not expired
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(username);
            throw new InvalidOTPException(
                    "OTP expired for user: " + username
                    + ". Please request a new OTP.");
        }

        // Check 3 — OTP value matches
        if (!entry.otp().equals(otp)) {
            int attempts = failedOtpAttempts
                    .getOrDefault(username, 0) + 1;
            failedOtpAttempts.put(username, attempts);
            throw new InvalidOTPException(
                    "Invalid OTP for user: " + username);
        }

        return true;
    }

    // ✅ Check if exceeded max OTP attempts
    public boolean hasExceededOtpAttempts(String username) {
        return failedOtpAttempts
                .getOrDefault(username, 0) >= MAX_OTP_ATTEMPTS;
    }

    // ✅ Get current failed OTP count
    public int getFailedOtpAttempts(String username) {
        return failedOtpAttempts.getOrDefault(username, 0);
    }

    // ✅ Clear OTP after successful validation
    public void clearOTP(String username) {
        otpStore.remove(username);
        failedOtpAttempts.remove(username);
        System.out.println("✅ OTP cleared for: " + username);
    }

    // ✅ Check if OTP is still pending
    public boolean hasPendingOtp(String username) {
        OtpEntry entry = otpStore.get(username);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            otpStore.remove(username);
            return false;
        }
        return true;
    }

    // ✅ Private record — holds OTP + expiry together
    private record OtpEntry(String otp,
                             LocalDateTime expiresAt) {}
}