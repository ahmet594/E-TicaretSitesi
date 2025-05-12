// Set up category filters
function setupCategoryFilters() {
    // Select all category filter buttons
    const categoryButtons = document.querySelectorAll('.category-filter');
    
    // Set the initial active category based on the currentCategory value
    categoryButtons.forEach(button => {
        // For 'all' category, use 'all' data attribute
        if (button.dataset.category === 'all' && currentCategory === 'all') {
            button.classList.add('active');
        }
        // For specific categories, match the button's data-category with currentCategory
        else if (button.dataset.category === currentCategory) {
            button.classList.add('active');
        }
    });
    
    // Add click event listener to each button
    categoryButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all buttons
            categoryButtons.forEach(btn => btn.classList.remove('active'));
            
            // Add active class to clicked button
            button.classList.add('active');
            
            // Get the category from button's data attribute
            const category = button.dataset.category;
            
            // Load products based on selected category
            if (category === 'all') {
                currentCategory = 'all';
                loadAllProducts();
            } else {
                currentCategory = category;
                loadProductsByCategory(category);
            }
        });
    });
    
    // Alt kategori linklerine tıklama olayı ekle
    setupSubcategoryLinks();
}

// Alt kategori linkleri için click olayları ekle
function setupSubcategoryLinks() {
    const subcategoryLinks = document.querySelectorAll('.subcategory-link');
    
    subcategoryLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            
            // Tüm linklerdeki active sınıfını kaldır
            subcategoryLinks.forEach(l => l.classList.remove('active'));
            
            // Tıklanan linke active sınıfı ekle
            link.classList.add('active');
            
            // Alt kategori bilgisini al
            const subcategory = link.getAttribute('data-category');
            
            // Ürünleri alt kategoriye göre filtrele
            filterProductsBySubcategory(subcategory);
        });
    });
    
    // İlk yüklemede "Tümü" kategorisini seç
    const allCategoryLink = document.querySelector('.subcategory-link[data-category="Tümü"]');
    if (allCategoryLink) {
        allCategoryLink.classList.add('active');
    }
    
    // CSS Stili ekleyelim
    const style = document.createElement('style');
    style.textContent = `
        .subcategory-link.active {
            color: var(--accent-color);
            font-weight: 600;
            transform: translateX(3px);
        }
        
        .subcategory-link.active .subcategory-count {
            background-color: var(--accent-color);
            color: white;
        }
    `;
    document.head.appendChild(style);
}

// Tüm ürünleri getir ve sayıları güncelle
async function loadAllProductsAndUpdateCounts() {
    try {
        const currentPath = window.location.pathname;
        let mainCategory = '';
        
        if (currentPath.includes('clothing.html')) {
            mainCategory = 'Giyim';
        } else if (currentPath.includes('shoes.html')) {
            mainCategory = 'Ayakkabı';
        } else if (currentPath.includes('accessories.html')) {
            mainCategory = 'Aksesuar';
        }

        const response = await fetch(`/api/products/category/${mainCategory}`);
        if (!response.ok) {
            throw new Error('Ürünler yüklenirken hata oluştu');
        }

        const products = await response.json();
        updateSubcategoryCounts(products);
    } catch (error) {
        console.error('Ürün sayıları güncellenirken hata:', error);
    }
}

// Alt kategorilere göre ürün sayısını güncelle
function updateSubcategoryCounts(products) {
    const subcategoryLinks = document.querySelectorAll('.subcategory-link');
    const subcategoryCounts = {};
    
    // Önce tüm alt kategorileri sıfırla
    subcategoryLinks.forEach(link => {
        const subcategory = link.getAttribute('data-category');
        subcategoryCounts[subcategory] = 0;
    });
    
    // Toplam ürün sayısını kaydet
    subcategoryCounts['Tümü'] = products.length;
    
    // Her alt kategori için ürün sayısını hesapla
    products.forEach(product => {
        if (product.subcategory) {
            // Alt kategori adını normalize et
            const normalizedSubcategory = product.subcategory.toLowerCase()
                .replace(/\s+/g, '-') // Tüm boşlukları tire ile değiştir
                .replace(/&/g, '-') // & işaretini tire ile değiştir
                .replace(/ğ/g, 'g')
                .replace(/ü/g, 'u')
                .replace(/ş/g, 's')
                .replace(/ı/g, 'i')
                .replace(/ö/g, 'o')
                .replace(/ç/g, 'c')
                .normalize('NFD').replace(/[\u0300-\u036f]/g, ''); // Aksanları kaldır
            
            console.log('Product Subcategory:', product.subcategory);
            console.log('Normalized Subcategory:', normalizedSubcategory);
            
            if (subcategoryCounts[normalizedSubcategory] !== undefined) {
                subcategoryCounts[normalizedSubcategory]++;
            }
        }
    });
    
    // Sayıları güncelle
    subcategoryLinks.forEach(link => {
        const subcategory = link.getAttribute('data-category');
        const countElement = link.querySelector('.subcategory-count');
        
        if (countElement) {
            countElement.textContent = subcategoryCounts[subcategory] || 0;
        }
    });
}

