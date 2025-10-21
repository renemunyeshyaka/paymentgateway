// Created after
// public/js/reset.js
class PasswordReset {
    constructor() {
        this.currentStep = 1;
        this.userEmail = '';
        this.resetToken = '';
        this.countdownInterval = null;
        
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Step 1: Request Reset
        document.getElementById('sendOtpBtn').addEventListener('click', () => this.requestReset());
        
        // Step 2: Verify OTP
        document.getElementById('backToStep1').addEventListener('click', () => this.goToStep(1));
        document.getElementById('verifyOtpBtn').addEventListener('click', () => this.verifyOtp());
        document.getElementById('resendOtp').addEventListener('click', (e) => {
            e.preventDefault();
            this.resendOtp();
        });
        
        // Step 3: New Password
        document.getElementById('backToStep2').addEventListener('click', () => this.goToStep(2));
        document.getElementById('updatePasswordBtn').addEventListener('click', () => this.updatePassword());
        
        // OTP Input handling
        this.setupOtpInputs();
        
        // Password strength checking
        this.setupPasswordStrength();
    }

    async requestReset() {
        const email = document.getElementById('email').value.trim();
        
        if (!this.validateEmail(email)) {
            this.showError('Please enter a valid email address');
            return;
        }

        this.userEmail = email;
        
        try {
            const response = await fetch('/api/auth/password-reset/request', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: email })
            });

            const data = await response.json();

            if (response.ok) {
                document.getElementById('userEmail').textContent = email;
                this.startCountdown();
                this.goToStep(2);
            } else {
                this.showError(data.message || 'Failed to send verification code');
            }
        } catch (error) {
            this.showError('Network error. Please try again.');
        }
    }

    async verifyOtp() {
        const otp = this.getOtpValue();
        
        if (otp.length !== 6) {
            this.showOtpError('Please enter the complete verification code');
            return;
        }

        try {
            const response = await fetch('/api/auth/password-reset/verify-otp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    email: this.userEmail, 
                    otp: otp 
                })
            });

            const data = await response.json();

            if (response.ok) {
                this.resetToken = data.data.token;
                this.hideOtpError();
                this.goToStep(3);
            } else {
                this.showOtpError(data.message || 'Invalid verification code');
            }
        } catch (error) {
            this.showOtpError('Network error. Please try again.');
        }
    }

    async updatePassword() {
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;

        if (newPassword.length < 8) {
            this.showPasswordError('Password must be at least 8 characters long');
            return;
        }

        if (newPassword !== confirmPassword) {
            this.showPasswordError('Passwords do not match');
            return;
        }

        try {
            const response = await fetch('/api/auth/password-reset/reset', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    email: this.userEmail,
                    token: this.resetToken,
                    newPassword: newPassword,
                    confirmPassword: confirmPassword
                })
            });

            const data = await response.json();

            if (response.ok) {
                this.hidePasswordError();
                this.goToStep(4);
            } else {
                this.showPasswordError(data.message || 'Failed to reset password');
            }
        } catch (error) {
            this.showPasswordError('Network error. Please try again.');
        }
    }

    async resendOtp() {
        const resendLink = document.getElementById('resendOtp');
        const countdown = document.getElementById('countdown');
        
        // Disable resend temporarily
        resendLink.style.pointerEvents = 'none';
        resendLink.style.opacity = '0.5';

        try {
            const response = await fetch('/api/auth/password-reset/resend-otp', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: this.userEmail })
            });

            const data = await response.json();

            if (response.ok) {
                this.startCountdown();
                this.showTemporaryMessage('Verification code sent!', countdown);
            } else {
                this.showError(data.message || 'Failed to resend code');
            }
        } catch (error) {
            this.showError('Network error. Please try again.');
        } finally {
            setTimeout(() => {
                resendLink.style.pointerEvents = 'auto';
                resendLink.style.opacity = '1';
            }, 5000);
        }
    }

    setupOtpInputs() {
        const otpInputs = document.querySelectorAll('.otp-input');
        
        otpInputs.forEach((input, index) => {
            input.addEventListener('input', (e) => {
                const value = e.target.value;
                
                if (value.length === 1 && index < otpInputs.length - 1) {
                    otpInputs[index + 1].focus();
                }
                
                this.hideOtpError();
            });
            
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Backspace' && !e.target.value && index > 0) {
                    otpInputs[index - 1].focus();
                }
            });
            
            input.addEventListener('paste', (e) => {
                e.preventDefault();
                const pasteData = e.clipboardData.getData('text').slice(0, 6);
                pasteData.split('').forEach((char, i) => {
                    if (otpInputs[i]) {
                        otpInputs[i].value = char;
                    }
                });
                if (pasteData.length === 6) {
                    otpInputs[5].focus();
                }
            });
        });
    }

    setupPasswordStrength() {
        const passwordInput = document.getElementById('newPassword');
        const strengthFill = document.getElementById('strengthFill');
        const strengthText = document.getElementById('strengthText');

        passwordInput.addEventListener('input', () => {
            const password = passwordInput.value;
            const strength = this.calculatePasswordStrength(password);
            
            strengthFill.style.width = strength.percentage + '%';
            strengthFill.style.backgroundColor = strength.color;
            strengthText.textContent = strength.text;
            strengthText.style.color = strength.color;
        });
    }

    calculatePasswordStrength(password) {
        let score = 0;
        
        if (password.length >= 8) score++;
        if (password.match(/[a-z]/)) score++;
        if (password.match(/[A-Z]/)) score++;
        if (password.match(/[0-9]/)) score++;
        if (password.match(/[^a-zA-Z0-9]/)) score++;
        
        const strengths = [
            { percentage: 20, color: '#ff4d4d', text: 'Very Weak' },
            { percentage: 40, color: '#ff8c1a', text: 'Weak' },
            { percentage: 60, color: '#ffcc00', text: 'Fair' },
            { percentage: 80, color: '#66cc66', text: 'Strong' },
            { percentage: 100, color: '#2ecc71', text: 'Very Strong' }
        ];
        
        return strengths[Math.min(score, strengths.length - 1)];
    }

    getOtpValue() {
        const otpInputs = document.querySelectorAll('.otp-input');
        return Array.from(otpInputs).map(input => input.value).join('');
    }

    goToStep(step) {
        // Hide all steps
        document.querySelectorAll('.reset-step').forEach(stepEl => {
            stepEl.classList.remove('active');
        });
        
        // Update step indicator
        document.querySelectorAll('.step').forEach(stepIndicator => {
            stepIndicator.classList.remove('active');
            if (parseInt(stepIndicator.dataset.step) <= step) {
                stepIndicator.classList.add('active');
            }
        });
        
        // Show current step
        document.getElementById(`step${step}`).classList.add('active');
        this.currentStep = step;
    }

    startCountdown() {
        let timeLeft = 60;
        const countdownElement = document.getElementById('countdown');
        const resendLink = document.getElementById('resendOtp');
        
        if (this.countdownInterval) {
            clearInterval(this.countdownInterval);
        }
        
        resendLink.style.pointerEvents = 'none';
        resendLink.style.opacity = '0.5';
        
        this.countdownInterval = setInterval(() => {
            countdownElement.textContent = `(${timeLeft}s)`;
            timeLeft--;
            
            if (timeLeft < 0) {
                clearInterval(this.countdownInterval);
                countdownElement.textContent = '';
                resendLink.style.pointerEvents = 'auto';
                resendLink.style.opacity = '1';
            }
        }, 1000);
    }

    validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    showError(message) {
        // You can implement a toast notification system here
        alert(message);
    }

    showOtpError(message) {
        const errorElement = document.getElementById('otpError');
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    hideOtpError() {
        document.getElementById('otpError').style.display = 'none';
    }

    showPasswordError(message) {
        const errorElement = document.getElementById('passwordMatchError');
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    hidePasswordError() {
        document.getElementById('passwordMatchError').style.display = 'none';
    }

    showTemporaryMessage(message, element) {
        const originalText = element.textContent;
        element.textContent = message;
        element.style.color = '#2ecc71';
        
        setTimeout(() => {
            element.textContent = originalText;
            element.style.color = '';
        }, 3000);
    }
}

// Initialize the password reset flow when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new PasswordReset();
});