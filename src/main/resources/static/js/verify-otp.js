// verify-otp.js - Complete fixed version
class OTPVerification {
    constructor() {
        this.BASE_URL = 'https://kcoders.onrender.com/api/auth';
        this.timer = null;
        this.timeLeft = 60;
        this.initialize();
    }
    
    initialize() {
        // Display email from URL parameter
        const urlParams = new URLSearchParams(window.location.search);
        const email = urlParams.get('email');
        if (email) {
            document.getElementById('userEmail').textContent = email;
            sessionStorage.setItem('pendingLoginEmail', email);
        } else {
            // Try to get email from session storage as fallback
            const storedEmail = sessionStorage.getItem('pendingLoginEmail');
            if (storedEmail) {
                document.getElementById('userEmail').textContent = storedEmail;
            }
        }
        
        // Initialize OTP inputs
        this.initializeOTPInputs();
        
        // Form submission
        document.getElementById('verifyOtpForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.verifyOTP();
        });
        
        // Resend OTP
        document.getElementById('resendOtp').addEventListener('click', (e) => {
            e.preventDefault();
            this.resendOTP();
        });
        
        // Start countdown
        this.startCountdown();
    }
    
    initializeOTPInputs() {
        const inputs = document.querySelectorAll('.otp-input');
        
        inputs.forEach((input, index) => {
            // Handle input
            input.addEventListener('input', (e) => {
                const value = e.target.value;
                
                if (value && /[0-9]/.test(value)) {
                    e.target.value = value;
                    e.target.classList.add('filled');
                    
                    // Auto-focus next input
                    if (index < inputs.length - 1) {
                        inputs[index + 1].focus();
                    }
                } else {
                    e.target.value = '';
                    e.target.classList.remove('filled');
                }
                
                // Clear error when user types
                this.hideError();
            });
            
            // Handle backspace
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && !e.target.value && index > 0) {
                    inputs[index - 1].focus();
                }
            });
            
            // Handle paste
            input.addEventListener('paste', (e) => {
                e.preventDefault();
                const pasteData = e.clipboardData.getData('text');
                if (/^\d{6}$/.test(pasteData)) {
                    for (let i = 0; i < 6; i++) {
                        if (inputs[i]) {
                            inputs[i].value = pasteData[i];
                            inputs[i].classList.add('filled');
                        }
                    }
                    inputs[5].focus();
                }
            });
        });
    }
    
    getOTPCode() {
        const inputs = document.querySelectorAll('.otp-input');
        let otp = '';
        inputs.forEach(input => {
            otp += input.value;
        });
        return otp;
    }
    
    getCurrentEmail() {
        const urlParams = new URLSearchParams(window.location.search);
        const email = urlParams.get('email');
        if (email) return email;
        
        // Fallback to session storage
        return sessionStorage.getItem('pendingLoginEmail');
    }
    
    async verifyOTP() {
        const email = this.getCurrentEmail();
        const otp = this.getOTPCode();
        
        if (!email) {
            this.showError('Email not found. Please return to login page.');
            return;
        }
        
        if (otp.length !== 6) {
            this.showError('Please enter the complete 6-digit code');
            return;
        }
        
        this.setLoading(true);
        
        try {
            console.log('🔐 Verifying OTP for:', email);
            console.log('📤 OTP code:', otp);
            
            // Try multiple OTP verification endpoints
            const endpoints = [
                '/verify-otp-simple',
                '/verify-otp', 
                '/validate-otp'
            ];
            
            let response = null;
            let data = null;
            
            for (let endpoint of endpoints) {
                try {
                    console.log(`🔄 Trying endpoint: ${endpoint}`);
                    response = await fetch(`${this.BASE_URL}${endpoint}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            email: email,
                            otp: otp
                        })
                    });
                    
                    console.log(`📨 ${endpoint} response status:`, response.status);
                    
                    if (response.ok) {
                        break; // Stop if we found a working endpoint
                    }
                } catch (endpointError) {
                    console.log(`❌ Endpoint ${endpoint} failed:`, endpointError.message);
                    continue;
                }
            }
            
            if (!response) {
                throw new Error('All OTP verification endpoints failed');
            }
            
            const responseText = await response.text();
            console.log('📨 Raw OTP response:', responseText);
            
            try {
                data = JSON.parse(responseText);
            } catch (parseError) {
                console.error('❌ JSON parse error:', parseError);
                throw new Error('Server returned invalid response');
            }
            
            if (response.ok) {
                console.log('✅ OTP verification successful:', data);
                
                // Store authentication data
                if (data.token) {
                    localStorage.setItem('authToken', data.token);
                    console.log('✅ JWT token stored');
                }
                if (data.user) {
                    localStorage.setItem('currentUser', JSON.stringify(data.user));
                    console.log('✅ User data stored:', data.user.email);
                }
                
                // Clear pending login email
                sessionStorage.removeItem('pendingLoginEmail');
                
                this.showSuccess('Verification successful! Redirecting to dashboard...');
                
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 2000);
                
            } else {
                console.log('❌ OTP verification failed:', data);
                const errorMessage = data.error || data.message || 'Invalid OTP code';
                this.showError(errorMessage);
                
                // Clear OTP inputs on failure
                this.clearOTPInputs();
            }
        } catch (error) {
            console.error('❌ OTP verification error:', error);
            this.showError('Verification failed: ' + error.message);
        } finally {
            this.setLoading(false);
        }
    }
    
    async resendOTP() {
        const email = this.getCurrentEmail();
        
        if (!email) {
            this.showError('Email not found. Please return to login page.');
            return;
        }
        
        if (this.timeLeft > 0) {
            this.showError(`Please wait ${this.timeLeft}s before resending`);
            return;
        }
        
        this.setLoading(true, 'resend');
        
        try {
            console.log('📧 Resending OTP for:', email);
            
            // Try multiple resend endpoints
            const endpoints = [
                '/generate-otp',
                '/resend-otp',
                '/generate-and-send-otp'
            ];
            
            let response = null;
            let data = null;
            
            for (let endpoint of endpoints) {
                try {
                    console.log(`🔄 Trying resend endpoint: ${endpoint}`);
                    response = await fetch(`${this.BASE_URL}${endpoint}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                        },
                        body: JSON.stringify({
                            email: email
                        })
                    });
                    
                    console.log(`📨 ${endpoint} response status:`, response.status);
                    
                    if (response.ok) {
                        break; // Stop if we found a working endpoint
                    }
                } catch (endpointError) {
                    console.log(`❌ Endpoint ${endpoint} failed:`, endpointError.message);
                    continue;
                }
            }
            
            if (!response) {
                throw new Error('All OTP resend endpoints failed');
            }
            
            const responseText = await response.text();
            console.log('📨 Raw resend response:', responseText);
            
            try {
                data = JSON.parse(responseText);
            } catch (parseError) {
                console.error('❌ JSON parse error:', parseError);
                throw new Error('Server returned invalid response');
            }
            
            if (response.ok) {
                console.log('✅ OTP resent successfully:', data);
                
                let successMessage = 'New OTP sent to your email';
                
                // If OTP is returned in response, show it for testing
                if (data.otp) {
                    console.log('🎯 NEW OTP for testing:', data.otp);
                    successMessage += `. Test OTP: ${data.otp}`;
                }
                
                this.showSuccess(successMessage);
                this.startCountdown();
                
                // Clear previous OTP inputs
                this.clearOTPInputs();
                
            } else {
                console.log('❌ Resend OTP failed:', data);
                const errorMessage = data.error || data.message || 'Failed to resend OTP';
                this.showError(errorMessage);
            }
        } catch (error) {
            console.error('❌ Resend OTP error:', error);
            this.showError('Failed to resend OTP: ' + error.message);
        } finally {
            this.setLoading(false, 'resend');
        }
    }
    
    startCountdown() {
        this.timeLeft = 60;
        this.updateCountdown();
        
        if (this.timer) {
            clearInterval(this.timer);
        }
        
        this.timer = setInterval(() => {
            this.timeLeft--;
            this.updateCountdown();
            
            if (this.timeLeft <= 0) {
                clearInterval(this.timer);
            }
        }, 1000);
    }
    
    updateCountdown() {
        const countdownElement = document.getElementById('countdown');
        const resendButton = document.getElementById('resendOtp');
        
        if (this.timeLeft > 0) {
            countdownElement.textContent = `(${this.timeLeft}s)`;
            resendButton.style.opacity = '0.6';
            resendButton.style.cursor = 'not-allowed';
        } else {
            countdownElement.textContent = '';
            resendButton.style.opacity = '1';
            resendButton.style.cursor = 'pointer';
        }
    }
    
    setLoading(loading, type = 'verify') {
        const verifyBtn = document.getElementById('verifyBtn');
        const resendBtn = document.getElementById('resendOtp');
        
        if (type === 'verify') {
            const btnText = verifyBtn.querySelector('.btn-text');
            const btnLoading = verifyBtn.querySelector('.btn-loading');
            
            if (loading) {
                verifyBtn.disabled = true;
                if (btnText) btnText.style.display = 'none';
                if (btnLoading) btnLoading.style.display = 'flex';
            } else {
                verifyBtn.disabled = false;
                if (btnText) btnText.style.display = 'block';
                if (btnLoading) btnLoading.style.display = 'none';
            }
        } else if (type === 'resend') {
            if (loading) {
                resendBtn.disabled = true;
                resendBtn.innerHTML = 'Sending...';
            } else {
                resendBtn.disabled = false;
                resendBtn.innerHTML = 'Resend OTP <span id="countdown"></span>';
                this.updateCountdown();
            }
        }
    }
    
    clearOTPInputs() {
        const inputs = document.querySelectorAll('.otp-input');
        inputs.forEach(input => {
            input.value = '';
            input.classList.remove('filled');
        });
        // Focus first input
        if (inputs[0]) inputs[0].focus();
    }
    
    showError(message) {
        const errorElement = document.getElementById('otpError');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.classList.add('show');
            
            // Auto-hide error after 5 seconds
            setTimeout(() => {
                this.hideError();
            }, 5000);
        } else {
            // Fallback alert
            alert('Error: ' + message);
        }
    }
    
    hideError() {
        const errorElement = document.getElementById('otpError');
        if (errorElement) {
            errorElement.classList.remove('show');
        }
    }
    
    showSuccess(message) {
        // Create success message element
        const successElement = document.createElement('div');
        successElement.className = 'alert-message success';
        successElement.innerHTML = `
            <i class="fas fa-check-circle"></i>
            <span>${message}</span>
        `;
        
        // Insert at top of form
        const form = document.getElementById('verifyOtpForm');
        if (form) {
            form.insertBefore(successElement, form.firstChild);
            
            // Auto-remove after 3 seconds
            setTimeout(() => {
                successElement.remove();
            }, 3000);
        } else {
            alert(message);
        }
    }
}

// Initialize OTP verification when page loads
document.addEventListener('DOMContentLoaded', () => {
    new OTPVerification();
});