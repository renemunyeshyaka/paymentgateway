// PasswordResetRequest.java
package com.paymentgateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {
    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}