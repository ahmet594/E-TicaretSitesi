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
        console.log('🚀 Öne çıkan ürünler yükleniyor...');
        const featuredProductsContainer = document.getElementById('featured-products');
        
        if (!featuredProductsContainer) {
            console.error('⚠️ featured-products elementi bulunamadı!');
            return;
        }
        
        // Yükleniyor göstergesi
        featuredProductsContainer.innerHTML = `
            <div class="loading">
                <i class="fas fa-spinner fa-spin"></i> Ürünler yükleniyor...
            </div>
        `;
        
        const response = await fetch('/api/products/featured');
        
        if (!response.ok) {
            throw new Error(`Öne çıkan ürünler yüklenirken hata oluştu: ${response.status}`);
        }
        
        const products = await response.json();
        console.log(`✅ ${products.length} adet öne çıkan ürün bulundu`);
        
        if (products.length === 0) {
            featuredProductsContainer.innerHTML = `
                <div style="text-align: center; padding: 30px;">
                    <p>Henüz öne çıkan ürün bulunmuyor.</p>
                </div>
            `;
            return;
        }
        
        displayProducts(products);
    } catch (error) {
        console.error('❌ Öne çıkan ürünleri getirme hatası:', error);
        
        const featuredProductsContainer = document.getElementById('featured-products');
        if (featuredProductsContainer) {
            featuredProductsContainer.innerHTML = `
                <div style="text-align: center; padding: 30px;">
                    <p>Ürünler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.</p>
                </div>
            `;
        }
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
    console.log('🎯 Ürünler DOM\'a ekleniyor:', products.length);
    
    const featuredProductsContainer = document.getElementById('featured-products');
    if (!featuredProductsContainer) {
        console.error('⚠️ Ürün gösterilecek konteyner bulunamadı!');
        return;
    }
    
    try {
        // Ürün kartlarını oluştur
        const productsHTML = products.map(product => {
            // Ürün resmi kontrolü
            const productImage = product.image || '../img/product-placeholder.jpg';
            
            // Fiyat formatı
            const price = product.price 
                ? product.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' }) 
                : 'Fiyat bilgisi yok';
                
            return `
                <div class="product-card">
                    <a href="/views/product.html?id=${product._id}" class="product-link">
                        <div class="product-img-container">
                            <img src="${productImage}" alt="${product.name}" 
                                 onerror="this.onerror=null; this.src='../img/product-placeholder.jpg';">
                            ${product.stock <= 3 && product.stock > 0 ? `<span class="product-badge limited-stock">Sınırlı Stok</span>` : ''}
                            ${product.stock === 0 ? `<span class="product-badge out-of-stock-badge">Tükendi</span>` : ''}
                            ${product.featured ? `<span class="product-badge featured-badge">Öne Çıkan</span>` : ''}
                        </div>
                        <div class="product-card-content">
                            <div class="product-brand">${product.brand || 'SmartWear'}</div>
                            <h3 class="product-title">${product.name}</h3>
                            <div class="product-details">
                                ${product.color ? `<div class="product-color">Renk: ${product.color}</div>` : ''}
                                ${product.size ? `<div class="product-size">Beden: ${product.size}</div>` : ''}
                            </div>
                            <div class="product-price-container">
                                <p class="price">${price}</p>
                                <p class="stock ${product.stock > 0 ? 'in-stock' : 'out-of-stock'}">
                                    ${product.stock > 0 ? `${product.stock} adet stokta` : 'Stokta Yok'}
                                </p>
                            </div>
                        </div>
                    </a>
                    <div class="product-actions">
                        <button class="sepete-ekle-btn" onclick="addToCartFromListing('${product._id}', '${product.name}', ${product.price}, '${product.image}')" ${product.stock === 0 ? 'disabled' : ''}>
                            <i class="fas fa-shopping-cart"></i>
                            <span>Sepete Ekle</span>
                        </button>
                        <button class="favorite-btn" onclick="toggleFavorite('${product._id}')">
                            <i class="far fa-heart"></i>
                        </button>
                    </div>
                </div>
            `;
        }).join('');
        
        // DOM'a ekleme
        featuredProductsContainer.innerHTML = productsHTML;
        
        // Favorileri kontrol edip işaretle
        setTimeout(() => {
            checkFavorites();
        }, 100);
        
        console.log('✅ Ürünler başarıyla gösterildi');
    } catch (error) {
        console.error('❌ Ürünleri gösterirken hata oluştu:', error);
        featuredProductsContainer.innerHTML = `
            <div style="text-align: center; padding: 30px;">
                <p>Ürünler gösterilirken bir hata oluştu: ${error.message}</p>
            </div>
        `;
    }
}

// Kategorilerin görünen adlarını döndürür
function getCategoryDisplayName(category) {
    const categoryDisplayNames = {
        'Giyim': 'Giyim',
        'Ayakkabı': 'Ayakkabı',
        'Aksesuar': 'Aksesuar'
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
    const favoriteButtons = document.querySelectorAll('.favorite-btn');
    
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

// Global product function
window.goToProduct = function(productId) {
    console.log('🔗 Ürün sayfasına yönlendiriliyor:', productId);
    window.location.href = `/views/product.html?id=${productId}`;
};

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('🚀 Sayfa yükleniyor...');
    // Arama işlevselliğini kur
    setTimeout(() => {
        setupSearchFunctionality();
    }, 1000);
});

