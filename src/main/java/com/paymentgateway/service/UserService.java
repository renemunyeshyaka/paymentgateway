// UserService.java - Complete and fixed version
package com.paymentgateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paymentgateway.entity.User;
import com.paymentgateway.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;


@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OtpService otpService;
    
    @Value("${app.email.verification.enabled:true}")
    private boolean emailVerificationEnabled;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔐 loadUserByUsername called for: " + email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("❌ User not found: " + email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });
        
        System.out.println("✅ User found: " + user.getEmail());
        System.out.println("   🔓 Active: " + user.getIsActive());
        System.out.println("   ✅ Enabled: " + user.getEnabled());
        
        if (!user.getIsActive()) {
            System.out.println("❌ User not active: " + email);
            throw new UsernameNotFoundException("Account is not activated");
        }
        
        return user;
    }

    public User registerUser(User user) {
        try {
            System.out.println("🔧 Starting registration for: " + user.getEmail());
            
            // Check if user already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                System.out.println("❌ User already exists: " + user.getEmail());
                throw new RuntimeException("User already exists with email: " + user.getEmail());
            }
            
            // Encode password before saving
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            System.out.println("✅ Password encoded for: " + user.getEmail());
            
            // Set activation details and default values
            String activationToken = UUID.randomUUID().toString();
            user.setActivationToken(activationToken);
            user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

            // Auto-activate if email verification is disabled
            if (!emailVerificationEnabled) {
                user.setIsActive(true);
                System.out.println("✅ AUTO-ACTIVATION: User activated immediately - " + user.getEmail());
            } else {
                user.setIsActive(false);
                System.out.println("⏳ EMAIL VERIFICATION: User requires activation - " + user.getEmail());
            }

            user.setMfaEnabled(false);
            user.setRole("USER"); // Set default role
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("💾 Saving user to database: " + user.getEmail());
            User savedUser = userRepository.save(user);
            System.out.println("✅ User saved with ID: " + savedUser.getId());
            
            // Send activation email only if email verification is enabled
            if (emailVerificationEnabled) {
                try {
                    String userName = savedUser.getFirstName() + " " + savedUser.getLastName();
                    System.out.println("📧 Sending activation email to: " + savedUser.getEmail());
                    emailService.sendActivationEmail(savedUser.getEmail(), userName);
                } catch (Exception emailException) {
                    System.out.println("⚠️ Activation email failed, but user was registered: " + emailException.getMessage());
                    // Don't throw the exception - user is still registered successfully
                }
            } else {
                System.out.println("✅ SKIPPING EMAIL: Auto-activation enabled for: " + savedUser.getEmail());
            }
            
            System.out.println("🎉 Registration completed successfully for: " + savedUser.getEmail());
            return savedUser;
            
        } catch (Exception e) {
            System.out.println("💥 Registration failed for " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    // SIMPLIFIED VERSION FOR TESTING - Auto-activates users
    public User registerUserAutoActivate(User user) {
        try {
            System.out.println("🔧 TEST MODE: Registering user with auto-activation: " + user.getEmail());
            
            // Check if user already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new RuntimeException("User already exists with email: " + user.getEmail());
            }
            
            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Auto-activate for testing
            user.setIsActive(true);
            user.setActivationToken(null);
            user.setActivationTokenExpiry(null);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            User savedUser = userRepository.save(user);
            System.out.println("✅ TEST USER registered and activated: " + savedUser.getEmail());
            
            return savedUser;
            
        } catch (Exception e) {
            System.out.println("❌ Test registration failed: " + e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public boolean activateUser(String email) {
        try {
            System.out.println("🔧 Activating user: " + email);
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setIsActive(true);
                user.setActivationToken(null);
                user.setActivationTokenExpiry(null);
                userRepository.save(user);
                System.out.println("✅ User activated: " + email);
                return true;
            }
            
            System.out.println("❌ User not found for activation: " + email);
            return false;
            
        } catch (Exception e) {
            System.out.println("❌ Activation failed: " + e.getMessage());
            return false;
        }
    }

    // UPDATED METHOD: Returns OTP in response for testing
    public Map<String, String> generateAndSendOtp(String email) {
        try {
            System.out.println("🔧 Generating OTP for: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is not activated");
            }
            
            // Use OtpService to generate OTP and return it for testing
            Map<String, String> otpResponse = otpService.generateOtpWithResponse(user);
            String otp = otpResponse.get("otp");
            
            // Store encoded OTP in database as backup
            user.setOtp(passwordEncoder.encode(otp));
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            
            // Try to send OTP email, but don't fail if email doesn't work
            try {
                System.out.println("📧 Attempting to send OTP email...");
                emailService.sendOtpEmail(user.getEmail(), otp);
                System.out.println("✅ OTP email sent successfully");
            } catch (Exception emailException) {
                System.out.println("⚠️ Email sending failed, but continuing: " + emailException.getMessage());
                // Don't throw the exception - just log it and continue
            }
            
            // Return OTP in response for testing
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP generated successfully");
            response.put("otp", otp); // Include OTP in response for testing
            response.put("email", email);
            response.put("note", "Check console for OTP. Use this for testing.");
            
            System.out.println("🎯 TESTING: OTP for " + email + " is: " + otp);
            System.out.println("💡 Display this OTP to the user for testing purposes");
            
            return response;
            
        } catch (Exception e) {
            System.out.println("❌ OTP generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage());
        }
    }
    
    // UPDATED METHOD: Generate OTP without attempting to send email
    public Map<String, String> generateOtpWithoutEmail(String email) {
        try {
            System.out.println("🔧 Generating OTP without email for: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is not activated");
            }
            
            // Use OtpService to generate OTP
            Map<String, String> otpResponse = otpService.generateOtpWithResponse(user);
            String otp = otpResponse.get("otp");
            
            // Store encoded OTP
            user.setOtp(passwordEncoder.encode(otp));
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            
            // Return OTP in response for testing
            Map<String, String> response = new HashMap<>();
            response.put("message", "OTP generated successfully (no email sent)");
            response.put("otp", otp); // Include OTP in response for testing
            response.put("email", email);
            response.put("note", "This OTP is for testing purposes only");
            
            System.out.println("🎯 OTP for testing: " + otp);
            
            return response;
            
        } catch (Exception e) {
            System.out.println("❌ OTP generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage());
        }
    }
    
    // NEW METHOD: Quick OTP generation for testing (uses in-memory storage only)
    public Map<String, String> generateQuickOtp(String email) {
        try {
            System.out.println("🔧 Generating quick OTP for testing: " + email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is not activated");
            }
            
            // Use OtpService for quick in-memory OTP
            String otp = otpService.generateOtpForTesting(user);
            
            // Return OTP immediately for testing
            Map<String, String> response = new HashMap<>();
            response.put("message", "Quick OTP generated for testing");
            response.put("otp", otp);
            response.put("email", email);
            response.put("validity", "10 minutes");
            response.put("note", "Use this OTP for immediate testing");
            
            System.out.println("🚀 QUICK OTP for " + email + ": " + otp);
            
            return response;
            
        } catch (Exception e) {
            System.out.println("❌ Quick OTP generation failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate OTP: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String otp) {
        try {
            System.out.println("🔧 Verifying OTP for: " + email);
            
            // First try in-memory OTP validation
            boolean inMemoryValid = otpService.validateOtp(email, otp);
            if (inMemoryValid) {
                System.out.println("✅ OTP verified successfully (in-memory) for: " + email);
                return true;
            }
            
            // Fallback to database OTP validation
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is not activated");
            }
            
            boolean dbValid = user.isOtpValid() && passwordEncoder.matches(otp, user.getOtp());
            
            if (dbValid) {
                user.setOtp(null);
                user.setOtpExpiry(null);
                userRepository.save(user);
                System.out.println("✅ OTP verified successfully (database) for: " + email);
            } else {
                System.out.println("❌ OTP verification failed for: " + email);
            }
            
            return dbValid;
            
        } catch (Exception e) {
            System.out.println("❌ OTP verification error: " + e.getMessage());
            return false;
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    // Add the missing method for PasswordResetController
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        System.out.println("✅ Password updated for: " + user.getEmail());
    }
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
    
    @Transactional
    public void fixExistingUsers() {
        try {
            System.out.println("🔧 Fixing existing users with null is_active field...");
            List<User> usersWithNullActive = userRepository.findAll().stream()
                    .filter(user -> user.getIsActive() == null)
                    .collect(Collectors.toList());
            
            for (User user : usersWithNullActive) {
                System.out.println("🔄 Fixing user: " + user.getEmail() + " - Setting isActive to true");
                user.setIsActive(true);
                userRepository.save(user);
            }
            
            System.out.println("✅ Fixed " + usersWithNullActive.size() + " users");
        } catch (Exception e) {
            System.out.println("❌ Error fixing users: " + e.getMessage());
        }
    }
    
    @Transactional
    public void updateUserPassword(String email, String newPassword) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String encodedPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedPassword);
                userRepository.save(user);
                System.out.println("✅ Password updated to BCrypt for: " + email);
            } else {
                System.out.println("❌ User not found: " + email);
            }
        } catch (Exception e) {
            System.out.println("❌ Error updating password: " + e.getMessage());
            throw new RuntimeException("Failed to update password: " + e.getMessage());
        }
    }
    
    public void debugPassword(String email, String attemptedPassword) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                System.out.println("🔍 PASSWORD DEBUG for: " + email);
                System.out.println("🔑 Stored password hash: " + user.getPassword());
                System.out.println("🔑 Password length: " + (user.getPassword() != null ? user.getPassword().length() : "null"));
                
                boolean matches = passwordEncoder.matches(attemptedPassword, user.getPassword());
                System.out.println("🔑 Password matches BCrypt: " + matches);
                
                // Also check if it's plain text (for existing users)
                if (user.getPassword() != null && user.getPassword().equals(attemptedPassword)) {
                    System.out.println("🔑 Password matches plain text: true");
                } else {
                    System.out.println("🔑 Password matches plain text: false");
                }
            } else {
                System.out.println("❌ User not found: " + email);
            }
        } catch (Exception e) {
            System.out.println("❌ Debug error: " + e.getMessage());
        }
    }
}