// DOM Elements
const loadingContainer = document.getElementById('loading-container');
const errorContainer = document.getElementById('error-container');
const errorMessage = document.getElementById('error-message');
const productContent = document.getElementById('product-detail-content');

// Product elements
const productImage = document.getElementById('product-image');
const productName = document.getElementById('product-name');
const productPrice = document.getElementById('product-price');
const productDescription = document.getElementById('product-description');
const productCategory = document.getElementById('product-category');
const productStock = document.getElementById('product-stock');
const addToCartBtn = document.getElementById('add-to-cart');
const addToFavoritesBtn = document.getElementById('add-to-favorites');

// State
let currentProduct = null;
let favorites = JSON.parse(localStorage.getItem('favorites')) || [];

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    // Get product ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (productId) {
        loadProductDetails(productId);
    } else {
        showError('Ürün ID\'si bulunamadı');
    }
    
    // Setup event listeners
    setupEventListeners();
});

// Load product details
async function loadProductDetails(productId) {
    try {
        showLoading();
        
        const response = await fetch(`/api/products/${productId}`);
        if (!response.ok) {
            throw new Error('Ürün detayları yüklenirken bir hata oluştu');
        }
        
        const product = await response.json();
        currentProduct = product;
        
        // Update page title
        document.title = `${product.name} | SmartWear`;
        
        // Display product details
        displayProductDetails(product);
        
        // Update UI based on favorites
        updateFavoriteButtonState();
    } catch (error) {
        showError(error.message);
    }
}

// Display product details
function displayProductDetails(product) {
    // Set product details
    productImage.src = product.image;
    productImage.alt = product.name;
    productName.textContent = product.name;
    productPrice.textContent = product.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' });
    productDescription.textContent = product.description;
    productCategory.textContent = `Kategori: ${product.category}`;
    
    // Set stock status
    if (product.stock > 0) {
        productStock.textContent = `Stok: ${product.stock}`;
        productStock.className = 'product-stock in-stock';
        addToCartBtn.disabled = false;
        addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Sepete Ekle';
    } else {
        productStock.textContent = 'Stokta Yok';
        productStock.className = 'product-stock out-of-stock';
        addToCartBtn.disabled = true;
        addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Stokta Yok';
    }
    
    // Show product content
    hideLoading();
    productContent.style.display = 'flex';
}

// Setup event listeners
function setupEventListeners() {
    // Add to cart button
    addToCartBtn.addEventListener('click', () => {
        if (currentProduct && currentProduct.stock > 0) {
            addToCart(currentProduct._id);
        }
    });
    
    // Add to favorites button
    addToFavoritesBtn.addEventListener('click', () => {
        if (currentProduct) {
            toggleFavorite(currentProduct._id);
        }
    });
}

// Add to cart
function addToCart(productId) {
    if (!currentProduct) return;
    
    // Yeni sepet fonksiyonunu çağır (cart.js'teki fonksiyon)
    if (typeof window.addToCart === 'function') {
        window.addToCart(
            currentProduct._id,
            currentProduct.name,
            currentProduct.price,
            currentProduct.image,
            1
        );
    } else {
        // Fallback eski koda
        let cart = JSON.parse(localStorage.getItem('cart')) || [];
        cart.push(productId);
        localStorage.setItem('cart', JSON.stringify(cart));
        
        // Update cart count
        if (typeof updateCartCount === 'function') {
            updateCartCount();
        }
        
        // Show notification
        showNotification('Ürün sepete eklendi!');
    }
}

// Toggle favorite
function toggleFavorite(productId) {
    const index = favorites.indexOf(productId);
    
    if (index === -1) {
        // Add to favorites
        favorites.push(productId);
        showNotification('Ürün favorilere eklendi!');
        addToFavoritesBtn.innerHTML = '<i class="fas fa-heart"></i> Favorilerden Çıkar';
    } else {
        // Remove from favorites
        favorites.splice(index, 1);
        showNotification('Ürün favorilerden çıkarıldı!');
        addToFavoritesBtn.innerHTML = '<i class="far fa-heart"></i> Favorilere Ekle';
    }
    
    // Update localStorage
    localStorage.setItem('favorites', JSON.stringify(favorites));
}

// Update favorite button state
function updateFavoriteButtonState() {
    if (!currentProduct) return;
    
    const isFavorite = favorites.includes(currentProduct._id);
    
    if (isFavorite) {
        addToFavoritesBtn.innerHTML = '<i class="fas fa-heart"></i> Favorilerden Çıkar';
    } else {
        addToFavoritesBtn.innerHTML = '<i class="far fa-heart"></i> Favorilere Ekle';
    }
}

// Show loading
function showLoading() {
    loadingContainer.style.display = 'flex';
    errorContainer.style.display = 'none';
    productContent.style.display = 'none';
}

// Hide loading
function hideLoading() {
    loadingContainer.style.display = 'none';
}

// Show error
function showError(message) {
    loadingContainer.style.display = 'none';
    errorContainer.style.display = 'flex';
    productContent.style.display = 'none';
    errorMessage.textContent = message;
}

// Show notification
function showNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
} 