// Sayfa tamamen yüklendiğinde
window.onload = function() {
    console.log('📱 Sayfa tamamen yüklendi - İçerik başlatılıyor');
    
    // Öne çıkan ürünleri yükle
    fetchFeaturedProducts();
    
    // Arama inputunu kontrol et (eski arama kutusu yoksa görmezden gelir)
    const mainSearchInput = document.getElementById('main-search-input');
    if (mainSearchInput) {
        console.log('✅ Ana arama input elementi bulundu');
        mainSearchInput.addEventListener('input', handleSearchInput);
        
        if (mainSearchInput.value.trim().length >= 2) {
            handleSearchInput.call(mainSearchInput);
        }
    } else {
        console.log('ℹ️ Ana sayfada arama elementi bulunmuyor - normal davranış');
    }
};

// Basitleştirilmiş arama işlevi - Direkt global tanımla
function handleSearchInput() {
    const query = this.value.trim();
    const searchContainer = document.querySelector('.search-container');
    let searchSuggestions = document.getElementById('search-suggestions');
    
    console.log('🔍 Arama girişi:', query);
    
    // Eğer öneriler bulunamazsa, yeni oluştur (olmaması durumunda)
    if (!searchSuggestions && searchContainer) {
        console.log('⚠️ Öneriler elementi bulunamadı, yeniden oluşturuluyor...');
        searchSuggestions = document.createElement('div');
        searchSuggestions.id = 'search-suggestions';
        searchSuggestions.className = 'search-suggestions';
        searchContainer.appendChild(searchSuggestions);
    }
    
    if (!searchSuggestions) {
        console.error('❌ Arama önerileri elementi bulunamadı ve oluşturulamadı!');
        return;
    }
    
    // 2 karakterden az ise işlem yapma
    if (query.length < 2) {
        searchSuggestions.classList.remove('active');
        return;
    }
    
    // Yükleniyor göster
    searchSuggestions.innerHTML = '<div class="suggestion-item"><p><i class="fas fa-spinner fa-spin"></i> Aranıyor...</p></div>';
    searchSuggestions.classList.add('active');
    
    // API'ye istek gönder
    fetch(`/api/products/live-search?q=${encodeURIComponent(query)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`API hatası: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log(`✅ ${data.length} adet sonuç bulundu`);
            
            if (data.length === 0) {
                searchSuggestions.innerHTML = `
                    <div class="suggestion-item no-results">
                        <p>"${query}" için sonuç bulunamadı</p>
                    </div>
                `;
            } else {
                // Önerileri oluştur
                let suggestionsHTML = '';
                
                // Her ürün için bir öneri kalemi oluştur
                data.forEach(product => {
                    const productImage = product.image || '../img/product-placeholder.jpg';
                    const price = product.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' });
                    
                    suggestionsHTML += `
                        <div class="suggestion-item" onclick="goToProduct('${product._id}')">
                            <div class="suggestion-img">
                                <img src="${productImage}" alt="${product.name}" onerror="this.onerror=null; this.src='../img/product-placeholder.jpg';">
                            </div>
                            <div class="suggestion-content">
                                <div class="suggestion-title">${product.name}</div>
                                <div class="suggestion-details">
                                    <span class="suggestion-category">${product.category || ''}</span>
                                    <span class="suggestion-price">${price}</span>
                                </div>
                            </div>
                        </div>
                    `;
                });
                
                // "Tüm sonuçları gör" bağlantısı ekle
                suggestionsHTML += `
                    <div class="search-all-results" onclick="window.location.href='/views/search.html?q=${encodeURIComponent(query)}'">
                        Tüm sonuçları gör <i class="fas fa-arrow-right"></i>
                    </div>
                `;
                
                searchSuggestions.innerHTML = suggestionsHTML;
            }
            
            // Önerileri göster
            searchSuggestions.classList.add('active');
        })
        .catch(error => {
            console.error('❌ Arama hatası:', error);
            searchSuggestions.innerHTML = `
                <div class="suggestion-item error">
                    <p>Arama sırasında bir hata oluştu: ${error.message}</p>
                </div>
            `;
            searchSuggestions.classList.add('active');
        });
    
    // Dokümana tıklama olayını ekle (eğer yoksa)
    if (!window.searchClickHandlerAdded) {
        document.addEventListener('click', function(event) {
            const searchSuggestions = document.getElementById('search-suggestions');
            const mainSearchForm = document.getElementById('main-search-form');
            
            if (searchSuggestions && mainSearchForm) {
                if (!mainSearchForm.contains(event.target) && !searchSuggestions.contains(event.target)) {
                    searchSuggestions.classList.remove('active');
                }
            }
        });
        window.searchClickHandlerAdded = true;
    }
}
