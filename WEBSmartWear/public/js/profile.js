document.addEventListener('DOMContentLoaded', async () => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
        // Redirect to login page if not logged in
        window.location.href = '/views/login.html?redirect=profile';
        return;
    }
    
    // Display user name
    const userName = localStorage.getItem('userName');
    const userNameSpan = document.getElementById('user-name');
    if (userNameSpan && userName) {
        userNameSpan.textContent = userName;
    }
    
    // Load user data
    await loadUserData();
    
    // Setup tab navigation
    setupTabs();
    
    // Setup form handlers
    setupFormHandlers();
    
    // Setup logout button
    setupLogout();
});

// Load user data from API
async function loadUserData() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/auth/me', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (!response.ok) {
            throw new Error('Kullanıcı bilgileri alınamadı');
        }
        
        const user = await response.json();
        
        // Fill account form
        const nameInput = document.getElementById('profile-name');
        const emailInput = document.getElementById('profile-email');
        
        if (nameInput && user.name) {
            nameInput.value = user.name;
        }
        
        if (emailInput && user.email) {
            emailInput.value = user.email;
        }
        
        // Load other user-related data
        loadOrdersData();
        loadFavoritesData();
        loadReviewsData();
        loadCouponsData();
        
    } catch (error) {
        console.error('Error loading user data:', error);
        showMessage('account-message', error.message, 'error');
    }
}

// Load orders data
function loadOrdersData() {
    // This would typically fetch from an API
    // For now we'll just show the empty state
}

// Load favorites data
function loadFavoritesData() {
    try {
        const favorites = JSON.parse(localStorage.getItem('favorites')) || [];
        const favoritesList = document.getElementById('favorites-list');
        
        if (favorites.length === 0) {
            return; // Keep the empty state
        }
        
        // Clear empty state
        favoritesList.innerHTML = '';
        
        // Create a grid to display favorites
        const favoritesGrid = document.createElement('div');
        favoritesGrid.className = 'favorites-grid';
        favoritesList.appendChild(favoritesGrid);
        
        // This would typically fetch product details from an API using the IDs
        // For now, we'll just show placeholder content
        favorites.forEach(productId => {
            const favoriteItem = document.createElement('div');
            favoriteItem.className = 'favorite-item';
            favoriteItem.innerHTML = `
                <div class="favorite-img-placeholder">
                    <i class="fas fa-tshirt"></i>
                </div>
                <div class="favorite-details">
                    <h4>Ürün #${productId}</h4>
                    <p class="price">₺199.99</p>
                    <div class="favorite-actions">
                        <button class="add-to-cart"><i class="fas fa-shopping-cart"></i></button>
                        <button class="remove-favorite" data-id="${productId}"><i class="fas fa-trash"></i></button>
                    </div>
                </div>
            `;
            favoritesGrid.appendChild(favoriteItem);
        });
        
        // Add event listeners to remove buttons
        document.querySelectorAll('.remove-favorite').forEach(button => {
            button.addEventListener('click', () => {
                const productId = button.getAttribute('data-id');
                removeFavorite(productId);
            });
        });
    } catch (error) {
        console.error('Error loading favorites:', error);
    }
}

// Remove a favorite
function removeFavorite(productId) {
    try {
        let favorites = JSON.parse(localStorage.getItem('favorites')) || [];
        favorites = favorites.filter(id => id != productId);
        localStorage.setItem('favorites', JSON.stringify(favorites));
        
        // Reload favorites
        loadFavoritesData();
    } catch (error) {
        console.error('Error removing favorite:', error);
    }
}

// Load reviews data
function loadReviewsData() {
    // This would typically fetch from an API
    // For now we'll just show the empty state
}

// Load coupons data
function loadCouponsData() {
    // This would typically fetch from an API
    // For now we'll just show the empty state and possibly some sample coupons
    const couponsList = document.getElementById('coupons-list');
    
    // If we want to show sample coupons:
    if (Math.random() > 0.5) { // 50% chance to show sample coupons
        couponsList.innerHTML = '';
        
        // Sample coupon
        const coupon = document.createElement('div');
        coupon.className = 'coupon-item';
        coupon.innerHTML = `
            <div class="coupon-content">
                <div class="coupon-header">
                    <h4>Hoş Geldin İndirimi</h4>
                    <span class="coupon-code">WELCOME20</span>
                </div>
                <p class="coupon-desc">İlk siparişinizde %20 indirim</p>
                <div class="coupon-footer">
                    <span class="coupon-expiry">Son Kullanım: 31.12.2023</span>
                    <button class="copy-coupon" data-code="WELCOME20">Kopyala</button>
                </div>
            </div>
        `;
        couponsList.appendChild(coupon);
        
        // Add event listener to copy button
        coupon.querySelector('.copy-coupon').addEventListener('click', function() {
            const code = this.getAttribute('data-code');
            navigator.clipboard.writeText(code).then(() => {
                this.textContent = 'Kopyalandı!';
                setTimeout(() => {
                    this.textContent = 'Kopyala';
                }, 2000);
            });
        });
    }
}

// Setup tab navigation
function setupTabs() {
    const navItems = document.querySelectorAll('.profile-nav-item');
    const tabs = document.querySelectorAll('.profile-tab');
    
    navItems.forEach(item => {
        if (item.id === 'logout-btn') return;
        
        item.addEventListener('click', () => {
            // Remove active class from all nav items and tabs
            navItems.forEach(nav => {
                if (nav.id !== 'logout-btn') {
                    nav.classList.remove('active');
                }
            });
            tabs.forEach(tab => tab.classList.remove('active'));
            
            // Add active class to clicked nav item and corresponding tab
            item.classList.add('active');
            const tabId = `${item.dataset.tab}-tab`;
            document.getElementById(tabId).classList.add('active');
        });
    });
}

