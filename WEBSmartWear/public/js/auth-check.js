// This script checks if a user is authenticated
// If not, redirects to the login page with the current page as redirect parameter

document.addEventListener('DOMContentLoaded', () => {
    // Pages that require authentication
    const authRequiredPages = [
        'profile', 
        'orders', 
        'coupons', 
        'favorites', 
        'reviews', 
        'addresses'
    ];
    
    const path = window.location.pathname;
    const filename = path.substring(path.lastIndexOf('/') + 1).replace('.html', '');
    
    // Check if current page requires authentication
    if (authRequiredPages.includes(filename)) {
        const token = localStorage.getItem('token');
        
        // If no token, redirect to login
        if (!token) {
            window.location.href = `/views/login.html?redirect=${filename}`;
        }
    }
}); 