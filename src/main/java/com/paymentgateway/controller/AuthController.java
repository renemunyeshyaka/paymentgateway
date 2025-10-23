package com.paymentgateway.controller;

import com.paymentgateway.entity.User;
import com.paymentgateway.service.JwtTokenProvider;
import com.paymentgateway.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserService userService,
                         JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // ... your existing methods ...

    @PostMapping("/verify-otp-simple")
    public ResponseEntity<?> verifyOtpSimple(@RequestBody OtpVerificationRequest request) {
        try {
            System.out.println("🔧 Verifying OTP for: " + request.getEmail());
            
            boolean isValid = userService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (isValid) {
                // Generate JWT token - FIXED LINE
                User user = userService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                String token = jwtTokenProvider.generateToken(user.getEmail());
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("token", token);
                response.put("user", Map.of(
                    "email", user.getEmail(),
                    "firstName", user.getFirstName(),
                    "lastName", user.getLastName()
                ));
                
                System.out.println("✅ OTP verified and login successful for: " + request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Invalid OTP for: " + request.getEmail());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid OTP"));
            }
            
        } catch (Exception e) {
            System.out.println("💥 OTP verification error: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "OTP verification failed: " + e.getMessage()));
        }
    }

    // Inner class for request
    public static class OtpVerificationRequest {
        private String email;
        private String otp;

        // getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
}