// OtpService.java - Simple in-memory version
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
    
    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);
        
        if (otpData == null) {
            return false;
        }
        
        // Check if OTP is expired
        if (System.currentTimeMillis() - otpData.getTimestamp() > OTP_VALIDITY_DURATION) {
            otpStorage.remove(email);
            return false;
        }
        
        // Check if OTP matches
        boolean isValid = otpData.getOtp().equals(otp);
        if (isValid) {
            otpStorage.remove(email);
        }
        return isValid;
    }
    
    public void clearOtp(String email) {
        otpStorage.remove(email);
    }
    
    public String getOtp(String email) {
        OtpData otpData = otpStorage.get(email);
        return otpData != null ? otpData.getOtp() : null;
    }
}