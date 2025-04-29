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

// Ana sayfa arama formunu ayarla
document.addEventListener('DOMContentLoaded', function() {
    // İşlevler tanımlanmadan önce global olarak erişilebilir olduğundan emin ol
    window.goToProduct = function(productId) {
        window.location.href = `/views/product.html?id=${productId}`;
    };

    window.performMainSearch = performMainSearch;
    
    const mainSearchForm = document.getElementById('main-search-form');
    const mainSearchInput = document.getElementById('main-search-input');
    const searchSuggestions = document.getElementById('search-suggestions');
    
    if (mainSearchForm && mainSearchInput) {
        console.log('Arama formu bulundu, olaylar ayarlanıyor...');
        
        // Arama formu submit olduğunda
        mainSearchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const query = mainSearchInput.value.trim();
            
            if (query) {
                performMainSearch(query);
                // Önerileri kapat
                searchSuggestions.classList.remove('active');
            }
        });
        
        // Canlı arama özelliği
        let searchTimeout;
        
        mainSearchInput.addEventListener('input', function() {
            const query = this.value.trim();
            console.log('Arama girişi:', query);
            
            // Timeout temizle (typing durduktan sonra istek yap)
            clearTimeout(searchTimeout);
            
            if (query.length < 2) {
                searchSuggestions.classList.remove('active');
                return;
            }
            
            // 300ms gecikme ile arama yap (çok fazla istek göndermemek için)
            searchTimeout = setTimeout(() => {
                fetchLiveSearchResults(query);
            }, 300);
        });
        
        // Öneriler dışında bir yere tıklandığında önerileri kapat
        document.addEventListener('click', function(e) {
            if (!mainSearchForm.contains(e.target) && !searchSuggestions.contains(e.target)) {
                searchSuggestions.classList.remove('active');
            }
        });
        
        // ESC tuşu ile önerileri kapat
        mainSearchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                searchSuggestions.classList.remove('active');
            }
        });
    } else {
        console.log('Arama formu bulunamadı!', {
            mainSearchForm,
            mainSearchInput,
            searchSuggestions
        });
    }
});

