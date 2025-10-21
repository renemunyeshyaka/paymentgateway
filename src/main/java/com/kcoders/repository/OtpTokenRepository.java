// OtpTokenRepository.java
package com.kcoders.repository;

import com.kcoders.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    Optional<OtpToken> findByUserEmail(String userEmail);
    
    Optional<OtpToken> findByUserEmailAndOtpAndUsedFalse(String userEmail, String otp);
    
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteByExpiresAtBefore(LocalDateTime now);
}