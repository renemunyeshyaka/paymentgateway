// PasswordResetController.java - Complete and error-free
package com.kcoders.controller;

import com.kcoders.entity.PasswordResetToken;
import com.kcoders.entity.User;
import com.kcoders.service.EmailService;
import com.kcoders.service.PasswordResetService;
import com.kcoders.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/password-reset")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500"})
//@CrossOrigin(origins = {"http://dpg-d3r70femcj7s73bmmfe0-a.oregon-postgres.render.com:3000", "http://127.0.0.1:5500"})
public class PasswordResetController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            
            String email = request.getEmail().toLowerCase().trim();
            
            // Check if user exists using the correct method
            if (!userService.existsByEmail(email)) {
                // For security reasons, don't reveal if email exists or not
                return ResponseEntity.ok(createSuccessResponse(
                    "If the email exists in our system, a verification code has been sent."
                ));
            }
            
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // Create password reset token
                PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(user);
                
                // Send email with OTP
                emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
            }
            
            return ResponseEntity.ok(createSuccessResponse(
                "If the email exists in our system, a verification code has been sent."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred while processing your request"));
        }
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            
            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("OTP code is required"));
            }
            
            String email = request.getEmail().toLowerCase().trim();
            String otp = request.getOtp().toUpperCase().trim();
            
            // Validate token
            Optional<PasswordResetToken> tokenOptional = passwordResetService.validatePasswordResetToken(otp);
            
            if (tokenOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid or expired verification code"));
            }
            
            PasswordResetToken token = tokenOptional.get();
            
            // Verify token belongs to the requested email
            if (!token.getUser().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid verification code for this email"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Verification code validated successfully");
            response.put("token", token.getToken());
            response.put("email", token.getUser().getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred during OTP verification"));
        }
    }
    
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Token is required"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("New password is required"));
            }
            
            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Confirm password is required"));
            }
            
            // Check if passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(createErrorResponse("Passwords do not match"));
            }
            
            String email = request.getEmail().toLowerCase().trim();
            String tokenValue = request.getToken().toUpperCase().trim();
            
            // Validate token
            Optional<PasswordResetToken> tokenOptional = passwordResetService.validatePasswordResetToken(tokenValue);
            
            if (tokenOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid or expired verification code"));
            }
            
            PasswordResetToken token = tokenOptional.get();
            
            // Verify token belongs to the requested email
            if (!token.getUser().getEmail().equalsIgnoreCase(email)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid verification code for this email"));
            }
            
            User user = token.getUser();
            
            // Update password using the correct method
            userService.updatePassword(user, request.getNewPassword());
            
            // Mark token as used
            passwordResetService.markTokenAsUsed(token);
            
            // Send success email
            String userName = user.getFirstName() != null ? user.getFirstName() : user.getEmail();
            emailService.sendPasswordResetEmail(user.getEmail(), userName);
            
            return ResponseEntity.ok(createSuccessResponse(
                "Password has been reset successfully. You can now log in with your new password."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred while resetting password"));
        }
    }
    
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody PasswordResetRequest request) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            
            String email = request.getEmail().toLowerCase().trim();
            
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                // For security reasons, don't reveal if email exists or not
                return ResponseEntity.ok(createSuccessResponse(
                    "If the email exists in our system, a verification code has been sent."
                ));
            }
            
            User user = userOptional.get();
            
            // Create new password reset token
            PasswordResetToken resetToken = passwordResetService.createPasswordResetToken(user);
            
            // Send email with new OTP
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
            
            return ResponseEntity.ok(createSuccessResponse(
                "If the email exists in our system, a verification code has been sent."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("An error occurred while resending OTP"));
        }
    }
    
    // Helper methods for response formatting
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
    
    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
    
    // Request DTO classes
    public static class PasswordResetRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class VerifyOtpRequest {
        private String email;
        private String otp;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    public static class NewPasswordRequest {
        private String email;
        private String token;
        private String newPassword;
        private String confirmPassword;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }
}