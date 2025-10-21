// forgot-password.js

class ForgotPassword {
    constructor() {
        this.currentStep = 1;
        this.userEmail = '';
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        // Form submission
        document.getElementById('forgotPasswordForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleSubmit();
        });

        // Resend email button
        document.getElementById('resendBtn').addEventListener('click', () => {
            this.handleResend();
        });

        // Real-time email validation
        document.getElementById('email').addEventListener('input', () => {
            this.hideError('emailError');
        });
    }

    async handleSubmit() {
        const email = document.getElementById('email').value.trim();
        
        if (!this.validateEmail(email)) {
            this.showError('emailError', 'Please enter a valid email address');
            return;
        }

        this.userEmail = email;
        this.setLoadingState(true);

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
                this.showSuccessStep();
            } else {
                // For security, we show success even if email doesn't exist
                this.showSuccessStep();
            }
        } catch (error) {
            console.error('Error:', error);
            // For security, we show success even if there's an error
            this.showSuccessStep();
        } finally {
            this.setLoadingState(false);
        }
    }

    async handleResend() {
        this.setResendLoadingState(true);

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
                this.showResendSuccess();
            } else {
                this.showResendError(data.message || 'Failed to resend email');
            }
        } catch (error) {
            console.error('Error:', error);
            this.showResendError('Network error. Please try again.');
        } finally {
            this.setResendLoadingState(false);
        }
    }

    showSuccessStep() {
        document.getElementById('sentEmail').textContent = this.userEmail;
        this.goToStep(2);
    }

    goToStep(step) {
        // Hide all steps
        document.querySelectorAll('.step').forEach(stepEl => {
            stepEl.classList.remove('active');
        });
        
        // Show current step
        document.getElementById(`step${step}`).classList.add('active');
        this.currentStep = step;
    }

    validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    showError(elementId, message) {
        const errorElement = document.getElementById(elementId);
        errorElement.textContent = message;
        errorElement.classList.add('show');
        
        // Add error styling to input
        const inputElement = document.getElementById('email');
        inputElement.style.borderColor = '#dc3545';
        inputElement.style.background = '#fff5f5';
    }

    hideError(elementId) {
        const errorElement = document.getElementById(elementId);
        errorElement.classList.remove('show');
        
        // Remove error styling from input
        const inputElement = document.getElementById('email');
        inputElement.style.borderColor = '';
        inputElement.style.background = '';
    }

    setLoadingState(loading) {
        const submitBtn = document.getElementById('submitBtn');
        const btnText = submitBtn.querySelector('.btn-text');
        const btnLoading = submitBtn.querySelector('.btn-loading');

        if (loading) {
            submitBtn.disabled = true;
            btnText.style.display = 'none';
            btnLoading.style.display = 'flex';
        } else {
            submitBtn.disabled = false;
            btnText.style.display = 'block';
            btnLoading.style.display = 'none';
        }
    }

    setResendLoadingState(loading) {
        const resendBtn = document.getElementById('resendBtn');
        const originalText = resendBtn.innerHTML;

        if (loading) {
            resendBtn.disabled = true;
            resendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Resending...';
        } else {
            resendBtn.disabled = false;
            resendBtn.innerHTML = '<i class="fas fa-redo"></i> Resend Email';
        }
    }

    showResendSuccess() {
        this.showToast('Reset email sent successfully!', 'success');
    }

    showResendError(message) {
        this.showToast(message, 'error');
    }

    showToast(message, type = 'info') {
        // Remove existing toasts
        const existingToasts = document.querySelectorAll('.toast');
        existingToasts.forEach(toast => toast.remove());

        // Create toast element
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <div class="toast-content">
                <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
                <span>${message}</span>
            </div>
        `;

        // Add styles
        toast.style.cssText = `
            position: fixed;
            top: 100px;
            right: 20px;
            background: ${type === 'success' ? '#28a745' : '#dc3545'};
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 8px;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
            z-index: 10000;
            animation: slideInRight 0.3s ease-out;
            max-width: 400px;
        `;

        document.body.appendChild(toast);

        // Remove toast after 5 seconds
        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease-in';
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    }
}

// Add CSS for toast animations
const toastStyles = `
@keyframes slideInRight {
    from {
        transform: translateX(100%);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes slideOutRight {
    from {
        transform: translateX(0);
        opacity: 1;
    }
    to {
        transform: translateX(100%);
        opacity: 0;
    }
}

.toast-content {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.toast-content i {
    font-size: 1.2rem;
}
`;

// Inject toast styles
const styleSheet = document.createElement('style');
styleSheet.textContent = toastStyles;
document.head.appendChild(styleSheet);

// Initialize the forgot password flow when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new ForgotPassword();
});