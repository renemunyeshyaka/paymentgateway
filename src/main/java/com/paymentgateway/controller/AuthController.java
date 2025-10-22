// AuthController.java - Complete with ALL endpoints
package com.paymentgateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.paymentgateway.entity.User;
import com.paymentgateway.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthenticationManager authenticationManager;

    // ========== AUTH ENDPOINTS ==========

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            System.out.println("📝 REGISTER: Attempting to register: " + user.getEmail());
            
            // Basic validation
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
            }
            
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("First name is required"));
            }
            
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Last name is required"));
            }

            User registeredUser = userService.registerUser(user);
            
            // Remove sensitive data
            registeredUser.setPassword(null);
            registeredUser.setActivationToken(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please check your email for activation link.");
            response.put("user", registeredUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam String email) {
        try {
            System.out.println("🔧 ACTIVATE: Attempting to activate: " + email);
            boolean activated = userService.activateUser(email);
            
            if (activated) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Account activated successfully. You can now login.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(createErrorResponse("Invalid email or account already activated"));
            }
            
        } catch (Exception e) {
            System.out.println("❌ Activation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Activation failed"));
        }
    }

	/*
	 * @PostMapping("/login") public ResponseEntity<?> initiateLogin(@RequestBody
	 * LoginRequest loginRequest) { try {
	 * System.out.println("🔐 LOGIN: Attempt for: " + loginRequest.getEmail());
	 * 
	 * // Validate input if (loginRequest.getEmail() == null ||
	 * loginRequest.getEmail().trim().isEmpty()) { return
	 * ResponseEntity.badRequest().body(createErrorResponse("Email is required")); }
	 * 
	 * if (loginRequest.getPassword() == null ||
	 * loginRequest.getPassword().trim().isEmpty()) { return
	 * ResponseEntity.badRequest().body(createErrorResponse("Password is required"))
	 * ; }
	 * 
	 * // Authenticate using Spring Security Authentication authentication =
	 * authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(
	 * loginRequest.getEmail(), loginRequest.getPassword() ) );
	 * 
	 * System.out.println("✅ Authentication successful for: " +
	 * loginRequest.getEmail());
	 * 
	 * // Generate and send OTP - NOW RETURNS MAP WITH OTP Map<String, String>
	 * otpResponse = userService.generateAndSendOtp(loginRequest.getEmail()); String
	 * otp = otpResponse.get("otp");
	 * 
	 * System.out.println("📧 OTP generated for: " + loginRequest.getEmail() +
	 * " - OTP: " + otp);
	 * 
	 * // Return the OTP in response for testing Map<String, Object> response = new
	 * HashMap<>(); response.put("message",
	 * "OTP sent to your email. Please verify to complete login.");
	 * response.put("email", loginRequest.getEmail()); response.put("otp", otp); //
	 * Include OTP in response for testing response.put("note",
	 * "Use this OTP for verification. Check console for details.");
	 * 
	 * return ResponseEntity.ok(response);
	 * 
	 * } catch (Exception e) { System.out.println("❌ Login failed: " +
	 * e.getMessage()); return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	 * .body(createErrorResponse("Invalid email or password")); } }
	 */
    
 // In your AuthController.java
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔐 Login attempt for: " + loginRequest.getEmail());
            
            // First, authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), 
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            User user = (User) authentication.getPrincipal();
            System.out.println("✅ Password authentication successful for: " + user.getEmail());
            
            // Check if user is active
            if (!user.getIsActive()) {
                System.out.println("❌ User not active: " + user.getEmail());
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Account not activated. Please check your email."));
            }
            
            // Generate OTP and return it directly for testing
            Map<String, String> otpResponse = userService.generateAndSendOtp(user.getEmail());
            
            // Return OTP in response for easy testing
            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP sent to your email");
            response.put("otp", otpResponse.get("otp")); // Include OTP for testing
            response.put("email", user.getEmail());
            response.put("note", "Use this OTP for verification");
            
            System.out.println("🎯 OTP for login: " + otpResponse.get("otp"));
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            System.out.println("❌ Invalid credentials for: " + loginRequest.getEmail());
            return ResponseEntity.status(401)
                .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            System.out.println("💥 Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }
    
    
    
	/*
	 * @PostMapping("/verify-otp") public ResponseEntity<?>
	 * verifyOtpAndLogin(@RequestBody OtpVerificationRequest otpRequest) { try {
	 * System.out.println("🔑 OTP VERIFY: For: " + otpRequest.getEmail());
	 * 
	 * if (otpRequest.getEmail() == null || otpRequest.getEmail().trim().isEmpty())
	 * { return
	 * ResponseEntity.badRequest().body(createErrorResponse("Email is required")); }
	 * 
	 * if (otpRequest.getOtp() == null || otpRequest.getOtp().trim().isEmpty()) {
	 * return
	 * ResponseEntity.badRequest().body(createErrorResponse("OTP is required")); }
	 * 
	 * // Verify OTP boolean otpValid = userService.verifyOtp(otpRequest.getEmail(),
	 * otpRequest.getOtp());
	 * 
	 * if (!otpValid) { return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	 * .body(createErrorResponse("Invalid or expired OTP")); }
	 * 
	 * // Complete authentication User user = (User)
	 * userService.loadUserByUsername(otpRequest.getEmail()); Authentication
	 * authentication = new UsernamePasswordAuthenticationToken( user, null,
	 * user.getAuthorities() );
	 * 
	 * SecurityContextHolder.getContext().setAuthentication(authentication);
	 * 
	 * // Remove sensitive data user.setPassword(null); user.setOtp(null);
	 * 
	 * Map<String, Object> response = new HashMap<>(); response.put("message",
	 * "Login successful"); response.put("user", user);
	 * 
	 * return ResponseEntity.ok(response);
	 * 
	 * } catch (Exception e) { System.out.println("❌ OTP verification failed: " +
	 * e.getMessage()); return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body(createErrorResponse("OTP verification failed")); } }
	 * 
	 * // ADD THIS ENDPOINT - Resend OTP
	 * 
	 * @PostMapping("/resend-otp") public ResponseEntity<?> resendOtp(@RequestBody
	 * ResendOtpRequest request) { try { String email = request.getEmail();
	 * System.out.println("📧 RESEND OTP: Request for: " + email);
	 * 
	 * if (email == null || email.trim().isEmpty()) { return
	 * ResponseEntity.badRequest().body(createErrorResponse("Email is required")); }
	 * 
	 * // Generate and send new OTP - NOW RETURNS MAP WITH OTP Map<String, String>
	 * otpResponse = userService.generateAndSendOtp(email); String otp =
	 * otpResponse.get("otp");
	 * 
	 * System.out.println("📧 New OTP sent to: " + email + " - OTP: " + otp);
	 * 
	 * // Return OTP in response for testing Map<String, Object> response = new
	 * HashMap<>(); response.put("message", "New OTP sent to your email");
	 * response.put("email", email); response.put("otp", otp); // Include OTP in
	 * response for testing response.put("note",
	 * "Use this new OTP for verification");
	 * 
	 * return ResponseEntity.ok(response);
	 * 
	 * } catch (Exception e) { System.out.println("❌ Resend OTP failed: " +
	 * e.getMessage()); return
	 * ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	 * .body(createErrorResponse("Failed to resend OTP: " + e.getMessage())); } }
	 */
    
    @PostMapping("/verify-otp-simple")
    public ResponseEntity<?> verifyOtpSimple(@RequestBody OtpVerificationRequest request) {
        try {
            System.out.println("🔧 Verifying OTP for: " + request.getEmail());
            
            boolean isValid = userService.verifyOtp(request.getEmail(), request.getOtp());
            
            if (isValid) {
                // Generate JWT token
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

    // NEW ENDPOINT: Quick OTP for testing (bypasses email)
    @PostMapping("/quick-otp")
    public ResponseEntity<?> generateQuickOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("🚀 QUICK OTP: Request for: " + email);
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }

            // Generate quick OTP (in-memory only, no email)
            Map<String, String> otpResponse = userService.generateQuickOtp(email);
            
            System.out.println("🎯 Quick OTP generated for testing: " + email + " - OTP: " + otpResponse.get("otp"));
            
            // Return OTP immediately for testing
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quick OTP generated for testing");
            response.put("email", email);
            response.put("otp", otpResponse.get("otp"));
            response.put("validity", "10 minutes");
            response.put("note", "Use this OTP immediately for testing. No email sent.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Quick OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to generate quick OTP: " + e.getMessage()));
        }
    }

    // NEW ENDPOINT: Generate OTP without email
    @PostMapping("/otp-without-email")
    public ResponseEntity<?> generateOtpWithoutEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("📱 OTP WITHOUT EMAIL: Request for: " + email);
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email is required"));
            }

            // Generate OTP without sending email
            Map<String, String> otpResponse = userService.generateOtpWithoutEmail(email);
            
            System.out.println("📱 OTP generated without email for: " + email + " - OTP: " + otpResponse.get("otp"));
            
            // Return OTP in response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP generated successfully (no email sent)");
            response.put("email", email);
            response.put("otp", otpResponse.get("otp"));
            response.put("note", "This OTP is stored in database and can be used for verification");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ OTP generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to generate OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/direct-login")
    public ResponseEntity<?> directLogin(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("🔐 DIRECT LOGIN: Attempt for: " + loginRequest.getEmail());
            
            // Check if user exists
            Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not found"));
            }
            
            User user = userOptional.get();
            System.out.println("✅ User found: " + user.getEmail());
            
            // Handle null isActive - treat as active
            boolean isUserActive = user.getIsActive() != null ? user.getIsActive() : true;
            
            // Manual password check
            boolean passwordMatches = userService.getPasswordEncoder().matches(
                loginRequest.getPassword(), 
                user.getPassword()
            );
            
            System.out.println("🔑 Password matches: " + passwordMatches);
            
            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid password"));
            }
            
            if (!user.getEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Account is disabled"));
            }
            
            if (!isUserActive) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Account is not activated"));
            }
            
            // Login successful - generate session
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            user.setPassword(null); // Remove password from response
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", user);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Direct login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    // ========== DEBUG ENDPOINTS ==========

    @PostMapping("/debug/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            System.out.println("🔑 DEBUG: Updating password for: " + email);
            
            if (email == null || password == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Email and password are required"));
            }
            
            userService.updateUserPassword(email, password);
            
            return ResponseEntity.ok(createSuccessResponse("Password updated successfully to BCrypt"));
        } catch (Exception e) {
            System.out.println("❌ Error updating password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update password: " + e.getMessage()));
        }
    }

    @PostMapping("/debug/fix-users")
    public ResponseEntity<?> fixExistingUsers() {
        try {
            System.out.println("🔧 Running user fix...");
            userService.fixExistingUsers();
            
            return ResponseEntity.ok(createSuccessResponse("Users fixed successfully"));
        } catch (Exception e) {
            System.out.println("❌ Error fixing users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fix users: " + e.getMessage()));
        }
    }

    @PostMapping("/debug/check-user")
    public ResponseEntity<?> checkUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        System.out.println("🔍 DEBUG: Checking user: " + email);
        
        Optional<User> userOptional = userService.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("active", user.getIsActive());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("id", user.getId());
            response.put("enabled", user.getEnabled());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok(Collections.singletonMap("exists", false));
        }
    }

    @GetMapping("/debug/test")
    public ResponseEntity<?> testEndpoint() {
        System.out.println("✅ DEBUG: Test endpoint reached");
        return ResponseEntity.ok(Collections.singletonMap("message", "Auth API is working!"));
    }

    // Helper methods
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

    // Request DTOs
    public static class LoginRequest {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class OtpVerificationRequest {
        private String email;
        private String otp;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }

    // ADD THIS DTO for resend OTP
    public static class ResendOtpRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    
}