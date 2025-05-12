// DOM Elements
const productsContainer = document.getElementById('products-container');
const categoryFilters = document.querySelectorAll('.category-filter');
let currentCategory = 'all'; // Default category

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Get current page path
    const currentPath = window.location.pathname;
    
    // Determine initial category based on page URL
    if (currentPath.includes('clothing.html')) {
        currentCategory = 'Giyim';
        loadProductsByCategory('Giyim');
        // Giyim sayfasında filtreleri gösterme
        hideOtherCategoryFilters();
    } else if (currentPath.includes('shoes.html')) {
        currentCategory = 'Ayakkabı';
        loadProductsByCategory('Ayakkabı');
        // Ayakkabı sayfasında filtreleri gösterme
        hideOtherCategoryFilters();
    } else if (currentPath.includes('accessories.html')) {
        currentCategory = 'Aksesuar';
        loadProductsByCategory('Aksesuar');
        // Aksesuar sayfasında filtreleri gösterme
        hideOtherCategoryFilters();
    } else {
        // Default to all products for other pages
        loadAllProducts();
    }
    
    // Add event listeners to category filters
    setupCategoryFilters();
});

// Load all products
async function loadAllProducts() {
    showLoading();
    try {
        const response = await fetch('/api/products');
        if (!response.ok) {
            throw new Error('Ürünler yüklenirken hata oluştu');
        }
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showError(error.message);
    }
}

// Load products by category
async function loadProductsByCategory(category) {
    showLoading();
    try {
        const response = await fetch(`/api/products/category/${category}`);
        
        if (!response.ok) {
            throw new Error('Kategori ürünleri yüklenirken hata oluştu');
        }
        
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        showError(error.message);
    }
}

// Display products
function displayProducts(products) {
    if (!productsContainer) return;
    
    if (products.length === 0) {
        productsContainer.innerHTML = `
            <div class="no-products">
                <i class="fas fa-box-open"></i>
                <p>Bu kategoride ürün bulunamadı</p>
            </div>
        `;
        return;
    }
    
    productsContainer.innerHTML = products.map(product => {
        // Ürün verilerini garantiye alalım
        const safeProduct = {
            _id: product._id || '',
            name: product.name || 'İsimsiz Ürün',
            price: typeof product.price === 'number' ? product.price : 0,
            image: product.image || '../img/product-placeholder.jpg',
            brand: product.brand || 'SmartWear',
            stock: typeof product.stock === 'number' ? product.stock : 0,
            color: product.color || '',
            size: product.size || '',
            featured: !!product.featured
        };
        
        return `
        <div class="product-card clickable" onclick="navigateToProductDetail('${safeProduct._id}')">
            <div class="product-img-container">
                <img src="${safeProduct.image}" alt="${safeProduct.name}" 
                     onerror="this.onerror=null; this.src='../img/product-placeholder.jpg';">
                ${safeProduct.stock <= 3 && safeProduct.stock > 0 ? `<span class="product-badge limited-stock">Sınırlı Stok</span>` : ''}
                ${safeProduct.stock === 0 ? `<span class="product-badge out-of-stock-badge">Tükendi</span>` : ''}
                ${safeProduct.featured ? `<span class="product-badge featured-badge">Öne Çıkan</span>` : ''}
            </div>
            <div class="product-card-content">
                <div class="product-brand">${safeProduct.brand}</div>
                <h3 class="product-title">${safeProduct.name}</h3>
                <div class="product-details">
                    ${safeProduct.color ? `<div class="product-color">Renk: ${safeProduct.color}</div>` : ''}
                    ${safeProduct.size ? `<div class="product-size">Beden: ${safeProduct.size}</div>` : ''}
                </div>
                <div class="product-price-container">
                    <p class="price">${safeProduct.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}</p>
                    <p class="stock ${safeProduct.stock > 0 ? 'in-stock' : 'out-of-stock'}">
                        ${safeProduct.stock > 0 ? `${safeProduct.stock} adet stokta` : 'Stokta Yok'}
                    </p>
                </div>
            </div>
            <div class="product-actions" onclick="event.stopPropagation()">
                <button class="sepete-ekle-btn" onclick="addToCartFromListing('${safeProduct._id}', '${safeProduct.name.replace(/'/g, "\\'")}', ${safeProduct.price}, '${safeProduct.image}', event)" ${safeProduct.stock === 0 ? 'disabled' : ''}>
                    <i class="fas fa-shopping-cart"></i>
                    <span>Sepete Ekle</span>
                </button>
                <button class="favorite-btn" onclick="toggleFavorite('${safeProduct._id}', event)">
                    <i class="far fa-heart"></i>
                </button>
            </div>
        </div>
    `;
    }).join('');
    
    // Favorileri kontrol edip işaretle
    checkFavorites();

    // Product card hover efektini belirginleştir
    addHoverEffectToCards();
}

