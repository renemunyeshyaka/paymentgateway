// dashboard.js

class Dashboard {
    constructor() {
        this.initializeEventListeners();
        this.loadUserData();
    }

    initializeEventListeners() {
        // User dropdown functionality
        this.setupUserDropdown();
        
        // Logout confirmation
        this.setupLogoutConfirmation();
        
        // Any other dashboard-specific interactions
    }

    setupUserDropdown() {
        const userBtn = document.querySelector('.user-btn');
        const dropdown = document.querySelector('.dropdown-menu');

        userBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            dropdown.classList.toggle('show');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', () => {
            dropdown.classList.remove('show');
        });
    }

    setupLogoutConfirmation() {
        const logoutLink = document.querySelector('.logout');
        
        logoutLink.addEventListener('click', (e) => {
            e.preventDefault();
            if (confirm('Are you sure you want to logout?')) {
                window.location.href = logoutLink.getAttribute('href');
            }
        });
    }

    loadUserData() {
        // In a real application, you would fetch user data from an API
        // For now, we'll use placeholder data
        const userName = this.getUserNameFromStorage() || 'User';
        
        document.getElementById('userName').textContent = userName;
        document.getElementById('welcomeUserName').textContent = userName;
    }

    getUserNameFromStorage() {
        // This would typically come from your authentication system
        // For demo purposes, we'll check sessionStorage
        return sessionStorage.getItem('userName') || localStorage.getItem('userName');
    }

    // Additional dashboard functionality can be added here
    // For example: loading charts, updating stats, etc.
}

// Initialize the dashboard when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new Dashboard();
});