// Loading göster
function showLoading() {
    if (!productsContainer) return;
    
    productsContainer.innerHTML = `
        <div class="loading">
            <i class="fas fa-spinner fa-spin"></i> Ürünler yükleniyor...
        </div>
    `;
}

// Hata göster
function showError(message) {
    if (!productsContainer) return;
    
    productsContainer.innerHTML = `
        <div class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            <p>${message}</p>
        </div>
    `;
}

// Activate the category link in navbar based on current page
function activateCurrentCategoryLink() {
    const currentPath = window.location.pathname;
    console.log("Current path:", currentPath);
    
    const categoryLinks = document.querySelectorAll('.category-nav-link');
    
    categoryLinks.forEach(link => {
        const linkHref = link.getAttribute('href');
        console.log("Checking link:", linkHref);
        
        link.classList.remove('active');
        
        if (currentPath.includes('clothing.html') && linkHref.includes('clothing.html')) {
            link.classList.add('active');
            console.log("Giyim sayfası aktif");
        } else if (currentPath.includes('shoes.html') && linkHref.includes('shoes.html')) {
            link.classList.add('active');
            console.log("Ayakkabı sayfası aktif");
        } else if (currentPath.includes('accessories.html') && linkHref.includes('accessories.html')) {
            link.classList.add('active');
            console.log("Aksesuar sayfası aktif");
        }
    });
}