// Setup form handlers
function setupFormHandlers() {
    // Account form
    const accountForm = document.getElementById('account-form');
    if (accountForm) {
        accountForm.addEventListener('submit', handleAccountUpdate);
    }
    
    // Password form
    const passwordForm = document.getElementById('password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', handlePasswordUpdate);
    }
    
    // Address form
    const addAddressBtn = document.getElementById('add-address-btn');
    const addressForm = document.getElementById('address-form');
    const cancelAddressBtn = document.getElementById('cancel-address-btn');
    
    if (addAddressBtn) {
        addAddressBtn.addEventListener('click', () => {
            const formContainer = document.getElementById('address-form-container');
            if (formContainer) {
                formContainer.style.display = 'block';
                addAddressBtn.style.display = 'none';
            }
        });
    }
    
    if (cancelAddressBtn) {
        cancelAddressBtn.addEventListener('click', () => {
            const formContainer = document.getElementById('address-form-container');
            if (formContainer) {
                formContainer.style.display = 'none';
                addAddressBtn.style.display = 'block';
                
                // Reset form
                if (addressForm) {
                    addressForm.reset();
                }
            }
        });
    }
    
    if (addressForm) {
        addressForm.addEventListener('submit', handleAddressSubmit);
    }
}

// Handle account update
async function handleAccountUpdate(e) {
    e.preventDefault();
    
    const nameInput = document.getElementById('profile-name');
    const name = nameInput.value.trim();
    
    if (!name) {
        showMessage('account-message', 'Lütfen isim alanını doldurun', 'error');
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/auth/update-profile', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ name })
        });
        
        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Profil güncellenirken bir hata oluştu');
        }
        
        const data = await response.json();
        
        // Update local storage
        localStorage.setItem('userName', data.name);
        
        // Update displayed name
        const userNameSpan = document.getElementById('user-name');
        if (userNameSpan) {
            userNameSpan.textContent = data.name;
        }
        
        showMessage('account-message', 'Profil başarıyla güncellendi', 'success');
    } catch (error) {
        console.error('Error updating profile:', error);
        showMessage('account-message', error.message, 'error');
    }
}

// Handle password update
async function handlePasswordUpdate(e) {
    e.preventDefault();
    
    const currentPasswordInput = document.getElementById('current-password');
    const newPasswordInput = document.getElementById('new-password');
    const confirmPasswordInput = document.getElementById('confirm-password');
    
    const currentPassword = currentPasswordInput.value;
    const newPassword = newPasswordInput.value;
    const confirmPassword = confirmPasswordInput.value;
    
    // Validate passwords
    if (!currentPassword || !newPassword || !confirmPassword) {
        showMessage('password-message', 'Lütfen tüm alanları doldurun', 'error');
        return;
    }
    
    if (newPassword.length < 6) {
        showMessage('password-message', 'Yeni şifre en az 6 karakter olmalıdır', 'error');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showMessage('password-message', 'Yeni şifreler eşleşmiyor', 'error');
        return;
    }
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch('/api/auth/change-password', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ currentPassword, newPassword })
        });
        
        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Şifre değiştirilirken bir hata oluştu');
        }
        
        // Reset form
        currentPasswordInput.value = '';
        newPasswordInput.value = '';
        confirmPasswordInput.value = '';
        
        showMessage('password-message', 'Şifre başarıyla değiştirildi', 'success');
    } catch (error) {
        console.error('Error changing password:', error);
        showMessage('password-message', error.message, 'error');
    }
}

// Handle address submission
async function handleAddressSubmit(e) {
    e.preventDefault();
    
    const title = document.getElementById('address-title').value.trim();
    const address = document.getElementById('address-text').value.trim();
    const city = document.getElementById('address-city').value.trim();
    const postal = document.getElementById('address-postal').value.trim();
    const phone = document.getElementById('address-phone').value.trim();
    
    if (!title || !address || !city || !postal || !phone) {
        alert('Lütfen tüm alanları doldurun');
        return;
    }
    
    // This would typically call an API endpoint to save the address
    // For now, we'll just simulate success and display the address
    
    const addressList = document.getElementById('address-list');
    const emptyState = addressList.querySelector('.empty-state');
    
    if (emptyState) {
        addressList.innerHTML = '';
    }
    
    const addressItem = document.createElement('div');
    addressItem.className = 'address-item';
    addressItem.innerHTML = `
        <h4>${title}</h4>
        <p>${address}</p>
        <p>${city}, ${postal}</p>
        <p>${phone}</p>
        <div class="address-actions">
            <button class="edit-address" data-id="temp">Düzenle</button>
            <button class="delete-address" data-id="temp">Sil</button>
        </div>
    `;
    
    addressList.appendChild(addressItem);
    
    // Reset and hide form
    document.getElementById('address-form').reset();
    document.getElementById('address-form-container').style.display = 'none';
    document.getElementById('add-address-btn').style.display = 'block';
}

// Setup logout functionality
function setupLogout() {
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            // Clear auth data
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('userName');
            
            // Redirect to home page
            window.location.href = '/';
        });
    }
}

// Show message in form
function showMessage(elementId, message, type) {
    const messageElement = document.getElementById(elementId);
    if (messageElement) {
        messageElement.textContent = message;
        messageElement.className = `form-message ${type}`;
        
        // Clear message after 5 seconds
        setTimeout(() => {
            messageElement.textContent = '';
            messageElement.className = 'form-message';
        }, 5000);
    }
} 