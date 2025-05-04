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
const sizeOptionsContainer = document.getElementById('size-options');

// State
let currentProduct = null;
let favorites = JSON.parse(localStorage.getItem('favorites')) || [];
let selectedSize = null;

// Varsayılan beden setleri
const defaultSizes = {
    'Giyim': ['XS', 'S', 'M', 'L', 'XL'],
    'Ayakkabı': ['36', '37', '38', '39', '40', '41', '42', '43', '44', '45'],
    'Aksesuar': [] // Aksesuarlar için beden yok
};

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
        
        // Kategori bazlı beden seçeneklerini göster
        if (product.category === 'Giyim' || product.category === 'Ayakkabı') {
            const sizes = defaultSizes[product.category];
            sizeOptionsContainer.innerHTML = `
                <h4>Beden Seçimi</h4>
                <div class="size-buttons">
                    ${sizes.map(size => `
                        <button class="size-button" data-size="${size}">
                            ${size}
                        </button>
                    `).join('')}
                </div>
            `;
            
            // Beden seçimi için event listener'ları ekle
            const sizeButtons = sizeOptionsContainer.querySelectorAll('.size-button');
            sizeButtons.forEach(button => {
                button.addEventListener('click', () => {
                    // Önceki seçili bedeni kaldır
                    sizeButtons.forEach(btn => btn.classList.remove('selected'));
                    // Yeni bedeni seç
                    button.classList.add('selected');
                    selectedSize = button.dataset.size;
                    
                    // Sepete ekle butonunu güncelle
                    addToCartBtn.disabled = false;
                    addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Sepete Ekle';
                });
            });
            
            // Sepete ekle butonunu devre dışı bırak (beden seçilene kadar)
            addToCartBtn.disabled = true;
            addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Beden Seçiniz';
            sizeOptionsContainer.style.display = 'block';
        } else {
            // Aksesuar kategorisi için beden seçimi gerekmez
            sizeOptionsContainer.style.display = 'none';
            addToCartBtn.disabled = false;
            addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Sepete Ekle';
        }
    } else {
        productStock.textContent = 'Stokta Yok';
        productStock.className = 'product-stock out-of-stock';
        addToCartBtn.disabled = true;
        addToCartBtn.innerHTML = '<i class="fas fa-shopping-cart"></i> Stokta Yok';
        sizeOptionsContainer.style.display = 'none';
    }
    
    // Show product content
    hideLoading();
    productContent.style.display = 'flex';
}

// Add to cart
function addToCart() {
    if (!currentProduct) return;
    
    // Beden kontrolü
    if ((currentProduct.category === 'Giyim' || currentProduct.category === 'Ayakkabı') && !selectedSize) {
        showNotification('Lütfen bir beden seçiniz!', 'error');
        return;
    }
    
    // Sepete ekle
    let cart = JSON.parse(localStorage.getItem('cart')) || [];
    
    // Ürün zaten sepette var mı kontrol et
    const existingItemIndex = cart.findIndex(item => 
        item.productId === currentProduct._id && item.size === selectedSize
    );
    
    if (existingItemIndex !== -1) {
        // Ürün zaten sepette, miktarını artır
        cart[existingItemIndex].quantity += 1;
    } else {
        // Yeni ürün ekle
        cart.push({
            productId: currentProduct._id,
            name: currentProduct.name,
            price: currentProduct.price,
            image: currentProduct.image,
            quantity: 1,
            size: selectedSize || null
        });
    }
    
    // Sepeti güncelle
    localStorage.setItem('cart', JSON.stringify(cart));
    
    // Sepet sayısını güncelle
    if (typeof updateCartCount === 'function') {
        updateCartCount();
    }
    
    // Bildirim göster
    showNotification('Ürün sepete eklendi!', 'success');
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
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    document.getElementById('notification-container').appendChild(notification);
    
    // 3 saniye sonra bildirimi kaldır
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Setup event listeners
function setupEventListeners() {
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', addToCart);
    }
    
    if (addToFavoritesBtn) {
        addToFavoritesBtn.addEventListener('click', toggleFavorite);
    }
} 