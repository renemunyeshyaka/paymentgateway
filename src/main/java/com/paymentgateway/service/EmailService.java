// EmailService.java - Fixed version
package com.paymentgateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    public boolean isEmailConfigured() {
        boolean configured = mailSender != null;
        logger.info("📧 Email service configured: {}", configured);
        return configured;
    }
    
    // Method for activation email - This one works
    @Async
    public void sendActivationEmail(String toEmail, String userName) {
        logger.info("🎯 SENDING ACTIVATION EMAIL to: {}, User: {}", toEmail, userName);
        
        if (!isEmailConfigured()) {
            logger.warn("📧 Email service not configured. Would send activation email to: {}", toEmail);
            System.out.println("=== ACTIVATION EMAIL (DEVELOPMENT) ===");
            System.out.println("To: " + toEmail);
            System.out.println("User: " + userName);
            System.out.println("Activation Link: http://localhost:10000/api/auth/activate?email=" + toEmail);
            //System.out.println("Activation Link: http://dpg-d3r70femcj7s73bmmfe0-a.oregon-postgres.render.com:10000/api/auth/activate?email=" + toEmail);
            System.out.println("=======================================");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Kcoders - Activate Your Account");
            
            // Use string concatenation instead of .formatted() to avoid the formatting error
            String emailContent = buildActivationEmailContent(userName, toEmail);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            logger.info("✅ Activation email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send activation email to: {}", toEmail, e);
            System.out.println("❌ Activation email sending failed for: " + toEmail);
        }
    }
    
    // Method for OTP email - Fixed version
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        logger.info("🎯 SENDING OTP EMAIL to: {}, OTP: {}", toEmail, otpCode);
        
        if (!isEmailConfigured()) {
            logger.warn("📧 Email service not configured. OTP for {}: {}", toEmail, otpCode);
            System.out.println("=== OTP EMAIL (DEVELOPMENT) ===");
            System.out.println("To: " + toEmail);
            System.out.println("OTP Code: " + otpCode);
            System.out.println("Use this OTP to login: " + otpCode);
            System.out.println("===============================");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Kcoders - Your Login Verification Code");
            
            // Use the fixed OTP email content builder
            String emailContent = buildOtpEmailContent(otpCode);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            logger.info("✅ OTP email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send OTP email to: {}", toEmail, e);
            System.out.println("❌ OTP email sending failed for: " + toEmail);
            // Don't throw exception - just log it
        }
    }
    
    // Alternative OTP email method with username
    @Async
    public void sendOtpEmail(String toEmail, String otpCode, String userName) {
        logger.info("🎯 SENDING OTP EMAIL to: {}, OTP: {}, User: {}", toEmail, otpCode, userName);
        // Call the 2-parameter version
        sendOtpEmail(toEmail, otpCode);
    }

    // Fixed email content builders without .formatted()
    private String buildActivationEmailContent(String userName, String email) {
        String activationLink = "http://localhost:10000/api/auth/activate?email=" + email;
        
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "    <meta charset=\"UTF-8\">" +
               "    <style>" +
               "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
               "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
               "        .activation-btn { display: inline-block; padding: 12px 24px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }" +
               "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
               "    </style>" +
               "</head>" +
               "<body>" +
               "    <div class=\"container\">" +
               "        <div class=\"header\">" +
               "            <h1>Kcoders</h1>" +
               "            <h2>Activate Your Account</h2>" +
               "        </div>" +
               "        <div class=\"content\">" +
               "            <p>Hello <strong>" + userName + "</strong>,</p>" +
               "            <p>Welcome to Kcoders! Thank you for registering with us.</p>" +
               "            <p>To activate your account and start using our services, please click the button below:</p>" +
               "            <div style=\"text-align: center;\">" +
               "                <a href=\"" + activationLink + "\" class=\"activation-btn\">Activate My Account</a>" +
               "            </div>" +
               "            <p>Or copy and paste this link in your browser:<br>" +
               "            <code>" + activationLink + "</code></p>" +
               "            <p>If you didn't create an account with us, please ignore this email.</p>" +
               "            <p>Best regards,<br>The Kcoders Team</p>" +
               "        </div>" +
               "        <div class=\"footer\">" +
               "            <p>&copy; 2025 Kcoders. All rights reserved.</p>" +
               "        </div>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }
    
    private String buildOtpEmailContent(String otpCode) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "    <meta charset=\"UTF-8\">" +
               "    <style>" +
               "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
               "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
               "        .otp-code { font-size: 32px; font-weight: bold; text-align: center; color: #667eea; margin: 20px 0; padding: 15px; background: white; border-radius: 5px; letter-spacing: 5px; }" +
               "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
               "    </style>" +
               "</head>" +
               "<body>" +
               "    <div class=\"container\">" +
               "        <div class=\"header\">" +
               "            <h1>Kcoders</h1>" +
               "            <h2>Login Verification Code</h2>" +
               "        </div>" +
               "        <div class=\"content\">" +
               "            <p>Hello,</p>" +
               "            <p>Your verification code for Kcoders login is:</p>" +
               "            <div class=\"otp-code\">" + otpCode + "</div>" +
               "            <p>This code will expire in 10 minutes. If you didn't request this login, please ignore this email.</p>" +
               "            <p>Best regards,<br>The Kcoders Team</p>" +
               "        </div>" +
               "        <div class=\"footer\">" +
               "            <p>&copy; 2025 Kcoders. All rights reserved.</p>" +
               "        </div>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }

    // Other email methods (password reset, etc.)
    @Async
    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        logger.info("🎯 SENDING PASSWORD RESET EMAIL to: {}, OTP: {}", toEmail, otpCode);
        
        if (!isEmailConfigured()) {
            logger.warn("📧 Email service not configured. Password reset OTP for {}: {}", toEmail, otpCode);
            System.out.println("=== PASSWORD RESET EMAIL (DEVELOPMENT) ===");
            System.out.println("To: " + toEmail);
            System.out.println("Reset OTP: " + otpCode);
            System.out.println("===============================");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Kcoders - Password Reset Verification Code");
            helper.setText(buildPasswordResetEmailContent(otpCode), true);
            
            mailSender.send(message);
            logger.info("✅ Password reset email sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("❌ Failed to send password reset email to: {}", toEmail, e);
        }
    }
    
    private String buildPasswordResetEmailContent(String otpCode) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "    <meta charset=\"UTF-8\">" +
               "    <style>" +
               "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
               "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
               "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
               "        .otp-code { font-size: 32px; font-weight: bold; text-align: center; color: #667eea; margin: 20px 0; padding: 15px; background: white; border-radius: 5px; letter-spacing: 5px; }" +
               "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
               "    </style>" +
               "</head>" +
               "<body>" +
               "    <div class=\"container\">" +
               "        <div class=\"header\">" +
               "            <h1>Kcoders</h1>" +
               "            <h2>Password Reset Request</h2>" +
               "        </div>" +
               "        <div class=\"content\">" +
               "            <p>Hello,</p>" +
               "            <p>You have requested to reset your password for your Kcoders account. Use the verification code below to proceed:</p>" +
               "            <div class=\"otp-code\">" + otpCode + "</div>" +
               "            <p>This code will expire in 10 minutes. If you didn't request this reset, please ignore this email.</p>" +
               "            <p>Best regards,<br>The Kcoders Team</p>" +
               "        </div>" +
               "        <div class=\"footer\">" +
               "            <p>&copy; 2025 Kcoders. All rights reserved.</p>" +
               "        </div>" +
               "    </div>" +
               "</body>" +
               "</html>";
    }
}