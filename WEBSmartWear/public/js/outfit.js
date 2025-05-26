// Seçili ürünleri ve önerileri yönetecek değişkenler
let selectedProducts = [];
let suggestedOutfits = [];

// DOM elementleri
const categoryFilter = document.getElementById('category-filter');
const colorFilter = document.getElementById('color-filter');
const styleFilter = document.getElementById('style-filter');
const applyFiltersBtn = document.getElementById('apply-filters');
const productsGrid = document.getElementById('products-grid');
const outfitSlots = document.querySelectorAll('.outfit-slot');
const saveOutfitBtn = document.getElementById('save-outfit');
const clearOutfitBtn = document.getElementById('clear-outfit');

// Mevsim belirleme fonksiyonu
function determineSeason(products) {
    const productNames = products.map(p => p.name.toLowerCase());
    const productCategories = products.map(p => (p.category || '').toLowerCase());
    
    // Kış kontrolü: Bot, mont, kaban, trençkot, palto, coat
    const winterKeywords = ['bot', 'mont', 'kaban', 'parka', 'trençkot', 'trenckot', 'palto', 'coat', 'kışlık'];
    
    // Debug için ürün isimlerini ve kategorilerini kontrol et
    console.log('=== MEVSIM DEBUG ===');
    console.log('Ürün isimleri:', productNames);
    console.log('Ürün kategorileri:', productCategories);
    
    const hasWinterItems = productNames.some(name => {
        const isWinter = winterKeywords.some(keyword => name.includes(keyword));
        if (isWinter) {
            console.log(`KIŞ ÜRÜNÜ BULUNDU - İsim: "${name}"`);
        }
        return isWinter;
    }) || productCategories.some(category => {
        const isWinter = winterKeywords.some(keyword => category.includes(keyword));
        if (isWinter) {
            console.log(`KIŞ ÜRÜNÜ BULUNDU - Kategori: "${category}"`);
        }
        return isWinter;
    });
    
    console.log('Kış ürünü var mı?', hasWinterItems);
    console.log('=================');
    
    if (hasWinterItems) return 'winter';
    
    // Bahar kontrolü: Hırka, sweatshirt, ceket, yelek
    const springKeywords = ['hırka', 'sweatshirt', 'ceket', 'yelek', 'hirka'];
    const hasSpringItems = productNames.some(name => 
        springKeywords.some(keyword => name.includes(keyword))
    ) || productCategories.some(category => 
        springKeywords.some(keyword => category.includes(keyword))
    );
    
    if (hasSpringItems) return 'spring';
    
    // CombinationCode kontrolü - combinationCode 5 olan ürün varsa bahar
    const hasCode5 = products.some(p => p.combinationCode === 5 || p.combinationCode === "5");
    if (hasCode5) return 'spring';
    
    // Subcategory kontrolü - subcategory "kazak" olan ürün varsa bahar
    const hasKazak = products.some(p => (p.subcategory || '').toLowerCase() === 'kazak');
    if (hasKazak) return 'spring';
    
    // Geri kalan her şey yaz
    return 'summer';
}

// Mevsim adlarını Türkçe'ye çevir
function getSeasonName(season) {
    const seasonNames = {
        'winter': 'Kış',
        'spring': 'Bahar',
        'summer': 'Yaz'
    };
    return seasonNames[season] || 'Belirsiz';
}

// DOM yüklendiğinde
document.addEventListener('DOMContentLoaded', () => {
    // Navbar'ı yükle
    loadNavbar();
    
    // URL'den seçili ürün ID'lerini al
    const urlParams = new URLSearchParams(window.location.search);
    const productIds = urlParams.get('products');
    
    if (productIds) {
        // Seçili ürünleri getir ve göster
        fetchSelectedProducts(productIds.split(','));
    } else {
        // LocalStorage'dan seçili ürünleri al ve görüntüle
        loadSelectedProducts();
    }

    // Kombinle butonuna tıklama olayını ekle
    const kombinleBtn = document.querySelector('.kombinle-btn');
    if (kombinleBtn) {
        kombinleBtn.addEventListener('click', () => {
            if (selectedProducts.length > 0) {
                findMatchingOutfits();
            }
        });
    }
    
    // Mevsim filtresi event listener'ı ekle
    const seasonFilter = document.getElementById('season-filter');
    if (seasonFilter) {
        seasonFilter.addEventListener('change', applySeasonFilter);
    }
});

