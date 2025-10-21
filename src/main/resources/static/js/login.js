// login.js - Fixed to match your backend API response format
class AuthSystem {
    constructor() {
        this.currentForm = 'login';
        this.BASE_URL = 'http://localhost:8080/api/auth';
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Tab switching
        this.initializeTabSwitching();
        
        // Form submissions
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });

        document.getElementById('registerForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleRegistration();
        });

        // Real-time validation
        this.initializeRealTimeValidation();
    }

    initializeTabSwitching() {
        const tabs = document.querySelectorAll('.auth-tab');
        const forms = document.querySelectorAll('.auth-form');
        
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const targetTab = tab.getAttribute('data-tab');
                
                // Update active tab
                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                
                // Show corresponding form
                forms.forEach(form => {
                    form.classList.remove('active');
                    if (form.id === targetTab + 'Form') {
                        form.classList.add('active');
                    }
                });
                
                this.currentForm = targetTab;
                this.clearAllMessages();
            });
        });
    }

    initializeRealTimeValidation() {
        const formInputs = document.querySelectorAll('.form-control');
        
        formInputs.forEach(input => {
            input.addEventListener('blur', () => {
                this.validateField(input);
            });
            
            input.addEventListener('input', () => {
                // Remove error state when user starts typing
                if (input.parentElement.classList.contains('error')) {
                    input.parentElement.classList.remove('error');
                    const errorElement = input.parentElement.querySelector('.error-message');
                    if (errorElement) {
                        errorElement.textContent = '';
                    }
                }
            });
        });
    }

	// FIXED Login Handler - Proper OTP flow
	async handleLogin() {
	    const email = document.getElementById('loginEmail').value.trim();
	    const password = document.getElementById('loginPassword').value;
	    const captcha = document.querySelector('#loginForm input[name="captchaResponse"]').value.trim();
	    
	    console.log('🔐 Login attempt:', email);
	    
	    if (!this.validateLoginForm(email, password, captcha)) {
	        return;
	    }

	    this.setLoadingState('login', true);

	    try {
	        console.log('📤 Sending login request to:', `${this.BASE_URL}/login`);
	        
	        const loginData = {
	            email: email,
	            password: password
	        };
	        
	        console.log('📤 Login payload:', { ...loginData, password: '***' });

	        const response = await fetch(`${this.BASE_URL}/login`, {
	            method: 'POST',
	            headers: {
	                'Content-Type': 'application/json',
	            },
	            body: JSON.stringify(loginData)
	        });

	        console.log('📨 Response status:', response.status);
	        
	        const responseText = await response.text();
	        console.log('📨 Raw response:', responseText);
	        
	        let data;
	        try {
	            data = JSON.parse(responseText);
	        } catch (parseError) {
	            console.error('❌ JSON parse error:', parseError);
	            throw new Error('Server returned invalid response');
	        }
	        
	        // PROPERLY HANDLE THE OTP RESPONSE
	        if (response.ok) {
	            console.log('✅ Login API success:', data);
	            
	            // ALWAYS expect OTP flow based on your backend
	            if (data.message && data.message.includes('OTP sent')) {
	                console.log('📧 OTP required - proceeding to OTP verification');
	                
	                this.showSuccessMessage('OTP sent to your email! Please check your email to complete login.');
	                
	                // Store email for OTP verification
	                sessionStorage.setItem('pendingLoginEmail', email);
	                
	                // Redirect to OTP verification after short delay
	                setTimeout(() => {
	                    window.location.href = `verify-otp.html?email=${encodeURIComponent(email)}`;
	                }, 2000);
	                
	            } else {
	                // This should not happen with your current backend
	                console.warn('⚠️ Unexpected response - no OTP mentioned:', data);
	                this.showError('Unexpected server response. Please try again.', 'login');
	            }
	        } else {
	            // Handle error response
	            console.log('❌ Login failed:', data);
	            const errorMessage = data.error || data.message || `Login failed (${response.status})`;
	            this.showError(errorMessage, 'login');
	        }
	        
	    } catch (error) {
	        console.error('❌ Login process error:', error);
	        this.showError(this.getUserFriendlyErrorMessage(error), 'login');
	    } finally {
	        this.setLoadingState('login', false);
	    }
	}

    // Fixed Registration Handler - Matches your backend API
    async handleRegistration() {
        const firstName = document.getElementById('registerFirstName').value.trim();
        const lastName = document.getElementById('registerLastName').value.trim();
        const email = document.getElementById('registerEmail').value.trim();
        const password = document.getElementById('registerPassword').value;
        const confirmPassword = document.getElementById('registerConfirmPassword').value;
        const captcha = document.querySelector('#registerForm input[name="captchaResponse"]').value.trim().toLowerCase();
        
        console.log('👤 Registration attempt:', email);
        
        if (!this.validateRegistrationForm(firstName, lastName, email, password, confirmPassword, captcha)) {
            return;
        }

        this.setLoadingState('register', true);

        try {
            console.log('📤 Sending registration request to:', `${this.BASE_URL}/register`);
            
            const response = await fetch(`${this.BASE_URL}/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ 
                    firstName: firstName,
                    lastName: lastName,
                    email: email,
                    password: password
                    // Note: Your backend doesn't use confirmPassword, so we don't send it
                })
            });

            console.log('📨 Registration response status:', response.status);
            
            const responseText = await response.text();
            console.log('📨 Raw registration response:', responseText);
            
            let data;
            try {
                data = JSON.parse(responseText);
            } catch (parseError) {
                console.error('❌ JSON parse error:', parseError);
                throw new Error('Server returned invalid response');
            }

            // Handle response based on your backend structure
            if (response.ok) {
                console.log('✅ Registration successful:', data);
                
                const successMessage = data.message || 'Registration successful! Please check your email to activate your account.';
                this.showSuccessMessage(successMessage);
                
                // Switch to login tab after successful registration
                setTimeout(() => {
                    const loginTab = document.querySelector('[data-tab="login"]');
                    if (loginTab) loginTab.click();
                    document.getElementById('loginEmail').value = email;
                    document.getElementById('registerForm').reset();
                }, 3000);
            } else {
                console.log('❌ Registration failed:', data);
                const errorMessage = data.error || data.message || `Registration failed (${response.status})`;
                this.showError(errorMessage, 'register');
            }
        } catch (error) {
            console.error('❌ Registration error:', error);
            this.showError(this.getUserFriendlyErrorMessage(error), 'register');
        } finally {
            this.setLoadingState('register', false);
        }
    }

    // Validation Methods
    validateLoginForm(email, password, captcha) {
        let isValid = true;
        this.clearErrors('login');

        if (!email) {
            this.showFieldError('loginEmail', 'Email is required');
            isValid = false;
        } else if (!this.isValidEmail(email)) {
            this.showFieldError('loginEmail', 'Please enter a valid email address');
            isValid = false;
        }

        if (!password) {
            this.showFieldError('loginPassword', 'Password is required');
            isValid = false;
        } else if (password.length < 6) {
            this.showFieldError('loginPassword', 'Password must be at least 6 characters');
            isValid = false;
        }

        if (!captcha) {
            this.showFieldError('loginCaptchaError', 'CAPTCHA answer is required');
            isValid = false;
        } else if (captcha !== '8') {
            this.showFieldError('loginCaptchaError', 'Please enter the correct answer (8)');
            isValid = false;
        }

        return isValid;
    }

    validateRegistrationForm(firstName, lastName, email, password, confirmPassword, captcha) {
        let isValid = true;
        this.clearErrors('register');

        if (!firstName) {
            this.showFieldError('registerFirstName', 'First name is required');
            isValid = false;
        }

        if (!lastName) {
            this.showFieldError('registerLastName', 'Last name is required');
            isValid = false;
        }

        if (!email) {
            this.showFieldError('registerEmail', 'Email is required');
            isValid = false;
        } else if (!this.isValidEmail(email)) {
            this.showFieldError('registerEmail', 'Please enter a valid email address');
            isValid = false;
        }

        if (!password) {
            this.showFieldError('registerPassword', 'Password is required');
            isValid = false;
        } else if (password.length < 8) {
            this.showFieldError('registerPassword', 'Password must be at least 8 characters');
            isValid = false;
        }

        if (!confirmPassword) {
            this.showFieldError('registerConfirmPassword', 'Please confirm your password');
            isValid = false;
        } else if (password !== confirmPassword) {
            this.showFieldError('registerConfirmPassword', 'Passwords do not match');
            isValid = false;
        }

        if (!captcha) {
            this.showFieldError('registerCaptchaError', 'CAPTCHA answer is required');
            isValid = false;
        } else if (captcha !== 'paris') {
            this.showFieldError('registerCaptchaError', 'Please enter the correct answer (Paris)');
            isValid = false;
        }

        return isValid;
    }

    // Utility Methods
    isValidEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    showFieldError(fieldId, message) {
        if (fieldId.includes('CaptchaError')) {
            const errorElement = document.getElementById(fieldId);
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.style.display = 'block';
                const formGroup = errorElement.closest('.form-group');
                if (formGroup) {
                    formGroup.classList.add('error');
                }
            }
            return;
        }
        
        const field = document.getElementById(fieldId);
        if (!field) return;
        
        const formGroup = field.parentElement;
        if (!formGroup) return;
        
        formGroup.classList.add('error');
        let errorElement = formGroup.querySelector('.error-message');
        
        if (!errorElement) {
            errorElement = document.createElement('div');
            errorElement.className = 'error-message';
            formGroup.appendChild(errorElement);
        }
        
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    clearErrors(formType) {
        const form = document.getElementById(formType + 'Form');
        if (!form) return;
        
        const formGroups = form.querySelectorAll('.form-group');
        formGroups.forEach(group => {
            group.classList.remove('error');
            const errorElement = group.querySelector('.error-message');
            if (errorElement) {
                errorElement.textContent = '';
                errorElement.style.display = 'none';
            }
        });
    }

    clearAllMessages() {
        const messages = document.querySelectorAll('.alert-message');
        messages.forEach(msg => msg.remove());
    }

    showError(message, formType) {
        this.showMessage(message, 'error', formType);
    }

    showSuccessMessage(message) {
        this.showMessage(message, 'success');
    }

    showMessage(message, type, formType = null) {
        this.clearAllMessages();

        const messageElement = document.createElement('div');
        messageElement.className = `alert-message ${type}`;
        messageElement.innerHTML = `
            <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
            <span>${message}</span>
        `;

        if (formType) {
            const form = document.getElementById(formType + 'Form');
            if (form) {
                form.insertBefore(messageElement, form.firstChild);
            } else {
                this.insertMessageInAuthBody(messageElement);
            }
        } else {
            this.insertMessageInAuthBody(messageElement);
        }
    }

    insertMessageInAuthBody(messageElement) {
        const authBody = document.querySelector('.auth-body');
        if (authBody) {
            authBody.insertBefore(messageElement, authBody.firstChild);
        }
    }

    setLoadingState(formType, loading) {
        const button = document.querySelector(`#${formType}Form .auth-btn`);
        if (!button) return;
        
        const buttonText = button.querySelector('.btn-text');
        const buttonLoading = button.querySelector('.btn-loading');

        if (loading) {
            button.disabled = true;
            if (buttonText) buttonText.style.display = 'none';
            if (buttonLoading) buttonLoading.style.display = 'flex';
        } else {
            button.disabled = false;
            if (buttonText) buttonText.style.display = 'block';
            if (buttonLoading) buttonLoading.style.display = 'none';
        }
    }

    getUserFriendlyErrorMessage(error) {
        if (error.message.includes('Failed to fetch') || error.message.includes('NetworkError')) {
            return 'Network error. Please check your internet connection and make sure the backend server is running.';
        } else if (error.message.includes('JSON')) {
            return 'Server configuration error. Please contact administrator.';
        } else if (error.message.includes('CORS')) {
            return 'Cross-origin request blocked. Please check server CORS configuration.';
        } else {
            return error.message || 'An unexpected error occurred. Please try again.';
        }
    }

    validateField(field) {
        const value = field.value.trim();
        const fieldId = field.id;

        if (!value) {
            this.showFieldError(fieldId, 'This field is required');
            return false;
        }

        if (fieldId.includes('email') && !this.isValidEmail(value)) {
            this.showFieldError(fieldId, 'Please enter a valid email address');
            return false;
        }

        if (fieldId.includes('password') && value.length < 6) {
            this.showFieldError(fieldId, 'Password must be at least 6 characters');
            return false;
        }

        return true;
    }
}

// Initialize the authentication system
document.addEventListener('DOMContentLoaded', () => {
    new AuthSystem();
});