// Ürün detay sayfasına yönlendirme fonksiyonu
function navigateToProductDetail(productId) {
    window.location.href = `/views/product.html?id=${productId}`;
}

// Ürün kartlarına hover efekti ekle
function addHoverEffectToCards() {
    const productCards = document.querySelectorAll('.product-card.clickable');
    productCards.forEach(card => {
        card.style.cursor = 'pointer';
        card.style.transition = 'transform 0.3s ease, box-shadow 0.3s ease';
    });
}

// Kategorilerin görünen adlarını döndürür
function getCategoryDisplayName(category) {
    const categoryDisplayNames = {
        'Erkek': 'Erkek'
    };
    
    return categoryDisplayNames[category] || category;
}

// Show loading indicator
function showLoading() {
    if (!productsContainer) return;
    productsContainer.innerHTML = `
        <div class="loading">
            <i class="fas fa-spinner fa-spin"></i> Ürünler yükleniyor...
        </div>
    `;
}

// Show error message
function showError(message) {
    if (!productsContainer) return;
    productsContainer.innerHTML = `
        <div class="error">
            <i class="fas fa-exclamation-circle"></i>
            <p>${message}</p>
        </div>
    `;
}

// Add to cart from product listing
function addToCartFromListing(productId, name, price, image, event) {
    // Olay yayılımını durdur
    if (event) {
        event.stopPropagation();
    }
    
    // Gerekli parametreleri kontrol et
    if (!productId || !name || isNaN(Number(price))) {
        console.error('Sepete eklenemedi: Eksik veya geçersiz ürün bilgisi', { productId, name, price });
        showNotification('Ürün eklenirken bir hata oluştu', 'error');
        return;
    }
    
    // Değerleri güvenli hale getir
    const safeId = String(productId);
    const safeName = String(name);
    const safePrice = Number(price);
    const safeImage = image || '../img/product-placeholder.jpg';
    
    // Tüm değerleri güvenli şekilde aktar
    if (typeof addToCart === 'function') {
        addToCart(safeId, safeName, safePrice, safeImage, 1);
    } else {
        // Fallback eski koda
        try {
            let cart = JSON.parse(localStorage.getItem('cart')) || [];
            
            // Nesne olarak ekle
            cart.push({
                productId: safeId,
                name: safeName,
                price: safePrice,
                image: safeImage,
                quantity: 1
            });
            
            localStorage.setItem('cart', JSON.stringify(cart));
            
            if (typeof updateCartCount === 'function') {
                updateCartCount();
            }
            
            showNotification('Ürün sepete eklendi!');
        } catch (error) {
            console.error('Sepete ekleme hatası:', error);
            showNotification('Ürün eklenirken bir hata oluştu');
        }
    }
}

// Favorileri localStorage'da saklamak için yardımcı fonksiyonlar
function getFavorites() {
    const favorites = localStorage.getItem('favorites');
    return favorites ? JSON.parse(favorites) : [];
}

function saveFavorites(favorites) {
    localStorage.setItem('favorites', JSON.stringify(favorites));
}

