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
    console.log('Gelen ürün verisi:', product);

    // Mutlaka doğru resim yolunu bulalım
    let productImageSrc = '../img/product-placeholder.jpg';
    
    // API'den gelen veriye göre uygun alanı kontrol et ve kullan
    if (product.imagePath && product.imagePath !== 'null' && product.imagePath !== 'undefined') {
        productImageSrc = product.imagePath;
    } else if (product.image && product.image !== 'null' && product.image !== 'undefined') {
        productImageSrc = product.image;
    } else if (product.thumbnail && product.thumbnail !== 'null' && product.thumbnail !== 'undefined') {
        productImageSrc = product.thumbnail;
    }
    
    // Görsel URL'sinin doğru formatta olduğundan emin olalım
    if (productImageSrc && !productImageSrc.startsWith('http') && !productImageSrc.startsWith('/') && !productImageSrc.startsWith('../')) {
        // Eğer URL tam değilse, hem img hem de images/products klasörünü deneyelim
        // Görselin img klasöründen mi yoksa images/products klasöründen mi geleceğini bilemediğimiz için
        // önce img klasörünü deneyelim, hata olursa images/products klasörüne geçeceğiz (onerror eventinde)
        productImageSrc = '../img/' + productImageSrc;
    }
    
    console.log('Kullanılan resim yolu:', productImageSrc);
    
    // Set product details
    productImage.src = productImageSrc;
    productImage.onerror = function() {
        if (this.src.includes('/img/')) {
            // İlk yol başarısız olduysa, images/products klasörünü deneyelim
            this.src = this.src.replace('/img/', '/images/products/');
            console.log('İkinci yol deneniyor:', this.src);
        } else if (this.src.includes('/images/products/')) {
            // Bu da başarısız olduysa, placeholder'a geçelim
            this.src = '../img/product-placeholder.jpg';
            console.log('Placeholder kullanılıyor');
        } else {
            // Başka türlü bir hata olduğunda da placeholder'ı kullanalım
            this.src = '../img/product-placeholder.jpg';
            console.log('Placeholder kullanılıyor');
        }
    };
    
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
        // Doğru resim yolunu bulalım
        let productImageSrc = '../img/product-placeholder.jpg';
        
        if (currentProduct.imagePath && currentProduct.imagePath !== 'null' && currentProduct.imagePath !== 'undefined') {
            productImageSrc = currentProduct.imagePath;
        } else if (currentProduct.image && currentProduct.image !== 'null' && currentProduct.image !== 'undefined') {
            productImageSrc = currentProduct.image;
        } else if (currentProduct.thumbnail && currentProduct.thumbnail !== 'null' && currentProduct.thumbnail !== 'undefined') {
            productImageSrc = currentProduct.thumbnail;
        }
        
        // Görsel URL'sinin doğru formatta olduğundan emin olalım
        if (productImageSrc && !productImageSrc.startsWith('http') && !productImageSrc.startsWith('/') && !productImageSrc.startsWith('../')) {
            // Eğer URL tam değilse ön ek ekleyelim
            productImageSrc = '../img/' + productImageSrc;
        }
        
        cart.push({
            productId: currentProduct._id,
            name: currentProduct.name,
            price: currentProduct.price,
            image: productImageSrc,
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

// Toggle favorite - Gelişmiş
function toggleFavorite() {
    if (!currentProduct) return;
    
    const productId = currentProduct._id;
    const index = favorites.indexOf(productId);
    
    if (index === -1) {
        // Add to favorites
        favorites.push(productId);
        
        // UI güncelleme
        addToFavoritesBtn.innerHTML = '<i class="fas fa-heart"></i> Favorilerden Çıkar';
        addToFavoritesBtn.classList.add('active');
        
        // Gelişmiş bildirim
        if (typeof showEnhancedNotification === 'function') {
            showEnhancedNotification('Ürün favorilere eklendi!', 'success');
        } else {
            showNotification('Ürün favorilere eklendi!', 'success');
        }
    } else {
        // Remove from favorites
        favorites.splice(index, 1);
        
        // UI güncelleme
        addToFavoritesBtn.innerHTML = '<i class="far fa-heart"></i> Favorilere Ekle';
        addToFavoritesBtn.classList.remove('active');
        
        // Gelişmiş bildirim
        if (typeof showEnhancedNotification === 'function') {
            showEnhancedNotification('Ürün favorilerden çıkarıldı!', 'info');
        } else {
            showNotification('Ürün favorilerden çıkarıldı!', 'info');
        }
    }
    
    // Update localStorage
    localStorage.setItem('favorites', JSON.stringify(favorites));
}

// Update favorite button state - Gelişmiş
function updateFavoriteButtonState() {
    if (!currentProduct) return;
    
    const isFavorite = favorites.includes(currentProduct._id);
    
    if (isFavorite) {
        addToFavoritesBtn.innerHTML = '<i class="fas fa-heart"></i> Favorilerden Çıkar';
        addToFavoritesBtn.classList.add('active');
    } else {
        addToFavoritesBtn.innerHTML = '<i class="far fa-heart"></i> Favorilere Ekle';
        addToFavoritesBtn.classList.remove('active');
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

// Show notification - Gelişmiş
function showNotification(message, type = 'success') {
    // Gelişmiş bildirim sistemi varsa onu kullan
    if (typeof showEnhancedNotification === 'function') {
        return showEnhancedNotification(message, type);
    }
    
    // Bildirim container'ı kontrol et
    let notificationContainer = document.getElementById('notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'notification-container';
        notificationContainer.style.position = 'fixed';
        notificationContainer.style.top = '20px';
        notificationContainer.style.right = '20px';
        notificationContainer.style.zIndex = '9999';
        document.body.appendChild(notificationContainer);
    }
    
    // Bildirim oluştur
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    
    // Bildirim stili
    notification.style.backgroundColor = type === 'success' ? '#4caf50' : 
                                         type === 'error' ? '#f44336' : 
                                         type === 'info' ? '#2196F3' : '#ff9800';
    notification.style.color = 'white';
    notification.style.padding = '12px 20px';
    notification.style.marginBottom = '10px';
    notification.style.borderRadius = '4px';
    notification.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
    notification.style.transition = 'all 0.3s ease';
    
    // İkon ekle
    let icon = '';
    if (type === 'success') icon = '<i class="fas fa-check-circle"></i> ';
    else if (type === 'error') icon = '<i class="fas fa-exclamation-circle"></i> ';
    else if (type === 'info') icon = '<i class="fas fa-info-circle"></i> ';
    else icon = '<i class="fas fa-bell"></i> ';
    
    notification.innerHTML = icon + message;
    
    // Bildirim containerına ekle
    notificationContainer.appendChild(notification);
    
    // 3 saniye sonra bildirimi kaldır
    setTimeout(() => {
        notification.style.opacity = '0';
    setTimeout(() => {
        notification.remove();
        }, 300);
    }, 2700);
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