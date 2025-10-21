// PasswordResetService.java
package com.kcoders.service;

import com.kcoders.entity.PasswordResetToken;
import com.kcoders.entity.User;
import com.kcoders.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserService userService;
    
    public PasswordResetToken createPasswordResetToken(User user) {
        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);
        
        String token = generateToken();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        return tokenRepository.save(resetToken);
    }
    
    public Optional<PasswordResetToken> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        if (resetToken.isPresent() && resetToken.get().isValid()) {
            return resetToken;
        }
        return Optional.empty();
    }
    
    public Optional<PasswordResetToken> getTokenByUserEmail(String email) {
        return tokenRepository.findByUserEmail(email);
    }
    
    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }
    
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupExpiredTokens() {
        tokenRepository.deleteAllExpiredSince(LocalDateTime.now());
    }
}