// Ürünleri görüntüle
function displayProducts(products) {
    const productsContainer = document.getElementById('products-container');
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
                    <img src="${product.image}" alt="${product.name}"
                         onerror="this.onerror=null; this.src='../img/product-placeholder.jpg';">
                    ${product.stock <= 3 && product.stock > 0 ? '<span class="product-badge limited-stock">Sınırlı Stok</span>' : ''}
                    ${product.stock === 0 ? '<span class="product-badge out-of-stock-badge">Tükendi</span>' : ''}
                    ${product.featured ? '<span class="product-badge featured-badge">Öne Çıkan</span>' : ''}
                </div>
                <div class="product-card-content">
                    <div class="product-brand">${product.brand || 'SmartWear'}</div>
                    <h3 class="product-title">${product.name}</h3>
                    <div class="product-details">
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
                <button class="sepete-ekle-btn" onclick="addToCartFromListing('${product._id}', '${product.name}', ${product.price}, '${product.image}')" ${product.stock === 0 ? 'disabled' : ''}>
                    <i class="fas fa-shopping-cart"></i>
                    <span>Sepete Ekle</span>
                </button>
                <button class="favorite-btn" onclick="toggleFavorite('${product._id}')">
                    <i class="far fa-heart"></i>
                </button>
            </div>
        </div>
    `).join('');

    // Favorileri kontrol edip işaretle
    if (typeof checkFavorites === 'function') {
        checkFavorites();
    }
}

// Sayfa yüklendiğinde çalışacak fonksiyonlar
document.addEventListener('DOMContentLoaded', async () => {
    setupCategoryFilters();
    activateCurrentCategoryLink();
    await loadAllProductsAndUpdateCounts();

    // Fiyat filtresi işlevi
    const applyPriceFilterBtn = document.getElementById('apply-price-filter');
    if (applyPriceFilterBtn) {
        applyPriceFilterBtn.addEventListener('click', async () => {
            const minPrice = parseFloat(document.getElementById('min-price').value) || 0;
            const maxPrice = parseFloat(document.getElementById('max-price').value) || Number.MAX_SAFE_INTEGER;

            // Ana kategoriyi belirle (sayfa adından)
            const currentPath = window.location.pathname;
            let mainCategory = '';
            if (currentPath.includes('clothing.html')) {
                mainCategory = 'Giyim';
            } else if (currentPath.includes('shoes.html')) {
                mainCategory = 'Ayakkabı';
            } else if (currentPath.includes('accessories.html')) {
                mainCategory = 'Aksesuar';
            }

            showLoading();
            try {
                const response = await fetch(`/api/products/category/${mainCategory}`);
                if (!response.ok) throw new Error('Ürünler yüklenirken hata oluştu');
                let products = await response.json();
                products = products.filter(product => product.price >= minPrice && product.price <= maxPrice);
                displayProducts(products);
                updateProductCount(products.length);
            } catch (error) {
                showError(error.message);
            }
        });
    }

    // URL'den alt kategori parametresini kontrol et
    const urlParams = new URLSearchParams(window.location.search);
    const subcategory = urlParams.get('subcategory');
    if (subcategory) {
        const subcategoryLink = document.querySelector(`.subcategory-link[data-category="${subcategory}"]`);
        if (subcategoryLink) {
            subcategoryLink.click();
        }
    } else {
        const allCategoryLink = document.querySelector('.subcategory-link[data-category="Tümü"]');
        if (allCategoryLink) {
            allCategoryLink.click();
        }
    }
});

// Alt kategoriye göre ürün filtreleme
async function filterProductsBySubcategory(subcategory) {
    showLoading();
    
    try {
        // Ana kategoriyi belirle (sayfa adından)
        const currentPath = window.location.pathname;
        let mainCategory = '';
        
        if (currentPath.includes('clothing.html')) {
            mainCategory = 'Giyim';
        } else if (currentPath.includes('shoes.html')) {
            mainCategory = 'Ayakkabı';
        } else if (currentPath.includes('accessories.html')) {
            mainCategory = 'Aksesuar';
        }
        
        let products;
        
        // Eğer "Tümü" seçildiyse tüm kategori ürünlerini göster
        if (subcategory === "Tümü") {
            const response = await fetch(`/api/products/category/${mainCategory}`);
            
            if (!response.ok) {
                throw new Error('Ürünler yüklenirken hata oluştu');
            }
            
            products = await response.json();
        } else {
            // Alt kategoriye göre API çağrısı yap
            const response = await fetch(`/api/products/category/${mainCategory}`);
            
            if (!response.ok) {
                throw new Error('Ürünler yüklenirken hata oluştu');
            }
            
            products = await response.json();
            
            // Gelen ürünleri subcategory özelliğine göre filtrele
            products = products.filter(product => {
                if (!product.subcategory) return false;
                
                // Ürünün subcategory'sini normalize et
                const normalizedProductSubcategory = product.subcategory.toLowerCase()
                    .replace(/\s+/g, '-') // Tüm boşlukları tire ile değiştir
                    .replace(/&/g, '-') // & işaretini tire ile değiştir
                    .replace(/ğ/g, 'g')
                    .replace(/ü/g, 'u')
                    .replace(/ş/g, 's')
                    .replace(/ı/g, 'i')
                    .replace(/ö/g, 'o')
                    .replace(/ç/g, 'c')
                    .normalize('NFD').replace(/[\u0300-\u036f]/g, ''); // Aksanları kaldır
                
                console.log('Normalized Product Subcategory:', normalizedProductSubcategory);
                console.log('Selected Subcategory:', subcategory);
                
                return normalizedProductSubcategory === subcategory;
            });
        }
        
        // Filtrelenmiş ürünleri göster
        displayProducts(products);
        
        // Ürün sayısını güncelle
        updateProductCount(products.length);
        
        // URL'yi güncelle
        const url = new URL(window.location.href);
        if (subcategory === "Tümü") {
            url.searchParams.delete('subcategory');
        } else {
            url.searchParams.set('subcategory', subcategory);
        }
        window.history.pushState({}, '', url);
        
    } catch (error) {
        showError(error.message);
    } finally {
        hideLoading();
    }
}

// Ürün sayısını güncelle
function updateProductCount(count) {
    const resultsCountElement = document.querySelector('.results-count span');
    if (resultsCountElement) {
        resultsCountElement.textContent = `${count} ürün bulundu`;
    }
}

// Sayfa yüklendiğinde çalışacak fonksiyonlar
document.addEventListener('DOMContentLoaded', () => {
    setupCategoryFilters();
    activateCurrentCategoryLink();
    
    // URL'den alt kategori parametresini kontrol et
    const urlParams = new URLSearchParams(window.location.search);
    const subcategory = urlParams.get('subcategory');
    
    if (subcategory) {
        const subcategoryLink = document.querySelector(`.subcategory-link[data-category="${subcategory}"]`);
        if (subcategoryLink) {
            subcategoryLink.click();
        }
    } else {
        // Alt kategori seçili değilse "Tümü" kategorisini seç
        const allCategoryLink = document.querySelector('.subcategory-link[data-category="Tümü"]');
        if (allCategoryLink) {
            allCategoryLink.click();
        }
    }
}); 