// Navbar'ı yükle
function loadNavbar() {
    fetch('../components/navbar.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('navbar-container').innerHTML = data;
            if (typeof updateNavbarAuthUI === 'function') {
                updateNavbarAuthUI();
            }
        });
}

// LocalStorage'dan seçili ürünleri yükle ve görüntüle
function loadSelectedProducts() {
    const storedProducts = JSON.parse(localStorage.getItem('outfitProducts') || '[]');
    
    if (storedProducts.length > 0) {
        selectedProducts = storedProducts;
        displaySelectedProducts();
    } else {
        displayInitialState();
    }
}

// Seçili ürünleri gösteren fonksiyon
function displaySelectedProducts() {
    const selectedProductsList = document.getElementById('selected-products-list');
    const productSuggestions = document.querySelector('.product-suggestions');
    
    if (!selectedProducts || selectedProducts.length === 0) {
        selectedProductsList.innerHTML = `
            <div class="no-products-message">
                <i class="fas fa-tshirt"></i>
                <p>Henüz ürün seçilmedi</p>
                <small>Kombin oluşturmak için ürün seçmeye başlayın.</small>
            </div>
        `;
        productSuggestions.classList.remove('visible');
        return;
    }
    
    productSuggestions.classList.add('visible');
    
    selectedProductsList.innerHTML = selectedProducts.map(product => `
        <div class="selected-product-card" data-id="${product.id}">
            <button class="remove-btn" onclick="removeFromOutfit('${product.id}'); event.stopPropagation();">
                <i class="fas fa-times"></i>
            </button>
            <div onclick="window.location.href='/views/product.html?id=${product.id}';" style="cursor: pointer;">
                <img src="${product.image}" alt="${product.name}">
                <div class="product-info">
                    <h4>${product.name}</h4>
                    <p>${typeof product.price === 'number' ? product.price.toFixed(2) : product.price} TL</p>
                </div>
            </div>
        </div>
    `).join('');
}

// Kombine ürün ekle
function addToOutfit(product) {
    // Ürün zaten ekliyse ekleme
    if (selectedProducts.some(p => p.id === product.id)) {
        showNotification('Bu ürün zaten kombinde mevcut', 'info');
        return;
    }

    // Aynı kombinasyon koduna sahip başka bir ürün var mı kontrol et
    if (product.combinationCode && selectedProducts.some(p => p.combinationCode === product.combinationCode)) {
        showNotification('Bu türde bir ürün zaten seçilmiş', 'warning');
        return;
    }
    
    selectedProducts.push(product);
    localStorage.setItem('outfitProducts', JSON.stringify(selectedProducts));
    displaySelectedProducts();
}

// Kombinden ürün çıkar
function removeFromOutfit(productId) {
    selectedProducts = selectedProducts.filter(product => product.id !== productId);
    localStorage.setItem('outfitProducts', JSON.stringify(selectedProducts));
    displaySelectedProducts();
    
    if (selectedProducts.length === 0) {
        // Hiç ürün kalmadıysa kombin önerilerini temizle ve gizle
        const suggestedProductsContainer = document.getElementById('suggested-products');
        const productSuggestions = document.querySelector('.product-suggestions');
        suggestedProductsContainer.innerHTML = '';
        productSuggestions.classList.remove('visible');
    }
}

// Seçili ürünleri getiren fonksiyon
async function fetchSelectedProducts(productIds) {
    try {
        const response = await fetch(`/api/products?ids=${productIds.join(',')}`);
        const products = await response.json();
        selectedProducts = products;
        displaySelectedProducts();
    } catch (error) {
        console.error('Seçili ürünler getirilirken hata oluştu:', error);
    }
}

// Kombindeki tüm ürünleri sepete ekle
async function addOutfitToCart(products) {
    try {
        // Her ürünü sırayla sepete ekle
        for (const product of products) {
            if (product.stock > 0) {
                await addToCart(product._id, product.name, product.price, product.image);
                // Küçük bir gecikme ekle
                await new Promise(resolve => setTimeout(resolve, 100));
            }
        }
        showNotification('Kombin sepete eklendi', 'success');
    } catch (error) {
        console.error('Kombin sepete eklenirken hata:', error);
        showNotification('Kombin sepete eklenirken hata oluştu', 'error');
    }
}