// Favorilere ekleme/çıkarma işlemi - Geliştirilmiş Versiyon
async function toggleFavorite(productId, event) {
    // Olay yayılımını durdur
    if (event) {
        event.stopPropagation();
    }
    
    const favorites = getFavorites();
    const index = favorites.indexOf(productId);
    
    let button;
    if (event && event.target) {
        button = event.target.closest('.favorite-btn');
    }
    
    if (index === -1) {
        // Favorilere ekle
        favorites.push(productId);
        
        // GUI güncelleme
        if (button) {
            button.innerHTML = '<i class="fas fa-heart"></i>';
            button.classList.add('active');
            button.setAttribute('title', 'Favorilerden Çıkar');
        }
        
        // Bildirim göster
        if (typeof showEnhancedNotification === 'function') {
            showEnhancedNotification('Ürün favorilere eklendi', 'success');
        } else {
            showNotification('Ürün favorilere eklendi', 'success');
        }
    } else {
        // Favorilerden çıkar
        favorites.splice(index, 1);
        
        // GUI güncelleme
        if (button) {
            button.innerHTML = '<i class="far fa-heart"></i>';
            button.classList.remove('active');
            button.setAttribute('title', 'Favorilere Ekle');
        }
        
        // Bildirim göster
        if (typeof showEnhancedNotification === 'function') {
            showEnhancedNotification('Ürün favorilerden çıkarıldı', 'info');
        } else {
            showNotification('Ürün favorilerden çıkarıldı', 'info');
        }
    }
    
    saveFavorites(favorites);
    
    // Ürün kartlarını güncelle
    updateProductCards();
    
    // Uygulamanın diğer bölümlerini de güncelle
    if (typeof checkFavorites === 'function') {
        checkFavorites();
    }
}

// Bildirim gösterme fonksiyonu - Geliştirilmiş Versiyon
function showNotification(message, type = 'success') {
    // showEnhancedNotification fonksiyonu varsa onu kullan
    if (typeof showEnhancedNotification === 'function') {
        return showEnhancedNotification(message, type);
    }
    
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    // Bildirim konteynerı mevcut değilse oluştur
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
    
    // Bildirimi ekle
    notificationContainer.appendChild(notification);
    
    // 3 saniye sonra bildirimi kaldır
    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 2700);
}

// Ürün kartlarını güncelleme fonksiyonu - Geliştirilmiş Versiyon
function updateProductCards() {
    const favorites = getFavorites();
    const favoriteButtons = document.querySelectorAll('.favorite-btn');
    
    favoriteButtons.forEach(button => {
        // Daha güvenli bir şekilde productId çıkarma
        let productId;
        
        // dataset'ten ürün ID'sini al
        if (button.dataset.productId) {
            productId = button.dataset.productId;
        } else {
            // onclick'ten parse et
            const onclickAttr = button.getAttribute('onclick');
            if (onclickAttr) {
                const match = onclickAttr.match(/['"]([\w\d]+)['"]/);
                if (match && match[1]) {
                    productId = match[1];
                }
            }
        }
        
        // Eğer ürün ID'si bulunduysa, favori durumunu güncelle
        if (productId) {
            if (favorites.includes(productId)) {
                button.innerHTML = '<i class="fas fa-heart"></i>';
                button.classList.add('active');
                button.setAttribute('title', 'Favorilerden Çıkar');
            } else {
                button.innerHTML = '<i class="far fa-heart"></i>';
            button.classList.remove('active');
                button.setAttribute('title', 'Favorilere Ekle');
            }
        }
    });
}

// Sayfa yüklendiğinde favori durumlarını güncelle
document.addEventListener('DOMContentLoaded', () => {
    updateProductCards();
});

// Setup category filters
function setupCategoryFilters() {
    categoryFilters.forEach(filter => {
        filter.addEventListener('click', () => {
            const category = filter.getAttribute('data-category');
            
            // Update active class
            categoryFilters.forEach(f => f.classList.remove('active'));
            filter.classList.add('active');
            
            // Update current category
            currentCategory = category;
            
            // Load products
            if (category === 'all') {
                loadAllProducts();
            } else {
                loadProductsByCategory(category);
            }
        });
    });
}

// Kadın ve Erkek sayfalarında diğer kategorileri gizle
function hideOtherCategoryFilters() {
    // Kategori filtreleri zaten HTML'den kaldırıldı, ek bir işlem gerekmiyor
    // Bu fonksiyon gelecekteki değişiklikler için burada kalıyor
}

// Activate the navbar link corresponding to the current category
function activateCurrentCategoryLink() {
    const categoryLinks = document.querySelectorAll('.category-nav-link');
    const path = window.location.pathname;
    
    categoryLinks.forEach(link => {
        // Get category from data attribute
        const category = link.getAttribute('data-category');
        
        // Check if current path matches link or category matches the active category
        if ((path.includes(link.getAttribute('href'))) || 
            (currentCategory && category && currentCategory === category)) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });
}
