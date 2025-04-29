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
    
    productsContainer.innerHTML = products.map(product => `
        <div class="product-card">
            <a href="/views/product.html?id=${product._id}" class="product-link">
                <div class="product-img-container">
                    <img src="${product.image}" alt="${product.name}">
                    ${product.stock <= 3 && product.stock > 0 ? `<span class="product-badge limited-stock">Sınırlı Stok</span>` : ''}
                    ${product.stock === 0 ? `<span class="product-badge out-of-stock-badge">Tükendi</span>` : ''}
                    ${product.featured ? `<span class="product-badge featured-badge">Öne Çıkan</span>` : ''}
                </div>
                <div class="product-card-content">
                    <div class="product-brand">${product.brand || 'SmartWear'}</div>
                    <h3 class="product-title">${product.name}</h3>
                    <div class="product-details">
                        <div class="product-category">${getCategoryDisplayName(product.category)}</div>
                        ${product.subcategory ? `<div class="product-subcategory">${product.subcategory}</div>` : ''}
                        ${product.color ? `<div class="product-color">Renk: ${product.color}</div>` : ''}
                        ${product.size ? `<div class="product-size">Beden: ${product.size}</div>` : ''}
                    </div>
                    <div class="product-price-container">
                        <p class="price">${product.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}</p>
                        <p class="stock ${product.stock > 0 ? 'in-stock' : 'out-of-stock'}">
                            ${product.stock > 0 ? `${product.stock} adet stokta` : 'Stokta Yok'}
                        </p>
                    </div>
                </div>
            </a>
            <div class="product-actions">
                <button onclick="addToCartFromListing('${product._id}', '${product.name}', ${product.price}, '${product.image}')" class="add-to-cart" ${product.stock === 0 ? 'disabled' : ''}>
                    <i class="fas fa-shopping-cart"></i>
                    ${product.stock > 0 ? 'Sepete Ekle' : 'Stokta Yok'}
                </button>
                <button onclick="addToFavorites('${product._id}')" class="add-to-favorites" title="Favorilere Ekle">
                    <i class="far fa-heart"></i>
                </button>
            </div>
        </div>
    `).join('');
    
    // Favorileri kontrol edip işaretle
    checkFavorites();
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
function addToCartFromListing(productId, name, price, image) {
    // Yeni sepet fonksiyonunu çağır (cart.js'teki fonksiyon)
    if (typeof addToCart === 'function') {
        addToCart(productId, name, price, image, 1);
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

// Add to favorites
function addToFavorites(productId) {
    let favorites = JSON.parse(localStorage.getItem('favorites')) || [];
    
    // Favorilerde varsa çıkar, yoksa ekle
    const index = favorites.indexOf(productId);
    if (index === -1) {
        favorites.push(productId);
        showNotification('Ürün favorilere eklendi!');
    } else {
        favorites.splice(index, 1);
        showNotification('Ürün favorilerden çıkarıldı!');
    }
    
    localStorage.setItem('favorites', JSON.stringify(favorites));
    
    // Favorileri güncelle
    checkFavorites();
}

// Favorileri kontrol et ve işaretle
function checkFavorites() {
    const favorites = JSON.parse(localStorage.getItem('favorites')) || [];
    const favoriteButtons = document.querySelectorAll('.add-to-favorites');
    
    favoriteButtons.forEach(button => {
        const productId = button.getAttribute('onclick').split("'")[1];
        if (favorites.includes(productId)) {
            button.innerHTML = '<i class="fas fa-heart"></i>';
            button.classList.add('active');
            button.title = 'Favorilerden Çıkar';
        } else {
            button.innerHTML = '<i class="far fa-heart"></i>';
            button.classList.remove('active');
            button.title = 'Favorilere Ekle';
        }
    });
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
