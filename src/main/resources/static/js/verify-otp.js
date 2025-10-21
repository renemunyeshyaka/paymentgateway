class OTPVerification {
    constructor() {
        this.BASE_URL = 'http://localhost:8080/api/auth';
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
    
    async verifyOTP() {
        const urlParams = new URLSearchParams(window.location.search);
        const email = urlParams.get('email');
        const otp = this.getOTPCode();
        
        if (otp.length !== 6) {
            this.showError('Please enter the complete 6-digit code');
            return;
        }
        
        this.setLoading(true);
        
        try {
            const response = await fetch(`${this.BASE_URL}/verify-otp`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email,
                    otp: otp
                })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                // Store user data
                if (data.user) {
                    localStorage.setItem('currentUser', JSON.stringify(data.user));
                }
                
                // Show success and redirect
                this.showSuccess('Verification successful! Redirecting to dashboard...');
                
                setTimeout(() => {
                    window.location.href = 'dashboard.html';
                }, 1500);
                
            } else {
                this.showError(data.error || 'Invalid OTP code');
            }
        } catch (error) {
            console.error('OTP verification error:', error);
            this.showError('Verification failed. Please check your connection and try again.');
        } finally {
            this.setLoading(false);
        }
    }
    
    async resendOTP() {
        const urlParams = new URLSearchParams(window.location.search);
        const email = urlParams.get('email');
        
        if (this.timeLeft > 0) {
            this.showError(`Please wait ${this.timeLeft}s before resending`);
            return;
        }
        
        this.setLoading(true, 'resend');
        
        try {
            const response = await fetch(`${this.BASE_URL}/resend-otp`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: email
                })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                this.showSuccess('New OTP sent to your email');
                this.startCountdown();
            } else {
                this.showError(data.error || 'Failed to resend OTP');
            }
        } catch (error) {
            console.error('Resend OTP error:', error);
            this.showError('Failed to resend OTP. Please try again.');
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
        if (this.timeLeft > 0) {
            countdownElement.textContent = `(${this.timeLeft}s)`;
        } else {
            countdownElement.textContent = '';
        }
    }
    
    setLoading(loading, type = 'verify') {
        const verifyBtn = document.getElementById('verifyBtn');
        const btnText = verifyBtn.querySelector('.btn-text');
        const btnLoading = verifyBtn.querySelector('.btn-loading');
        
        if (loading) {
            verifyBtn.disabled = true;
            btnText.style.display = 'none';
            btnLoading.style.display = 'flex';
        } else {
            verifyBtn.disabled = false;
            btnText.style.display = 'block';
            btnLoading.style.display = 'none';
        }
    }
    
    showError(message) {
        const errorElement = document.getElementById('otpError');
        errorElement.textContent = message;
        errorElement.classList.add('show');
        
        // Auto-hide error after 5 seconds
        setTimeout(() => {
            this.hideError();
        }, 5000);
    }
    
    hideError() {
        const errorElement = document.getElementById('otpError');
        errorElement.classList.remove('show');
    }
    
    showSuccess(message) {
        alert(message); // You can replace this with a nicer toast notification
    }
}

// Initialize OTP verification when page loads
document.addEventListener('DOMContentLoaded', () => {
    new OTPVerification();
});