// Eşleşen kombinleri bulan fonksiyon
async function findMatchingOutfits() {
    const suggestedProductsContainer = document.getElementById('suggested-products');

    // Seçili ürün kontrolü
    if (!selectedProducts || selectedProducts.length === 0) {
        suggestedProductsContainer.innerHTML = `
            <div class="no-match-message">
                <i class="fas fa-shopping-bag"></i>
                <p>Kombin önerileri için lütfen ürün seçin.</p>
                <small>Ürün seçtikten sonra size uygun kombinler burada listelenecektir.</small>
            </div>
        `;
        return;
    }

    suggestedProductsContainer.innerHTML = `
        <div class="loading-message">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Kombinler getiriliyor...</p>
        </div>
    `;

    try {
        // Seçili ürünlerin ID'lerini al
        const selectedIds = selectedProducts.map(p => p.id);
        console.log('Seçili ürün ID\'leri:', selectedIds);

        // Tüm kombinleri getir
        const response = await fetch('/api/outfits');
        const allOutfits = await response.json();
        console.log('Tüm kombinler:', allOutfits);

        // Kombinleri eşleşme durumuna göre filtrele ve grupla
        const perfectMatches = []; // Tüm seçili ürünleri içeren kombinler
        const partialMatches = []; // Seçili ürünlerden bazılarını içeren kombinler

        // Her ürün için eşleşme sayısını tut
        const productMatchCounts = {};
        selectedIds.forEach(id => {
            productMatchCounts[id] = 0;
        });

        allOutfits.forEach(outfit => {
            const matchingIds = outfit.productIds.filter(id => selectedIds.includes(id));
            
            // Her eşleşen ürünün sayacını artır
            matchingIds.forEach(id => {
                productMatchCounts[id] = (productMatchCounts[id] || 0) + 1;
            });

            if (matchingIds.length === selectedIds.length && 
                selectedIds.every(id => outfit.productIds.includes(id))) {
                perfectMatches.push({
                    ...outfit,
                    matchCount: matchingIds.length,
                    matchingIds
                });
            } else if (matchingIds.length > 0) {
                partialMatches.push({
                    ...outfit,
                    matchCount: matchingIds.length,
                    matchingIds
                });
            }
        });

        // En az eşleşen ürünü bul
        if (perfectMatches.length === 0 && partialMatches.length > 0) {
            let minMatchCount = Infinity;
            let leastMatchedProductId = null;
            let leastMatchedProduct = null;

            // En az eşleşen ürünü bul
            Object.entries(productMatchCounts).forEach(([productId, count]) => {
                if (count < minMatchCount) {
                    minMatchCount = count;
                    leastMatchedProductId = productId;
                    leastMatchedProduct = selectedProducts.find(p => p.id === productId);
                }
            });

            // En az eşleşen ürünü işaretle
            if (leastMatchedProductId) {
                const selectedProductCards = document.querySelectorAll('.selected-product-card');
                selectedProductCards.forEach(card => {
                    if (card.dataset.id === leastMatchedProductId) {
                        card.classList.add('least-matched');
                        
                        // Açıklama mesajını ekle
                        const warningMessage = document.createElement('div');
                        warningMessage.className = 'matching-warning';
                        warningMessage.innerHTML = `
                            <div class="warning-content">
                                <i class="fas fa-exclamation-circle"></i>
                                <p><strong>${leastMatchedProduct.name}</strong> ürünü diğer seçimlerinizle daha az eşleşme sağlıyor.</p>
                                <small>Bu ürünü değiştirerek daha fazla kombin seçeneği bulabilirsiniz.</small>
                            </div>
                        `;
                        
                        // Mesajı seçili ürünler bölümünün altına ekle
                        const selectedProducts = document.querySelector('.selected-products');
                        const existingWarning = selectedProducts.querySelector('.matching-warning');
                        if (existingWarning) {
                            existingWarning.remove();
                        }
                        selectedProducts.appendChild(warningMessage);
                    } else {
                        card.classList.remove('least-matched');
                    }
                });
            }
        } else {
            // Tam eşleşme varsa kırmızı işaretlemeyi ve uyarı mesajını kaldır
            const selectedProductCards = document.querySelectorAll('.selected-product-card');
            selectedProductCards.forEach(card => {
                card.classList.remove('least-matched');
            });
            const existingWarning = document.querySelector('.matching-warning');
            if (existingWarning) {
                existingWarning.remove();
            }
        }

        // Sadece tam eşleşen kombinleri göster
        console.log('Tam eşleşen kombinler:', perfectMatches);
        console.log('Kısmi eşleşen kombinler:', partialMatches);

        if (perfectMatches.length > 0) {
            // Tam eşleşen kombinlerin ürün ID'lerini topla
            const allProductIds = [...new Set(perfectMatches.flatMap(outfit => outfit.productIds))];
            
            // Tüm ürünlerin detaylarını getir
            const productsResponse = await fetch(`/api/products?ids=${allProductIds.join(',')}`);
            const productsData = await productsResponse.json();
            const products = productsData.products || [];

            // Kombinleri mevsimlere göre grupla
            const seasonGroups = {
                winter: [],
                spring: [],
                summer: []
            };

            perfectMatches.forEach(outfit => {
                // Bu kombindeki ürünlerin detaylarını bul
                const outfitProducts = outfit.productIds
                    .map(productId => products.find(p => p._id === productId))
                    .filter(Boolean);
                
                // Bu kombinun mevsimini belirle
                const season = determineSeason(outfitProducts);
                
                seasonGroups[season].push({
                    ...outfit,
                    products: outfitProducts,
                    season: season
                });
            });

            // Mevsim filtresi kontrolü ekle
            const seasonFilterContainer = `
                <div class="season-filter-container">
                    <label for="season-filter">Mevsime göre filtrele:</label>
                    <select id="season-filter" class="season-filter">
                        <option value="all">Tüm Mevsimler</option>
                        <option value="winter">Kış (${seasonGroups.winter.length})</option>
                        <option value="spring">Bahar (${seasonGroups.spring.length})</option>
                        <option value="summer">Yaz (${seasonGroups.summer.length})</option>
                    </select>
                </div>
            `;

            // Her kombinim ürün kartları ile göster
            suggestedProductsContainer.innerHTML = seasonFilterContainer + `
                <div class="outfits-list" id="outfits-list">
                    ${Object.entries(seasonGroups).map(([season, outfits]) => {
                        if (outfits.length === 0) return '';
                        
                        return `
                            <div class="season-group" data-season="${season}">
                                <h3 class="season-title">
                                    <i class="fas ${season === 'winter' ? 'fa-snowflake' : season === 'spring' ? 'fa-leaf' : 'fa-sun'}"></i>
                                    ${getSeasonName(season)} Kombinleri (${outfits.length})
                                </h3>
                                <div class="season-outfits">
                                    ${outfits.map((outfit, index) => {
                                        // Kombindeki ürünlerin toplam fiyatını hesapla
                                        const totalPrice = outfit.products.reduce((sum, product) => sum + product.price, 0);
                                        
                                        // Kombinde stokta olmayan ürün var mı kontrol et
                                        const hasOutOfStock = outfit.products.some(product => product.stock === 0);

                                        return `
                                            <div class="outfit-item ${outfit.matchCount === selectedIds.length ? 'perfect-match' : ''}">
                                                <div class="outfit-header">
                                                    <span class="season-badge season-${season}">
                                                        <i class="fas ${season === 'winter' ? 'fa-snowflake' : season === 'spring' ? 'fa-leaf' : 'fa-sun'}"></i>
                                                        ${getSeasonName(season)}
                                                    </span>
                                                    <span class="outfit-total-price">
                                                        Toplam: ${totalPrice.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}
                                                    </span>
                                                </div>
                                                <div class="outfit-products">
                                                    ${outfit.products.map(product => `
                                                        <div class="outfit-product-card ${selectedIds.includes(product._id) ? 'selected-product' : ''}" 
                                                             onclick="window.location.href='/views/product.html?id=${product._id}';" 
                                                             style="cursor: pointer;">
                                                            <div class="product-img-container">
                                                                <img src="${product.image}" alt="${product.name}" 
                                                                     onerror="this.onerror=null; this.src='../img/product-placeholder.jpg';">
                                                                ${product.stock <= 3 && product.stock > 0 ? 
                                                                    `<span class="product-badge limited-stock">Sınırlı Stok</span>` : ''}
                                                                ${product.stock === 0 ? 
                                                                    `<span class="product-badge out-of-stock-badge">Tükendi</span>` : ''}
                                                            </div>
                                                            <div class="product-info">
                                                                <div class="product-brand">${product.brand || 'SmartWear'}</div>
                                                                <h5 class="product-title">${product.name}</h5>
                                                                <div class="product-details">
                                                                    ${product.color ? 
                                                                        `<div class="product-color">Renk: ${product.color}</div>` : ''}
                                                                    ${product.size ? 
                                                                        `<div class="product-size">Beden: ${product.size}</div>` : ''}
                                                                </div>
                                                                <div class="product-price">
                                                                    ${product.price.toLocaleString('tr-TR', { 
                                                                        style: 'currency', 
                                                                        currency: 'TRY' 
                                                                    })}
                                                                </div>
                                                            </div>
                                                            <div class="product-actions" onclick="event.stopPropagation()">
                                                                <button class="add-to-cart-btn" 
                                                                        onclick="addToCartFromListing('${product._id}', '${product.name}', ${product.price}, '${product.image}')"
                                                                        ${product.stock === 0 ? 'disabled' : ''}>
                                                                    <i class="fas fa-shopping-cart"></i>
                                                                    Sepete Ekle
                                                                </button>
                                                            </div>
                                                        </div>
                                                    `).join('')}
                                                </div>
                                            </div>
                                        `;
                                    }).join('')}
                                </div>
                            </div>
                        `;
                    }).join('')}
                </div>
            `;

            // Mevsim filtresi event listener'ını ekle
            const seasonFilter = document.getElementById('season-filter');
            if (seasonFilter) {
                seasonFilter.addEventListener('change', applySeasonFilter);
            }

            // Bildirim mesajını hazırla
            let notificationMessage = '';
            const totalMatches = perfectMatches.length;
            if (totalMatches > 0) {
                notificationMessage = `${totalMatches} eşleşme bulundu (Kış: ${seasonGroups.winter.length}, Bahar: ${seasonGroups.spring.length}, Yaz: ${seasonGroups.summer.length})`;
                showNotification(notificationMessage, 'success');
            } else {
                notificationMessage = `Eşleşen kombin bulunamadı`;
                showNotification(notificationMessage, 'error');
            }
            
        } else {
            suggestedProductsContainer.innerHTML = `
                <div class="no-match-message">
                    <i class="fas fa-search"></i>
                    <p>Seçili ürünlerle eşleşen kombin bulunamadı.</p>
                </div>
            `;
            showNotification('Eşleşen kombin bulunamadı', 'info');
        }
    } catch (error) {
        console.error('Kombinler getirilirken hata oluştu:', error);
        suggestedProductsContainer.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-circle"></i>
                <p>Kombinler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.</p>
            </div>
        `;
        showNotification('Bir hata oluştu', 'warning');
    }
}

// Mevsim filtresi uygula
function applySeasonFilter() {
    const seasonFilter = document.getElementById('season-filter');
    const selectedSeason = seasonFilter.value;
    const seasonGroups = document.querySelectorAll('.season-group');

    seasonGroups.forEach(group => {
        const groupSeason = group.dataset.season;
        if (selectedSeason === 'all' || selectedSeason === groupSeason) {
            group.style.display = 'block';
        } else {
            group.style.display = 'none';
        }
    });
}

// Önerilen kombinleri gösteren fonksiyon
function displaySuggestedOutfits() {
    const suggestedProductsContainer = document.getElementById('suggested-products');
    
    if (suggestedOutfits.length === 0) {
        displayNoMatchMessage();
        return;
    }

    suggestedProductsContainer.innerHTML = suggestedOutfits.map((outfit, index) => `
        <div class="outfit-suggestion">
            <h4>
                <i class="fas fa-tshirt"></i>
                Kombin ${index + 1}
            </h4>
            <div class="outfit-products">
                ${outfit.products.map(product => `
                    <div class="outfit-product-card">
            <img src="${product.image}" alt="${product.name}">
            <div class="product-info">
                            <h5>${product.name}</h5>
                            <p>${typeof product.price === 'number' ? product.price.toFixed(2) : product.price} TL</p>
                        </div>
                    </div>
                `).join('')}
            </div>
        </div>
    `).join('');
}

// Eşleşme bulunamadığında gösterilecek mesaj
function displayNoMatchMessage() {
    const suggestedProductsContainer = document.getElementById('suggested-products');
    suggestedProductsContainer.innerHTML = `
        <div class="no-match-message">
            <i class="fas fa-search"></i>
            <p>Seçilen ürünlerle eşleşen kombin bulunamadı.</p>
            </div>
        `;
}

// Hata mesajını gösteren fonksiyon
function displayErrorMessage() {
    const suggestedProductsContainer = document.getElementById('suggested-products');
    suggestedProductsContainer.innerHTML = `
        <div class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            <p>Kombinler yüklenirken bir hata oluştu. Lütfen daha sonra tekrar deneyin.</p>
        </div>
    `;
}

// Gelişmiş bildirim fonksiyonu
function showNotification(message, type = 'success') {
    // Bildirim konteynerını kontrol et veya oluştur
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
    
    // Yeni bildirim oluştur
    const notification = document.createElement('div');
    notification.className = 'notification ' + type;
    
    // Bildirim stili
    Object.assign(notification.style, {
        backgroundColor: type === 'success' ? '#4caf50' : 
                         type === 'error' ? '#f44336' : 
                         type === 'info' ? '#2196F3' : '#ff9800',
        color: 'white',
        padding: '12px 20px',
        marginBottom: '10px',
        borderRadius: '4px',
        boxShadow: '0 2px 5px rgba(0,0,0,0.2)',
        transition: 'all 0.3s ease',
        opacity: '0',
        transform: 'translateX(100%)'
    });
    
    // İkon seç
    let icon = '';
    if (type === 'success') icon = '<i class="fas fa-check-circle"></i> ';
    else if (type === 'error') icon = '<i class="fas fa-exclamation-circle"></i> ';
    else if (type === 'info') icon = '<i class="fas fa-info-circle"></i> ';
    else icon = '<i class="fas fa-bell"></i> ';
    
    notification.innerHTML = icon + message;
    
    // Bildirim containerına ekle
    notificationContainer.appendChild(notification);
    
    // Bildirim animasyonu (görünür yap)
    setTimeout(() => {
        notification.style.opacity = '1';
        notification.style.transform = 'translateX(0)';
    }, 10);
    
    // Belirli süre sonra bildirim kaybolur
    setTimeout(() => {
        notification.style.opacity = '0';
        notification.style.transform = 'translateX(100%)';
        
        // Gerçekten kaldırmadan önce animasyonun bitmesini bekle
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
    
    return notification;
}

// Başlangıç durumunu gösteren fonksiyon
function displayInitialState() {
    selectedProducts = []; // Seçili ürünleri temizle
    const selectedProductsList = document.getElementById('selected-products-list');
    const suggestedProductsContainer = document.getElementById('suggested-products');
    const productSuggestions = document.querySelector('.product-suggestions');

    // Seçili ürünler listesi için başlangıç mesajı
    selectedProductsList.innerHTML = `
        <div class="no-products-message">
            <i class="fas fa-tshirt"></i>
            <p>Henüz ürün seçilmedi</p>
            <small>Kombin oluşturmak için ürün seçmeye başlayın.</small>
        </div>
    `;

    // Önerilen kombinler için başlangıç mesajı
    suggestedProductsContainer.innerHTML = `
        <div class="no-match-message">
            <i class="fas fa-shopping-bag"></i>
            <p>Kombin önerileri için lütfen ürün seçin.</p>
            <small>Ürün seçtikten sonra size uygun kombinler burada listelenecektir.</small>
        </div>
    `;

    // Önerilen kombinler bölümünü gizle
    productSuggestions.classList.remove('visible');
}

// Listeden sepete ekleme fonksiyonu
function addToCartFromListing(productId, name, price, image) {
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
    
    // Sepeti localStorage'dan al
    let cart = JSON.parse(localStorage.getItem('cart')) || [];
    
    // Sepette aynı üründen var mı kontrol et
    const existingItem = cart.find(item => item.productId === safeId);
    
    if (existingItem) {
        // Eğer ürün zaten sepette varsa miktarını artır
        existingItem.quantity += 1;
        showNotification('Ürün miktarı sepette güncellendi', 'success');
    } else {
        // Yoksa yeni ürün olarak ekle
        cart.push({
            productId: safeId,
            name: safeName,
            price: safePrice,
            image: safeImage,
            quantity: 1
        });
        showNotification('Ürün sepete eklendi', 'success');
    }
    
    // Sepeti güncelle
    localStorage.setItem('cart', JSON.stringify(cart));
    
    // Sepet sayısını güncelle
    if (typeof updateCartCount === 'function') {
        updateCartCount();
    }
} 