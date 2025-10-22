// OtpService.java - Simple in-memory version with OTP return for testing
package com.paymentgateway.service;

import org.springframework.stereotype.Service;

import com.paymentgateway.entity.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {
    
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long OTP_VALIDITY_DURATION = 10 * 60 * 1000; // 10 minutes
    
    public static class OtpData {
        private final String otp;
        private final long timestamp;
        private final String userEmail;
        
        public OtpData(String otp, long timestamp, String userEmail) {
            this.otp = otp;
            this.timestamp = timestamp;
            this.userEmail = userEmail;
        }
        
        public String getOtp() { return otp; }
        public long getTimestamp() { return timestamp; }
        public String getUserEmail() { return userEmail; }
    }
    
    // Returns the OTP string directly
    public String generateOtp(User user) {
        String otp = String.format("%06d", random.nextInt(999999));
        OtpData otpData = new OtpData(otp, System.currentTimeMillis(), user.getEmail());
        otpStorage.put(user.getEmail(), otpData);
        
        System.out.println("📧 OTP generated for " + user.getEmail() + ": " + otp);
        return otp; // Return the OTP string directly
    }
    
    // NEW METHOD: Generate OTP and return it for testing (without email sending)
    public String generateOtpForTesting(User user) {
        String otp = String.format("%06d", random.nextInt(999999));
        OtpData otpData = new OtpData(otp, System.currentTimeMillis(), user.getEmail());
        otpStorage.put(user.getEmail(), otpData);
        
        System.out.println("🎯 TEST OTP for " + user.getEmail() + ": " + otp);
        System.out.println("💡 Display this OTP to user for testing: " + otp);
        
        return otp;
    }
    
    // NEW METHOD: Generate OTP with email simulation (for testing)
    public Map<String, String> generateOtpWithResponse(User user) {
        String otp = String.format("%06d", random.nextInt(999999));
        OtpData otpData = new OtpData(otp, System.currentTimeMillis(), user.getEmail());
        otpStorage.put(user.getEmail(), otpData);
        
        System.out.println("🎯 OTP for " + user.getEmail() + ": " + otp);
        System.out.println("💡 FOR TESTING - Display this OTP to user: " + otp);
        
        // Return both success message and OTP for testing
        return Map.of(
            "message", "OTP generated successfully. Check console for testing OTP.",
            "otp", otp, // Include OTP in response for testing
            "email", user.getEmail()
        );
    }
    
    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            System.out.println("❌ OTP validation failed: No OTP found for " + email);
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() - otpData.getTimestamp() > OTP_VALIDITY_DURATION) {
            otpStorage.remove(email);
            System.out.println("❌ OTP validation failed: OTP expired for " + email);
            return false;
        }
        
        // Check if OTP matches
        boolean isValid = otpData.getOtp().equals(otp);
        if (isValid) {
            otpStorage.remove(email);
            System.out.println("✅ OTP validated successfully for " + email);
        } else {
            System.out.println("❌ OTP validation failed: Invalid OTP for " + email);
        }
        return isValid;
    }
    
    public void clearOtp(String email) {
        otpStorage.remove(email);
        System.out.println("🧹 OTP cleared for " + email);
    }
    
    public String getOtp(String email) {
        OtpData otpData = otpStorage.get(email);
        String otp = otpData != null ? otpData.getOtp() : null;
        System.out.println("🔍 Retrieved OTP for " + email + ": " + otp);
        return otp;
    }
    
    // NEW METHOD: Check if OTP exists and is valid
    public Map<String, Object> getOtpStatus(String email) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            return Map.of(
                "exists", false,
                "message", "No OTP found for this email"
            );
        }
        
        boolean isExpired = System.currentTimeMillis() - otpData.getTimestamp() > OTP_VALIDITY_DURATION;
        long timeLeft = OTP_VALIDITY_DURATION - (System.currentTimeMillis() - otpData.getTimestamp());
        
        return Map.of(
            "exists", true,
            "expired", isExpired,
            "timeLeftSeconds", timeLeft / 1000,
            "otp", isExpired ? "EXPIRED" : otpData.getOtp(), // Only return OTP if not expired
            "message", isExpired ? "OTP has expired" : "OTP is active"
        );
    }
}