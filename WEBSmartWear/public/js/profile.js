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
        
        // Load address data
        loadAddressData(user);
        
        // Load other user-related data
        loadOrdersData();
        loadFavoritesData();
        
    } catch (error) {
        console.error('Error loading user data:', error);
        showMessage('account-message', error.message, 'error');
    }
}

// Load address data
function loadAddressData(user) {
    const noAddressMessage = document.getElementById('no-address-message');
    const addressContent = document.getElementById('address-content');
    const addressValue = document.getElementById('address-value');
    const addressFormContainer = document.getElementById('address-form-container');
    const addressText = document.getElementById('address-text');
    
    if (user.address && user.address.trim() !== '') {
        // Show address content
        noAddressMessage.style.display = 'none';
        addressContent.style.display = 'block';
        addressValue.textContent = user.address;
        
        // Set form value in case user wants to edit
        if (addressText) {
            addressText.value = user.address;
        }
    } else {
        // Show no address message and form
        noAddressMessage.style.display = 'block';
        addressContent.style.display = 'none';
        addressFormContainer.style.display = 'block';
    }
    
    // Add event listener for edit button
    const editAddressBtn = document.getElementById('edit-address-btn');
    if (editAddressBtn) {
        editAddressBtn.addEventListener('click', () => {
            addressContent.style.display = 'none';
            addressFormContainer.style.display = 'block';
        });
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
            const tabId = `${item.getAttribute('data-tab')}-tab`;
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
    const addressForm = document.getElementById('address-form');
    const cancelAddressBtn = document.getElementById('cancel-address-btn');
    
    if (addressForm) {
        addressForm.addEventListener('submit', handleAddressSubmit);
    }
    
    if (cancelAddressBtn) {
        cancelAddressBtn.addEventListener('click', () => {
            const addressContent = document.getElementById('address-content');
            const formContainer = document.getElementById('address-form-container');
            const noAddressMessage = document.getElementById('no-address-message');
            
            // Check if we have an address already
            if (addressContent.style.display === 'none' && noAddressMessage.style.display === 'block') {
                // No address, keep the form visible
                return;
            }
            
            // Hide form and show content
            formContainer.style.display = 'none';
            addressContent.style.display = 'block';
        });
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
        console.error('Error updating account:', error);
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
    
    if (!currentPassword || !newPassword || !confirmPassword) {
        showMessage('password-message', 'Lütfen tüm alanları doldurun', 'error');
        return;
    }
    
    if (newPassword !== confirmPassword) {
        showMessage('password-message', 'Yeni şifreler eşleşmiyor', 'error');
        return;
    }
    
    if (newPassword.length < 6) {
        showMessage('password-message', 'Şifre en az 6 karakter olmalıdır', 'error');
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
            body: JSON.stringify({
                currentPassword,
                newPassword
            })
        });
        
        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Şifre değiştirme başarısız oldu');
        }
        
        // Reset form
        currentPasswordInput.value = '';
        newPasswordInput.value = '';
        confirmPasswordInput.value = '';
        
        showMessage('password-message', 'Şifreniz başarıyla değiştirildi', 'success');
    } catch (error) {
        console.error('Error changing password:', error);
        showMessage('password-message', error.message, 'error');
    }
}

// Handle address submit
async function handleAddressSubmit(e) {
    e.preventDefault();
    
    const addressInput = document.getElementById('address-text');
    const address = addressInput.value.trim();
    
    if (!address) {
        showMessage('address-message', 'Lütfen adres alanını doldurun', 'error');
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
            body: JSON.stringify({ address })
        });
        
        if (!response.ok) {
            const data = await response.json();
            throw new Error(data.message || 'Adres güncellenirken bir hata oluştu');
        }
        
        const data = await response.json();
        
        // Update the address display
        const noAddressMessage = document.getElementById('no-address-message');
        const addressContent = document.getElementById('address-content');
        const addressValue = document.getElementById('address-value');
        const addressFormContainer = document.getElementById('address-form-container');
        
        // Show the address and hide the form
        noAddressMessage.style.display = 'none';
        addressContent.style.display = 'block';
        addressValue.textContent = address;
        addressFormContainer.style.display = 'none';
        
        showMessage('address-message', 'Adres başarıyla güncellendi', 'success');
    } catch (error) {
        console.error('Error updating address:', error);
        showMessage('address-message', error.message, 'error');
    }
}

// Setup logout
function setupLogout() {
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            // Clear auth data from localStorage
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('userName');
            
            // Redirect to homepage
            window.location.href = '/';
        });
    }
}

// Show message
function showMessage(elementId, message, type) {
    const messageElement = document.getElementById(elementId);
    if (messageElement) {
        messageElement.textContent = message;
        messageElement.className = `form-message ${type}`;
        
        // Clear message after a timeout
        setTimeout(() => {
            messageElement.textContent = '';
            messageElement.className = 'form-message';
        }, 5000);
    }
} 