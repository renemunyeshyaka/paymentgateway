
        // DOM Elements
        const header = document.getElementById('header');
        const menuToggle = document.getElementById('menuToggle');
        const navLinks = document.getElementById('navLinks');
        //const loginBtn = document.getElementById('loginBtn');
        const authModal = document.getElementById('authModal');
        const closeAuth = document.getElementById('closeAuth');
        const authTabs = document.querySelectorAll('.auth-tab');
        const authForms = document.querySelectorAll('.auth-form');
        const forgotPasswordLink = document.getElementById('forgotPassword');
        const forgotPasswordModal = document.getElementById('forgotPasswordModal');
        const closeForgotPassword = document.getElementById('closeForgotPassword');
        //const backToLogin = document.getElementById('backToLogin');
        const filterBtns = document.querySelectorAll('.filter-btn');
        const portfolioItems = document.querySelectorAll('.portfolio-item');
        const contactForm = document.getElementById('contactForm');
        
        // User authentication state
        let isLoggedIn = false;
        let currentUser = null;
        
        // Header scroll effect
        window.addEventListener('scroll', () => {
            if (window.scrollY > 100) {
                header.classList.add('header-scrolled');
            } else {
                header.classList.remove('header-scrolled');
            }
        });
        
        // Mobile menu toggle
        menuToggle.addEventListener('click', () => {
            navLinks.classList.toggle('active');
        });
        
        // Close mobile menu when clicking on a link
        document.querySelectorAll('.nav-links a').forEach(link => {
            link.addEventListener('click', () => {
                navLinks.classList.remove('active');
            });
        });
        
        /* // Authentication modal functionality
        loginBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (isLoggedIn) {
                // If user is logged in, show logout option
                if (confirm('Are you sure you want to log out?')) {
                    logout();
                }
            } else {
                // Show login modal
                authModal.style.display = 'flex';
            }
        }); */
        
        // Close auth modal
        closeAuth.addEventListener('click', () => {
            authModal.style.display = 'none';
        });
        
        // Switch between login and register tabs
        authTabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const tabName = tab.getAttribute('data-tab');
                
                // Update active tab
                authTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                
                // Show corresponding form
                authForms.forEach(form => {
                    form.classList.remove('active');
                    if (form.id === `${tabName}Form`) {
                        form.classList.add('active');
                    }
                });
            });
        });
        
        // Login form submission
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            // In a real application, this would send a request to your backend
            // For demo purposes, we'll simulate a successful login
            simulateLogin();
        });
        
        // Register form submission
        document.getElementById('registerForm').addEventListener('submit', (e) => {
            e.preventDefault();
            // In a real application, this would send a request to your backend
            alert('Registration functionality would be implemented with a backend service.');
            // Switch to login tab after registration
            authTabs.forEach(tab => {
                if (tab.getAttribute('data-tab') === 'login') {
                    tab.click();
                }
            });
        });
        
        // Forgot password functionality
        forgotPasswordLink.addEventListener('click', (e) => {
            e.preventDefault();
            authModal.style.display = 'none';
            forgotPasswordModal.style.display = 'flex';
        });
        
        closeForgotPassword.addEventListener('click', () => {
            forgotPasswordModal.style.display = 'none';
        });
        
        backToLogin.addEventListener('click', (e) => {
            e.preventDefault();
            forgotPasswordModal.style.display = 'none';
            authModal.style.display = 'flex';
        });
        
        // Forgot password form submission
        document.getElementById('forgotPasswordForm').addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Password reset link would be sent to your email in a real application.');
            forgotPasswordModal.style.display = 'none';
        });
        
        // Portfolio filtering
        filterBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                // Update active filter button
                filterBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                
                const filter = btn.getAttribute('data-filter');
                
                // Filter portfolio items
                portfolioItems.forEach(item => {
                    if (filter === 'all' || item.getAttribute('data-category') === filter) {
                        item.style.display = 'block';
                    } else {
                        item.style.display = 'none';
                    }
                });
            });
        });
        
        // Contact form submission
        contactForm.addEventListener('submit', (e) => {
            e.preventDefault();
            // In a real application, this would send the form data to your backend
            alert('Thank you for your message! We will get back to you soon.');
            contactForm.reset();
        });
        
        // Simulate login function
        function simulateLogin() {
            isLoggedIn = true;
            currentUser = {
                name: 'Demo User',
                email: 'demo@example.com'
            };
            
            // Update UI for logged in state
            loginBtn.textContent = 'Logout';
            authModal.style.display = 'none';
            
            // Show welcome message
            alert(`Welcome back, ${currentUser.name}!`);
        }
        
        // Logout function
        function logout() {
            isLoggedIn = false;
            currentUser = null;
            
            // Update UI for logged out state
            loginBtn.textContent = 'Login';
            
            // Redirect to home page (current page)
            window.scrollTo(0, 0);
        }
        
        // Close modals when clicking outside
        window.addEventListener('click', (e) => {
            if (e.target === authModal) {
                authModal.style.display = 'none';
            }
            if (e.target === forgotPasswordModal) {
                forgotPasswordModal.style.display = 'none';
            }
        });    