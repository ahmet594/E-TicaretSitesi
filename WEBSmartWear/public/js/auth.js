// DOM Elements
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const errorMessage = document.getElementById('error-message');

// Event Listeners
if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
}

if (registerForm) {
    registerForm.addEventListener('submit', handleRegister);
}

// Check if user is logged in
function checkAuth() {
    const token = localStorage.getItem('token');
    return !!token;
}

// Update UI based on auth status
function updateAuthUI() {
    const token = localStorage.getItem('token');
    const userName = localStorage.getItem('userName');
    
    const authLinks = document.querySelectorAll('.auth-link');
    const profileLinks = document.querySelectorAll('.profile-link');
    
    if (token && userName) {
        // User is logged in
        authLinks.forEach(link => link.style.display = 'none');
        profileLinks.forEach(link => {
            link.style.display = 'block';
            if (link.querySelector('.user-name')) {
                link.querySelector('.user-name').textContent = userName;
            }
        });
    } else {
        // User is not logged in
        authLinks.forEach(link => link.style.display = 'block');
        profileLinks.forEach(link => link.style.display = 'none');
    }
}

// Get redirect URL from query parameters
function getRedirectUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    const redirect = urlParams.get('redirect');
    if (redirect) {
        return `/views/${redirect}.html`;
    }
    return '/';
}

// Handle Login
async function handleLogin(e) {
    e.preventDefault();
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Giriş başarısız oldu');
        }
        
        // Save auth data to localStorage
        localStorage.setItem('token', data.token);
        localStorage.setItem('userId', data.user.id);
        localStorage.setItem('userName', data.user.name);
        localStorage.setItem('userRole', data.user.role);
        
        // Redirect to homepage or the page that required login
        window.location.href = getRedirectUrl();
    } catch (error) {
        errorMessage.textContent = error.message;
    }
}

// Handle Register
async function handleRegister(e) {
    e.preventDefault();
    
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    
    // Validate password match
    if (password !== confirmPassword) {
        errorMessage.textContent = 'Şifreler eşleşmiyor';
        return;
    }
    
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, email, password })
        });
        
        const data = await response.json();
        
        if (!response.ok) {
            throw new Error(data.message || 'Kayıt işlemi başarısız oldu');
        }
        
        // Save auth data to localStorage
        localStorage.setItem('token', data.token);
        localStorage.setItem('userId', data.user.id);
        localStorage.setItem('userName', data.user.name);
        localStorage.setItem('userRole', data.user.role);
        
        // Redirect to homepage or the page that required login
        window.location.href = getRedirectUrl();
    } catch (error) {
        errorMessage.textContent = error.message;
    }
}

// Logout
function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    window.location.href = '/';
}

// Initialize
document.addEventListener('DOMContentLoaded', updateAuthUI); 