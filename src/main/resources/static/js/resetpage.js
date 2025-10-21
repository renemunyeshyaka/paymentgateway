document.addEventListener('DOMContentLoaded', function() {
            // DOM Elements
            const steps = document.querySelectorAll('.step');
            const stepContainers = document.querySelectorAll('.reset-step');
            const emailInput = document.getElementById('email');
            const userEmailSpan = document.getElementById('userEmail');
            const otpInputs = document.querySelectorAll('.otp-input');
            const newPasswordInput = document.getElementById('newPassword');
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const strengthFill = document.getElementById('strengthFill');
            const strengthText = document.getElementById('strengthText');
            const passwordMatchError = document.getElementById('passwordMatchError');
            const otpError = document.getElementById('otpError');
            const countdownElement = document.getElementById('countdown');
            
            // Buttons
            const sendOtpBtn = document.getElementById('sendOtpBtn');
            const verifyOtpBtn = document.getElementById('verifyOtpBtn');
            const updatePasswordBtn = document.getElementById('updatePasswordBtn');
            const backToStep1Btn = document.getElementById('backToStep1');
            const backToStep2Btn = document.getElementById('backToStep2');
            const resendOtpLink = document.getElementById('resendOtp');
            
            // State
            let currentStep = 1;
            let countdownInterval;
            let countdownTime = 60;
            
            // Initialize
            updateStepIndicator();
            
            // Event Listeners
            sendOtpBtn.addEventListener('click', sendOtp);
            verifyOtpBtn.addEventListener('click', verifyOtp);
            updatePasswordBtn.addEventListener('click', updatePassword);
            backToStep1Btn.addEventListener('click', () => navigateToStep(1));
            backToStep2Btn.addEventListener('click', () => navigateToStep(2));
            resendOtpLink.addEventListener('click', resendOtp);
            
            // OTP Input Navigation
            otpInputs.forEach((input, index) => {
                input.addEventListener('input', (e) => {
                    // Auto-focus next input
                    if (e.target.value.length === 1 && index < otpInputs.length - 1) {
                        otpInputs[index + 1].focus();
                    }
                    
                    // Clear error when user types
                    otpError.style.display = 'none';
                });
                
                input.addEventListener('keydown', (e) => {
                    // Handle backspace
                    if (e.key === 'Backspace' && !e.target.value && index > 0) {
                        otpInputs[index - 1].focus();
                    }
                });
            });
            
            // Password Strength Checker
            newPasswordInput.addEventListener('input', checkPasswordStrength);
            confirmPasswordInput.addEventListener('input', checkPasswordMatch);
            
            // Functions
            function sendOtp() {
                const email = emailInput.value.trim();
                
                if (!validateEmail(email)) {
                    alert('Please enter a valid email address.');
                    return;
                }
                
                // In a real application, you would make an API call here
                // For demo purposes, we'll simulate the process
                
                // Update UI
                userEmailSpan.textContent = email;
                navigateToStep(2);
                startCountdown();
            }
            
            function verifyOtp() {
                const otp = Array.from(otpInputs).map(input => input.value).join('');
                
                if (otp.length !== 6) {
                    otpError.textContent = 'Please enter the complete 6-digit code.';
                    otpError.style.display = 'block';
                    return;
                }
                
                // In a real application, you would verify the OTP with the server
                // For demo purposes, we'll accept any 6-digit code
                
                navigateToStep(3);
            }
            
            function updatePassword() {
                const newPassword = newPasswordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                
                if (newPassword !== confirmPassword) {
                    passwordMatchError.style.display = 'block';
                    return;
                }
                
                if (newPassword.length < 8) {
                    alert('Password must be at least 8 characters long.');
                    return;
                }
                
                // In a real application, you would send the new password to the server
                // For demo purposes, we'll simulate the process
                
                navigateToStep(4);
            }
            
            function resendOtp(e) {
                e.preventDefault();
                
                // Reset countdown
                countdownTime = 60;
                startCountdown();
                
                // In a real application, you would request a new OTP from the server
                alert('A new verification code has been sent to your email.');
            }
            
            function navigateToStep(step) {
                // Hide current step
                document.getElementById(`step${currentStep}`).classList.remove('active');
                
                // Update current step
                currentStep = step;
                
                // Show new step
                document.getElementById(`step${currentStep}`).classList.add('active');
                
                // Update step indicator
                updateStepIndicator();
            }
            
            function updateStepIndicator() {
                steps.forEach(step => {
                    const stepNumber = parseInt(step.getAttribute('data-step'));
                    
                    step.classList.remove('active', 'completed');
                    
                    if (stepNumber === currentStep) {
                        step.classList.add('active');
                    } else if (stepNumber < currentStep) {
                        step.classList.add('completed');
                    }
                });
            }
            
            function startCountdown() {
                // Clear existing interval
                if (countdownInterval) {
                    clearInterval(countdownInterval);
                }
                
                // Disable resend link
                resendOtpLink.style.pointerEvents = 'none';
                resendOtpLink.style.opacity = '0.5';
                
                countdownInterval = setInterval(() => {
                    countdownTime--;
                    countdownElement.textContent = `(${countdownTime}s)`;
                    
                    if (countdownTime <= 0) {
                        clearInterval(countdownInterval);
                        countdownElement.textContent = '';
                        resendOtpLink.style.pointerEvents = 'auto';
                        resendOtpLink.style.opacity = '1';
                    }
                }, 1000);
            }
            
            function checkPasswordStrength() {
                const password = newPasswordInput.value;
                let strength = 0;
                let text = '';
                let color = '';
                
                if (password.length >= 8) strength++;
                if (/[A-Z]/.test(password)) strength++;
                if (/[a-z]/.test(password)) strength++;
                if (/[0-9]/.test(password)) strength++;
                if (/[^A-Za-z0-9]/.test(password)) strength++;
                
                switch(strength) {
                    case 0:
                    case 1:
                        text = 'Very Weak';
                        color = '#ef4444';
                        break;
                    case 2:
                        text = 'Weak';
                        color = '#f59e0b';
                        break;
                    case 3:
                        text = 'Medium';
                        color = '#eab308';
                        break;
                    case 4:
                        text = 'Strong';
                        color = '#84cc16';
                        break;
                    case 5:
                        text = 'Very Strong';
                        color = '#10b981';
                        break;
                }
                
                strengthFill.style.width = `${strength * 20}%`;
                strengthFill.style.backgroundColor = color;
                strengthText.textContent = text;
                strengthText.style.color = color;
            }
            
            function checkPasswordMatch() {
                const password = newPasswordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                
                if (confirmPassword && password !== confirmPassword) {
                    passwordMatchError.style.display = 'block';
                } else {
                    passwordMatchError.style.display = 'none';
                }
            }
            
            function validateEmail(email) {
                const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                return re.test(email);
            }
        });