// Canlı arama sonuçlarını getir
async function fetchLiveSearchResults(query) {
    console.log('Canlı arama başlatıldı:', query);
    const searchSuggestions = document.getElementById('search-suggestions');
    
    if (!searchSuggestions) {
        console.error('Arama önerileri konteynerı bulunamadı!');
        return;
    }
    
    try {
        const apiUrl = `/api/products/search?query=${encodeURIComponent(query)}`;
        console.log('API çağrısı yapılıyor:', apiUrl);
        
        const response = await fetch(apiUrl);
        console.log('API yanıtı alındı, durum kodu:', response.status);
        
        if (!response.ok) {
            throw new Error(`API error: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Arama sonuçları:', data);
        
        // En fazla 5 öneri göster
        const maxSuggestions = Math.min(5, data.length);
        const suggestions = data.slice(0, maxSuggestions);
        
        if (suggestions.length === 0) {
            searchSuggestions.classList.remove('active');
            return;
        }
        
        // Önerileri oluştur
        let suggestionsHTML = '';
        
        suggestions.forEach(product => {
            // Kategori için ikon belirle
            let categoryIcon = 'tshirt'; // Varsayılan ikon
            
            if (product.category === 'Ayakkabı') {
                categoryIcon = 'shoe-prints';
            } else if (product.category === 'Aksesuar') {
                categoryIcon = 'hat-cowboy';
            }
            
            const price = product.price ? `₺${product.price.toFixed(2)}` : 'Fiyat bilgisi yok';
            
            // Görseller için image veya imagePath özelliğini kontrol et
            const productImage = product.imagePath || product.image || null;
            
            suggestionsHTML += `
                <div class="suggestion-item" data-id="${product._id}" onclick="goToProduct('${product._id}')">
                    <div class="suggestion-img">
                        ${productImage 
                            ? `<img src="${productImage}" alt="${product.name}" onerror="this.onerror=null; this.src='/public/images/products/default.jpg';">` 
                            : `<i class="fas fa-${categoryIcon}"></i>`
                        }
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
        
        // Tüm sonuçları göster butonu
        if (data.length > maxSuggestions) {
            const escapedQuery = query.replace(/'/g, "\\'");
            suggestionsHTML += `
                <div class="search-all-results" onclick="performMainSearch('${escapedQuery}')">
                    Tüm sonuçları gör (${data.length}) <i class="fas fa-arrow-right"></i>
                </div>
            `;
        }
        
        searchSuggestions.innerHTML = suggestionsHTML;
        searchSuggestions.classList.add('active');
        
    } catch (error) {
        console.error('Canlı arama hatası:', error);
        searchSuggestions.classList.remove('active');
    }
}

// Ana sayfadan arama yapma fonksiyonu
async function performMainSearch(query) {
    console.log('Ana sayfa araması başlatıldı:', query);
    
    // Search results container oluştur veya mevcut olanı al
    let searchResults = document.getElementById('inline-search-results');
    
    if (!searchResults) {
        searchResults = document.createElement('section');
        searchResults.id = 'inline-search-results';
        searchResults.className = 'search-results-section';
        
        // Hero section'dan sonra yerleştir
        const heroSection = document.querySelector('.hero');
        if (heroSection) {
            heroSection.parentNode.insertBefore(searchResults, heroSection.nextSibling);
        } else {
            document.body.appendChild(searchResults);
        }
    }
    
    // Loading durumunu göster
    searchResults.innerHTML = `
        <div class="search-results-container">
            <h2>Arama Sonuçları: "${query}"</h2>
            <div class="loading"><i class="fas fa-spinner fa-spin"></i> Aranıyor...</div>
        </div>
    `;
    
    // Search results'ı görünür yap
    searchResults.style.display = 'block';
    
    // Sonuçlara kaydır
    searchResults.scrollIntoView({ behavior: 'smooth', block: 'start' });
    
    try {
        const apiUrl = `/api/products/search?query=${encodeURIComponent(query)}`;
        console.log('API çağrısı yapılıyor:', apiUrl);
        
        const response = await fetch(apiUrl);
        console.log('API yanıtı alındı, durum kodu:', response.status);
        
        if (!response.ok) {
            throw new Error(`Arama API hatası: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('Arama sonuçları:', data, 'Sonuç sayısı:', data.length);
        
        let resultsHTML;
        
        if (data.length === 0) {
            resultsHTML = `
                <div class="search-results-container">
                    <h2>Arama Sonuçları: "${query}"</h2>
                    <div class="no-results">
                        <i class="fas fa-search"></i>
                        <p>"${query}" için sonuç bulunamadı</p>
                        <p class="search-tip">Farklı anahtar kelimeler deneyebilir veya kategori ismi ile arama yapabilirsiniz.</p>
                    </div>
                </div>
            `;
        } else {
            resultsHTML = `
                <div class="search-results-container">
                    <h2>Arama Sonuçları: "${query}" (${data.length} ürün)</h2>
                    <div class="results-grid">
            `;
            
            data.forEach(product => {
                // Kategori için ikon belirle
                let categoryIcon = 'tshirt'; // Varsayılan ikon
                
                if (product.category === 'Ayakkabı') {
                    categoryIcon = 'shoe-prints';
                } else if (product.category === 'Aksesuar') {
                    categoryIcon = 'hat-cowboy';
                }
                
                // Kullanıcı-dostu kategori metnini oluştur
                let categoryText = product.category || 'Kategori belirtilmemiş';
                if (product.subcategory) {
                    categoryText += ` > ${product.subcategory}`;
                }
                
                const price = product.price ? `₺${product.price.toFixed(2)}` : 'Fiyat bilgisi yok';
                // Görseller için image veya imagePath özelliğini kontrol et
                const productImage = product.imagePath || product.image || null;
                
                resultsHTML += `
                    <div class="product-card">
                        <a href="/views/product.html?id=${product._id}" class="product-link">
                            <div class="product-img-container">
                                ${productImage 
                                    ? `<img src="${productImage}" alt="${product.name}" loading="lazy" onerror="this.onerror=null; this.src='/public/images/products/default.jpg';">` 
                                    : `<div class="product-img-placeholder"><i class="fas fa-${categoryIcon} fa-3x"></i></div>`
                                }
                            </div>
                            <div class="product-card-content">
                                <h4 class="product-title">${product.name}</h4>
                                <span class="product-category">${categoryText}</span>
                                <p class="price">${price}</p>
                                <div class="product-actions">
                                    <button class="view-product">Ürüne Git <i class="fas fa-arrow-right"></i></button>
                                </div>
                            </div>
                        </a>
                    </div>
                `;
            });
            
            resultsHTML += `
                    </div>
                </div>
            `;
        }
        
        searchResults.innerHTML = resultsHTML;
        
        // Add event listeners to view product buttons
        searchResults.querySelectorAll('.view-product').forEach(button => {
            button.addEventListener('click', (e) => {
                // Burada bir şey yapmaya gerek yok çünkü parent <a> zaten ürün sayfasına yönlendirecek
                e.stopPropagation(); // Olası çift tıklama sorunlarını önle
            });
        });
        
    } catch (error) {
        console.error('Search error details:', error);
        searchResults.innerHTML = `
            <div class="search-results-container">
                <h2>Arama Sonuçları: "${query}"</h2>
                <div class="error">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>Arama sırasında bir hata oluştu: ${error.message}</p>
                    <p>Lütfen daha sonra tekrar deneyin.</p>
                </div>
            </div>
        `;
    }
}
