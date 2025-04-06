// DOM Elements
const featuredProducts = document.getElementById('featured-products');
const cartCount = document.querySelector('.cart-count');
const newsletterForm = document.querySelector('.newsletter-form');
const categoryFilters = document.querySelectorAll('.category-filter');

// Cart State
let cart = JSON.parse(localStorage.getItem('cart')) || [];
updateCartCount();

// Fetch Featured Products
async function fetchFeaturedProducts() {
    try {
        const response = await fetch('/api/products/featured');
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        console.error('Error fetching featured products:', error);
    }
}

// Fetch Products by Category
async function fetchProductsByCategory(category) {
    try {
        // Kategori için normal sorgu yap
        const response = await fetch(`/api/products/category/${category}`);
        
        if (!response.ok) {
            throw new Error('Kategori ürünleri yüklenirken hata oluştu');
        }
        
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        console.error('Ürünleri getirirken hata oluştu:', error);
        displayError('Ürünler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.');
    }
}

// Display Products
function displayProducts(products) {
    featuredProducts.innerHTML = products.map(product => `
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
                <button onclick="addToCart('${product._id}')" class="add-to-cart" ${product.stock === 0 ? 'disabled' : ''}>
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
        'Kadın': 'Kadın',
        'Erkek': 'Erkek'
    };
    
    return categoryDisplayNames[category] || category;
}

// Add to Cart
function addToCart(productId) {
    cart.push(productId);
    localStorage.setItem('cart', JSON.stringify(cart));
    updateCartCount();
    showNotification('Ürün sepete eklendi!');
}

// Update Cart Count
function updateCartCount() {
    const cartCount = document.querySelector('.cart-count');
    if (cartCount) {
        cartCount.textContent = cart.length;
    }
}

// Show Notification
function showNotification(message) {
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.textContent = message;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Category Filter Event Listeners
categoryFilters.forEach(filter => {
    filter.addEventListener('click', (e) => {
        e.preventDefault();
        const category = filter.getAttribute('data-category');
        
        // Update active state
        categoryFilters.forEach(f => f.classList.remove('active'));
        filter.classList.add('active');
        
        // Fetch products
        if (category === 'all') {
            fetchFeaturedProducts();
        } else {
            fetchProductsByCategory(category);
        }
    });
});

// Newsletter Form Submit
newsletterForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = newsletterForm.querySelector('input[type="email"]').value;

    try {
        const response = await fetch('/api/newsletter/subscribe', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email })
        });

        if (response.ok) {
            showNotification('Bültene başarıyla kayıt oldunuz!');
            newsletterForm.reset();
        } else {
            throw new Error('Kayıt işlemi başarısız oldu.');
        }
    } catch (error) {
        showNotification('Bir hata oluştu. Lütfen tekrar deneyin.');
    }
});

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

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchFeaturedProducts();
});
