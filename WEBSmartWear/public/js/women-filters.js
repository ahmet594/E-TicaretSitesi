// DOM Elements

let productsContainer;
let sortOption;
let subcategoryFilters;
let resetFiltersBtn;
let minPriceInput;
let maxPriceInput;
let applyPriceFilterBtn;
let currentCategoryTitle;
let activeFiltersContainer;
let sizeFilters;
let colorFilters;
let categoryTitles;

// Debug için DOM elemanlarını kontrol et
console.log("Women-filters.js yüklendi");

// Global variables
let allProducts = [];
let filteredProducts = [];
let categoryCounters = {};

// Active filters object
let activeFilters = {
    category: 'Kadın', // Ana kategori (değişmez)
    subcategory: '',   // Alt kategori
    minPrice: null,
    maxPrice: null,
    sort: 'featured',
    sizes: [],         // Seçilen bedenler
    colors: []         // Seçilen renkler
};

// Initialize everything
function initWomenFilters() {
    console.log("Women filters init başlatılıyor...");
    
    // URL parametrelerini kontrol et
    checkUrlParams();
    
    // Tüm filtreleri sıfırla
    resetFilters();
    
    // Kategori başlıkları için açılır-kapanır fonksiyonalite ekle
    setupCategoryCollapse();
    
    // Ürünleri yükle
    fetchKadinProducts();
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    console.log("DOM yüklendi, kadın sayfası hazırlanıyor...");
    
    // DOM elementlerini başlat
    initDomElements();
    
    // Tüm filtreleme sistemini başlat
    initWomenFilters();
});

// DOM elementlerini başlat
function initDomElements() {
    productsContainer = document.getElementById('products-container');
    sortOption = document.getElementById('sort-option');
    subcategoryFilters = document.querySelectorAll('.subcategory-filter');
    resetFiltersBtn = document.getElementById('reset-filters');
    minPriceInput = document.getElementById('min-price');
    maxPriceInput = document.getElementById('max-price');
    applyPriceFilterBtn = document.getElementById('apply-price-filter');
    currentCategoryTitle = document.getElementById('current-category');
    activeFiltersContainer = document.getElementById('active-filters');
    sizeFilters = document.querySelectorAll('.size-filter');
    colorFilters = document.querySelectorAll('.color-filter');
    categoryTitles = document.querySelectorAll('.category-title');
    
    // Debug için DOM elemanlarını kontrol et
    console.log("DOM Elementleri başlatıldı:", {
        productsContainer: !!productsContainer,
        sortOption: !!sortOption,
        subcategoryFilters: subcategoryFilters.length,
        resetFiltersBtn: !!resetFiltersBtn,
        minPriceInput: !!minPriceInput,
        maxPriceInput: !!maxPriceInput,
        applyPriceFilterBtn: !!applyPriceFilterBtn,
        currentCategoryTitle: !!currentCategoryTitle,
        activeFiltersContainer: !!activeFiltersContainer,
        sizeFilters: sizeFilters.length,
        colorFilters: colorFilters.length,
        categoryTitles: categoryTitles.length
    });
    
    // Diğer olay dinleyicilerini ayarla
    setupOtherEventListeners();
}

// Diğer olay dinleyicilerini ayarla
function setupOtherEventListeners() {
    // Sort option change
    const sortOption = document.getElementById('sort-option');
    if (sortOption) {
        sortOption.addEventListener('change', () => {
            activeFilters.sort = sortOption.value;
            applyFilters();
            updateUrlParams();
        });
    }
    
    // Reset filters button
    const resetFiltersBtn = document.getElementById('reset-filters');
    if (resetFiltersBtn) {
        resetFiltersBtn.addEventListener('click', resetFilters);
    }
    
    // Apply price filter
    const applyPriceFilterBtn = document.getElementById('apply-price-filter');
    const minPriceInput = document.getElementById('min-price');
    const maxPriceInput = document.getElementById('max-price');
    
    if (applyPriceFilterBtn && minPriceInput && maxPriceInput) {
        applyPriceFilterBtn.addEventListener('click', () => {
            activeFilters.minPrice = minPriceInput.value ? parseFloat(minPriceInput.value) : null;
            activeFilters.maxPrice = maxPriceInput.value ? parseFloat(maxPriceInput.value) : null;
            applyFilters();
            updateUrlParams();
        });
        
        // Allow Enter key in price inputs
        minPriceInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter' && applyPriceFilterBtn) applyPriceFilterBtn.click();
        });
        
        maxPriceInput.addEventListener('keyup', (e) => {
            if (e.key === 'Enter' && applyPriceFilterBtn) applyPriceFilterBtn.click();
        });
    }
    
    // Beden filtreleri için event listener'lar
    sizeFilters.forEach(filter => {
        filter.addEventListener('change', () => {
            // Tüm seçili bedenleri topla
            activeFilters.sizes = Array.from(sizeFilters)
                .filter(input => input.checked)
                .map(input => input.dataset.size);
            
            applyFilters();
            updateUrlParams();
        });
    });
    
    // Renk filtreleri için event listener'lar
    colorFilters.forEach(filter => {
        filter.addEventListener('change', () => {
            // Tüm seçili renkleri topla
            activeFilters.colors = Array.from(colorFilters)
                .filter(input => input.checked)
                .map(input => input.dataset.color);
            
            applyFilters();
            updateUrlParams();
        });
    });
}

// Fetch all women's products
async function fetchKadinProducts() {
    showLoading();
    
    try {
        console.log("Kadın ürünleri yükleniyor...");
        const response = await fetch('/api/products/category/Kadın');
        
        if (!response.ok) {
            throw new Error('Kadın ürünleri yüklenirken hata oluştu: ' + response.status);
        }
        
        const data = await response.json();
        console.log("Yüklenen ürün sayısı:", data.length);
        
        // Ürünlerin veritabanındaki durumunu kontrol et ve sabitleştir
        console.log("VERİTABANINDAN GELEN ÜRÜNLER:");
        
        // Ön işleme ile ürünlerin kategori bilgilerini düzelt
        const processedData = processProductCategories(data);
        
        allProducts = processedData;
        filteredProducts = [...allProducts];
        
        // Şimdi tüm ürün kategorilerini listele
        listAllCategories();
        
        // Update category counters
        updateCategoryCounters();
        
        // Apply initial filters if any
        applyFilters();
        
    } catch (error) {
        console.error("Ürün yükleme hatası:", error);
        showError(error.message);
    }
}

// Ürünleri ön işleme ile kategori düzeltmelerini yap
function processProductCategories(products) {
    return products.map(product => {
        // Derin kopyalama
        const processedProduct = { ...product };
        
        console.log(`Ürün: ${processedProduct.name}`);
        console.log(`- Category: ${processedProduct.category}`);
        console.log(`- Subcategory: ${processedProduct.subcategory || 'TANIMLANMAMIŞ'}`);
        console.log(`- Type: ${processedProduct.type || 'TANIMLANMAMIŞ'}`);
        
        // subcategory yoksa type'dan oluştur
        if (!processedProduct.subcategory && processedProduct.type) {
            console.log(`- Subcategory eksik, type değerinden oluşturuluyor: ${processedProduct.type}`);
            processedProduct.subcategory = processedProduct.type;
        }
        
        // Kot pantolon için özel kontrol
        if (
            (processedProduct.name && processedProduct.name.toLowerCase().includes('jean')) ||
            (processedProduct.description && processedProduct.description.toLowerCase().includes('jean')) ||
            (processedProduct.name && processedProduct.name.toLowerCase().includes('kot'))
        ) {
            console.log(`- Bu ürün bir kot/jean ürünü`);
            
            // Eğer subcategory tanımlanmamış veya farklı bir değere sahipse değiştir
            if (!processedProduct.subcategory || !processedProduct.subcategory.toLowerCase().includes('kot')) {
                console.log(`- Kot pantolon olarak işaretleniyor (önceki değer: ${processedProduct.subcategory || 'yok'})`);
                processedProduct.subcategory = 'kot-pantolon';
            }
        }
        
        return processedProduct;
    });
}

// Tüm kategorileri listele
function listAllCategories() {
    console.log("\nTÜM MEVCUT KATEGORİLER:");
    const categories = new Set();
    allProducts.forEach(product => {
        if (product.subcategory) {
            categories.add(product.subcategory.toLowerCase());
        }
    });
    console.log([...categories]);
}

// Update category counters
function updateCategoryCounters() {
    // Initialize counters for all subcategories
    subcategoryFilters.forEach(filter => {
        const subcategory = filter.getAttribute('data-category');
        categoryCounters[subcategory] = 0;
    });
    
    // Count products per subcategory
    allProducts.forEach(product => {
        // Doğrudan subcategory alanını kullan
        if (product.subcategory) {
            const subcategoryKey = 'kadin-' + product.subcategory.toLowerCase().replace(/\s+/g, '-');
            if (categoryCounters.hasOwnProperty(subcategoryKey)) {
                categoryCounters[subcategoryKey]++;
            }
        }
        
        // Alternatif olarak tip alanını da kontrol et (eski ürünler için)
        if (product.type) {
            const type = 'kadin-' + product.type.toLowerCase().replace(/\s+/g, '-');
            if (categoryCounters.hasOwnProperty(type)) {
                categoryCounters[type]++;
            }
        }
    });
    
    // Kategori sayılarını artık göstermiyoruz
}

// HTML kategori ismi ile veritabanı kategori ismi eşleştirme
function mapHtmlCategoryToDbCategory(htmlCategory) {
    // HTML'deki kadin- ön ekini temizle
    const cleanCategory = htmlCategory.replace('kadin-', '');
    
    // Özel eşleştirmeler
    const categoryMappings = {
        'kot-pantolon': ['kot-pantolon', 'kot', 'jean', 'jeans', 'denim'],
        'pantolon': ['pantolon'],
        'sort': ['şort', 'sort', 'short'],
        'tayt': ['tayt', 'leggings'],
        'etek': ['etek', 'skirt'],
        'tshirt': ['tshirt', 't-shirt', 'tişört'],
        'sweatshirt': ['sweatshirt', 'hoodie'],
        'ceket': ['ceket', 'jacket'],
        'bluz': ['bluz', 'blouse'],
        'tunik': ['tunik', 'tunic'],
        'gomlek': ['gömlek', 'gomlek', 'shirt'],
        'esofman': ['eşofman', 'esofman', 'tracksuit'],
        'mont': ['mont', 'coat'],
        'trenckot': ['trençkot', 'trenckot', 'trenchcoat'],
        'hirka': ['hırka', 'hirka', 'cardigan'],
        'kazak': ['kazak', 'sweater'],
        'kaban': ['kaban', 'overcoat'],
        'bolero': ['bolero'],
        'topuklu-ayakkabi': ['topuklu', 'topuklu-ayakkabi', 'high-heels'],
        'bot': ['bot', 'boot'],
        'sneakers': ['sneakers', 'sneaker'],
        'spor-ayakkabi': ['spor-ayakkabi', 'spor'],
        'babet': ['babet', 'flat'],
        'canta': ['çanta', 'canta', 'bag'],
        'atki': ['atkı', 'atki', 'scarf'],
        'eldiven': ['eldiven', 'gloves'],
        'sal': ['şal', 'sal', 'shawl'],
        'sapka': ['şapka', 'sapka', 'hat'],
        'kemer': ['kemer', 'belt']
    };
    
    // İlgili htmlCategory için tüm olası veritabanı kategorilerini döndür
    return categoryMappings[cleanCategory] || [cleanCategory];
}

// Apply all active filters
function applyFilters() {
    console.log("applyFilters çağrıldı");
    console.log("Aktif filtreler:", activeFilters);
    
    // Start with all products (always women's products)
    filteredProducts = [...allProducts];
    console.log("Toplam kadın ürünleri:", filteredProducts.length);
    
    // Apply subcategory filter
    if (activeFilters.subcategory) {
        // HTML kategori adını veritabanı kategori isimlerine dönüştür
        const possibleDbCategories = mapHtmlCategoryToDbCategory(activeFilters.subcategory);
        console.log("HTML kategori:", activeFilters.subcategory);
        console.log("Olası veritabanı kategorileri:", possibleDbCategories);
        
        // TÜM ÜRÜN SUBCATEGORY DEĞERLERİNİ YAZDIR (HATA AYIKLAMA İÇİN)
        console.log("VERİTABANINDAKİ TÜM ÜRÜNLER VE KATEGORİLERİ:");
        allProducts.forEach(product => {
            console.log(`${product.name}:
            - subcategory: ${product.subcategory || 'YOK'}
            - type: ${product.type || 'YOK'}
            - category: ${product.category || 'YOK'}`);
        });
        
        // Daha esnek bir filtreleme mantığı uygulayalım
        filteredProducts = filteredProducts.filter(product => {
            // Her ürünün veritabanındaki kategori değerini standartlaştıralım
            let normalizedDbSubcategory = '';
            
            if (product.subcategory) {
                normalizedDbSubcategory = product.subcategory.toLowerCase().replace(/\s+/g, '-');
            } else if (product.type) {
                normalizedDbSubcategory = product.type.toLowerCase().replace(/\s+/g, '-');
            }
            
            // Herhangi bir kategori eşleşmesi var mı kontrol et
            for (const dbCategory of possibleDbCategories) {
                // Tam eşleşme kontrolü
                if (normalizedDbSubcategory === dbCategory) {
                    console.log(`✅ EŞLEŞME: ${product.name} - tam kategori eşleşmesi (${dbCategory})`);
                    return true;
                }
                
                // Kısmi eşleşme kontrolü
                if (normalizedDbSubcategory.includes(dbCategory) || 
                    (product.name && product.name.toLowerCase().includes(dbCategory))) {
                    console.log(`✅ EŞLEŞME: ${product.name} - kısmi kategori eşleşmesi (${dbCategory})`);
                    return true;
                }
            }
            
            console.log(`❌ EŞLEŞME YOK: ${product.name}`);
            return false;
        });
        
        console.log(`Filtreleme sonrası ${filteredProducts.length} ürün bulundu:`, 
            filteredProducts.map(p => p.name).join(', '));
    }
    
    // Apply price range filter
    if (activeFilters.minPrice !== null) {
        filteredProducts = filteredProducts.filter(product => 
            product.price >= activeFilters.minPrice
        );
    }
    
    if (activeFilters.maxPrice !== null) {
        filteredProducts = filteredProducts.filter(product => 
            product.price <= activeFilters.maxPrice
        );
    }
    
    // Apply size filters
    if (activeFilters.sizes && activeFilters.sizes.length > 0) {
        filteredProducts = filteredProducts.filter(product => {
            if (!product.size) return false;
            
            // Tam eşleşme için direkt kontrol
            if (activeFilters.sizes.includes(product.size)) {
                return true;
            }
            
            // Virgülle ayrılmış bedenler için kontrol (ör: "S, M, L")
            if (product.size.includes(',')) {
                const productSizes = product.size.split(',').map(s => s.trim());
                return productSizes.some(size => activeFilters.sizes.includes(size));
            }
            
            return false;
        });
    }
    
    // Apply color filters
    if (activeFilters.colors && activeFilters.colors.length > 0) {
        filteredProducts = filteredProducts.filter(product => {
            if (!product.color) return false;
            
            // Tam eşleşme için direkt kontrol
            if (activeFilters.colors.some(color => 
                product.color.toLowerCase() === color.toLowerCase())) {
                return true;
            }
            
            // Ürün açıklamasında renk geçiyorsa kontrol
            if (product.description && activeFilters.colors.some(color => 
                product.description.toLowerCase().includes(color.toLowerCase()))) {
                return true;
            }
            
            // Ürün adında renk geçiyorsa kontrol
            if (product.name && activeFilters.colors.some(color => 
                product.name.toLowerCase().includes(color.toLowerCase()))) {
                return true;
            }
            
            return false;
        });
    }
    
    // Sort products
    sortProducts();
    
    // Display filtered products
    displayProducts(filteredProducts);
    
    // Update active filters display
    updateActiveFiltersTags();
}

// Sort products based on selected option
function sortProducts() {
    switch (activeFilters.sort) {
        case 'price-low':
            filteredProducts.sort((a, b) => a.price - b.price);
            break;
        case 'price-high':
            filteredProducts.sort((a, b) => b.price - a.price);
            break;
        case 'name-asc':
            filteredProducts.sort((a, b) => a.name.localeCompare(b.name));
            break;
        case 'name-desc':
            filteredProducts.sort((a, b) => b.name.localeCompare(a.name));
            break;
        case 'featured':
        default:
            // Featured products first, then sort by id (newest)
            filteredProducts.sort((a, b) => {
                if (a.featured && !b.featured) return -1;
                if (!a.featured && b.featured) return 1;
                return 0;
            });
            break;
    }
}

// Display active filter tags
function updateActiveFiltersTags() {
    activeFiltersContainer.innerHTML = '';
    
    // Subcategory filter tag
    if (activeFilters.subcategory) {
        const subcategoryFilter = document.querySelector(`.subcategory-filter[data-category="${activeFilters.subcategory}"]`);
        const subcategoryName = subcategoryFilter ? subcategoryFilter.textContent.trim() : activeFilters.subcategory;
        
        addFilterTag('Kategori', subcategoryName, () => {
            // Clear subcategory filter
            activeFilters.subcategory = '';
            subcategoryFilters.forEach(f => f.classList.remove('active'));
            currentCategoryTitle.textContent = 'Tüm Kadın Ürünleri';
            applyFilters();
            updateUrlParams();
        });
    }
    
    // Price range filter tag
    if (activeFilters.minPrice !== null || activeFilters.maxPrice !== null) {
        let priceRangeText = '';
        if (activeFilters.minPrice !== null && activeFilters.maxPrice !== null) {
            priceRangeText = `${activeFilters.minPrice} TL - ${activeFilters.maxPrice} TL`;
        } else if (activeFilters.minPrice !== null) {
            priceRangeText = `≥ ${activeFilters.minPrice} TL`;
        } else if (activeFilters.maxPrice !== null) {
            priceRangeText = `≤ ${activeFilters.maxPrice} TL`;
        }
        
        addFilterTag('Fiyat', priceRangeText, () => {
            // Clear price filter
            minPriceInput.value = '';
            maxPriceInput.value = '';
            activeFilters.minPrice = null;
            activeFilters.maxPrice = null;
            applyFilters();
            updateUrlParams();
        });
    }
    
    // Size filter tags
    if (activeFilters.sizes && activeFilters.sizes.length > 0) {
        activeFilters.sizes.forEach(size => {
            addFilterTag('Beden', size, () => {
                // Beden filtresini kaldır
                const sizeInput = document.querySelector(`.size-filter[data-size="${size}"]`);
                if (sizeInput) sizeInput.checked = false;
                
                // Aktif filtrelerden bu bedeni çıkar
                activeFilters.sizes = activeFilters.sizes.filter(s => s !== size);
                
                applyFilters();
                updateUrlParams();
            });
        });
    }
    
    // Color filter tags
    if (activeFilters.colors && activeFilters.colors.length > 0) {
        activeFilters.colors.forEach(color => {
            addFilterTag('Renk', color, () => {
                // Renk filtresini kaldır
                const colorInput = document.querySelector(`.color-filter[data-color="${color}"]`);
                if (colorInput) colorInput.checked = false;
                
                // Aktif filtrelerden bu rengi çıkar
                activeFilters.colors = activeFilters.colors.filter(c => c !== color);
                
                applyFilters();
                updateUrlParams();
            });
        });
    }
    
    // Sort order filter tag (only if not default)
    if (activeFilters.sort && activeFilters.sort !== 'featured') {
        const sortOptionElement = sortOption.querySelector(`option[value="${activeFilters.sort}"]`);
        const sortName = sortOptionElement ? sortOptionElement.textContent : activeFilters.sort;
        
        addFilterTag('Sıralama', sortName, () => {
            // Reset sort option
            sortOption.value = 'featured';
            activeFilters.sort = 'featured';
            applyFilters();
            updateUrlParams();
        });
    }
}

// Add a filter tag to active filters container
function addFilterTag(type, value, removeCallback) {
    const tag = document.createElement('div');
    tag.className = 'filter-tag';
    tag.innerHTML = `
        <span>${type}: ${value}</span>
        <i class="fas fa-times"></i>
    `;
    
    tag.querySelector('i').addEventListener('click', removeCallback);
    activeFiltersContainer.appendChild(tag);
}

// Reset all filters
function resetFilters() {
    // Reset UI elements
    subcategoryFilters.forEach(f => f.classList.remove('active'));
    minPriceInput.value = '';
    maxPriceInput.value = '';
    sortOption.value = 'featured';
    currentCategoryTitle.textContent = 'Tüm Kadın Ürünleri';
    
    // Reset checkbox filters
    sizeFilters.forEach(filter => filter.checked = false);
    colorFilters.forEach(filter => filter.checked = false);
    
    // Reset active filters
    activeFilters = {
        category: 'Kadın', // Ana kategori (değişmez)
        subcategory: '',   // Alt kategori
        minPrice: null,
        maxPrice: null,
        sort: 'featured',
        sizes: [],         // Seçilen bedenler
        colors: []         // Seçilen renkler
    };
    
    // Update display
    applyFilters();
    updateUrlParams();
}

// Update URL parameters to reflect current filters
function updateUrlParams() {
    const url = new URL(window.location);
    
    // Clear existing params
    url.searchParams.delete('subcategory');
    url.searchParams.delete('min_price');
    url.searchParams.delete('max_price');
    url.searchParams.delete('sort');
    url.searchParams.delete('sizes');
    url.searchParams.delete('colors');
    
    // Add current filters as params
    if (activeFilters.subcategory) {
        url.searchParams.set('subcategory', activeFilters.subcategory);
    }
    
    if (activeFilters.minPrice !== null) {
        url.searchParams.set('min_price', activeFilters.minPrice);
    }
    
    if (activeFilters.maxPrice !== null) {
        url.searchParams.set('max_price', activeFilters.maxPrice);
    }
    
    if (activeFilters.sort !== 'featured') {
        url.searchParams.set('sort', activeFilters.sort);
    }
    
    // Beden, renk ve marka filtrelerini URL'e ekle
    if (activeFilters.sizes && activeFilters.sizes.length > 0) {
        url.searchParams.set('sizes', activeFilters.sizes.join(','));
    }
    
    if (activeFilters.colors && activeFilters.colors.length > 0) {
        url.searchParams.set('colors', activeFilters.colors.join(','));
    }
    
    // Update URL without reloading the page
    window.history.replaceState({}, '', url);
}

// Check URL parameters for initial filters
function checkUrlParams() {
    const params = new URLSearchParams(window.location.search);
    
    // Check and set subcategory filter
    if (params.has('subcategory')) {
        const subcategory = params.get('subcategory');
        const subcategoryFilter = document.querySelector(`.subcategory-filter[data-category="${subcategory}"]`);
        
        if (subcategoryFilter) {
            activeFilters.subcategory = subcategory;
            subcategoryFilter.classList.add('active');
            
            // Update current category title
            const subcategoryName = subcategoryFilter.textContent.trim();
            currentCategoryTitle.textContent = `${subcategoryName}`;
        }
    }
    
    // Check and set price filters
    if (params.has('min_price')) {
        const minPrice = parseFloat(params.get('min_price'));
        if (!isNaN(minPrice)) {
            minPriceInput.value = minPrice;
            activeFilters.minPrice = minPrice;
        }
    }
    
    if (params.has('max_price')) {
        const maxPrice = parseFloat(params.get('max_price'));
        if (!isNaN(maxPrice)) {
            maxPriceInput.value = maxPrice;
            activeFilters.maxPrice = maxPrice;
        }
    }
    
    // Check and set sort option
    if (params.has('sort')) {
        const sort = params.get('sort');
        if (['featured', 'price-low', 'price-high', 'name-asc', 'name-desc'].includes(sort)) {
            sortOption.value = sort;
            activeFilters.sort = sort;
        }
    }
    
    // Check and set size filters
    if (params.has('sizes')) {
        const sizes = params.get('sizes').split(',');
        activeFilters.sizes = sizes;
        
        // UI'da checkbox'ları güncelle
        sizes.forEach(size => {
            const sizeInput = document.querySelector(`.size-filter[data-size="${size}"]`);
            if (sizeInput) sizeInput.checked = true;
        });
    }
    
    // Check and set color filters
    if (params.has('colors')) {
        const colors = params.get('colors').split(',');
        activeFilters.colors = colors;
        
        // UI'da checkbox'ları güncelle
        colors.forEach(color => {
            const colorInput = document.querySelector(`.color-filter[data-color="${color}"]`);
            if (colorInput) colorInput.checked = true;
        });
    }
}

// Show loading indicator
function showLoading() {
    if (!productsContainer) {
        console.error("productsContainer bulunamadı!");
        return;
    }
    
    // Önce içeriği temizle
    productsContainer.innerHTML = '';
    
    // Loading elementini oluştur
    const loadingDiv = document.createElement('div');
    loadingDiv.className = 'loading';
    loadingDiv.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Ürünler yükleniyor...';
    
    // Container'a ekle
    productsContainer.appendChild(loadingDiv);
}

// Show error message
function showError(message) {
    if (!productsContainer) {
        console.error("productsContainer bulunamadı!");
        return;
    }
    
    // Önce içeriği temizle
    productsContainer.innerHTML = '';
    
    // Error elementini oluştur
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error';
    errorDiv.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <p>${message}</p>
    `;
    
    // Container'a ekle
    productsContainer.appendChild(errorDiv);
}

// Display products
function displayProducts(products) {
    if (!productsContainer) {
        console.error("productsContainer bulunamadı!");
        return;
    }
    
    // Önce içeriği temizle
    productsContainer.innerHTML = '';
    
    // Ürün yoksa
    if (!products || products.length === 0) {
        const noProductsDiv = document.createElement('div');
        noProductsDiv.className = 'no-products';
        noProductsDiv.innerHTML = `
            <p>Bu kriterlere uygun ürün bulunamadı.</p>
            <button id="clear-filters" class="filter-btn">Filtreleri Temizle</button>
        `;
        
        productsContainer.appendChild(noProductsDiv);
        
        // Temizleme butonuna event listener ekle
        const clearFiltersBtn = document.getElementById('clear-filters');
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', resetFilters);
        }
        return;
    }
    
    // Ürünleri görüntüle
    products.forEach(product => {
        const productCard = document.createElement('div');
        productCard.className = 'product-card';
        
        // Ürün bilgilerinden undefined olanları kontrol et
        const productName = product.name || 'İsimsiz Ürün';
        const productId = product._id || '';
        const productPrice = product.price || 0;
        const productImage = product.image || '/images/placeholder.jpg';
        const productStock = typeof product.stock === 'number' ? product.stock : 0;
        const productCategory = product.subcategory || '';
        const productBrand = product.brand || 'SmartWear';
        
        productCard.innerHTML = `
            <a href="/views/product.html?id=${productId}" class="product-link">
                <div class="product-img-container">
                    <img src="${productImage}" alt="${productName}">
                    ${productStock <= 3 && productStock > 0 ? `<span class="product-badge limited-stock">Sınırlı Stok</span>` : ''}
                    ${productStock === 0 ? `<span class="product-badge out-of-stock-badge">Tükendi</span>` : ''}
                    ${product.featured ? `<span class="product-badge featured-badge">Öne Çıkan</span>` : ''}
                </div>
                <div class="product-card-content">
                    <div class="product-brand">${productBrand}</div>
                    <h3 class="product-title">${productName}</h3>
                    <div class="product-details">
                        <div class="product-category">${getCategoryDisplayName(productCategory)}</div>
                        ${product.color ? `<div class="product-color">Renk: ${product.color}</div>` : ''}
                        ${product.size ? `<div class="product-size">Beden: ${product.size}</div>` : ''}
                    </div>
                    <div class="product-price-container">
                        <p class="price">${productPrice.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}</p>
                        <p class="stock ${productStock > 0 ? 'in-stock' : 'out-of-stock'}">
                            ${productStock > 0 ? `${productStock} adet stokta` : 'Stokta Yok'}
                        </p>
                    </div>
                </div>
            </a>
            <div class="product-actions">
                <button onclick="addToCartFromListing('${productId}', '${productName.replace(/'/g, "\\'")}', ${productPrice}, '${productImage}')" class="add-to-cart" ${productStock === 0 ? 'disabled' : ''}>
                    <i class="fas fa-shopping-cart"></i>
                    ${productStock > 0 ? 'Sepete Ekle' : 'Stokta Yok'}
                </button>
                <button onclick="addToFavorites('${productId}')" class="add-to-favorites" title="Favorilere Ekle">
                    <i class="far fa-heart"></i>
                </button>
            </div>
        `;
        
        productsContainer.appendChild(productCard);
    });
    
    // Favorileri kontrol edip işaretle (products.js'den)
    if (typeof checkFavorites === 'function') {
        checkFavorites();
    }
}

// Kategorilerin görünen adlarını döndürür
function getCategoryDisplayName(subcategory) {
    const subcategoryDisplayNames = {
        'pantolon': 'Pantolon',
        'sort': 'Şort',
        'tayt': 'Tayt',
        'etek': 'Etek',
        'tshirt': 'T-Shirt',
        'sweatshirt': 'Sweatshirt',
        'ceket': 'Ceket',
        'bluz': 'Bluz',
        'tunik': 'Tunik',
        'gomlek': 'Gömlek',
        'esofman': 'Eşofman',
        'mont': 'Mont',
        'trenckot': 'Trençkot',
        'hirka': 'Hırka',
        'kazak': 'Kazak',
        'kaban': 'Kaban',
        'bolero': 'Bolero',
        'kot-pantolon': 'Kot Pantolon',
        'topuklu-ayakkabi': 'Topuklu Ayakkabı',
        'bot': 'Bot',
        'sneakers': 'Sneakers',
        'spor-ayakkabi': 'Spor Ayakkabı',
        'babet': 'Babet',
        'canta': 'Çanta',
        'atki': 'Atkı',
        'eldiven': 'Eldiven',
        'sal': 'Şal',
        'sapka': 'Şapka',
        'kemer': 'Kemer'
    };
    
    // Normalize subcategory for lookup
    const normalizedSubcategory = subcategory.toLowerCase().replace(/\s+/g, '-');
    
    return subcategoryDisplayNames[normalizedSubcategory] || subcategory;
}

// Global olarak filterByCategory fonksiyonunu tanımla (tüm tarayıcılarda çalışması için)
window.filterByCategory = function(category) {
    console.log(`filterByCategory fonksiyonu çağrıldı: ${category}`);
    
    // Aktif kategoriyi ayarla
    activeFilters.subcategory = category;
    
    // Diğer tüm aktif sınıfları temizle
    const allCategoryLinks = document.querySelectorAll('.subcategory-filter');
    allCategoryLinks.forEach(link => {
        link.classList.remove('active');
    });
    
    // Seçilen kategori linkini aktif yap
    const selectedLink = document.querySelector(`.subcategory-filter[data-category="${category}"]`);
    if (selectedLink) {
        selectedLink.classList.add('active');
        
        // Kategori başlığını güncelle
        if (currentCategoryTitle) {
            currentCategoryTitle.textContent = selectedLink.textContent.trim();
        }
    }
    
    // Özellikle kot pantolon kontrolü
    if (category === 'kadin-kot-pantolon') {
        console.log("KOT PANTOLON KATEGORİSİ SEÇİLDİ - TÜM ÜRÜNLER KONTROL EDİLİYOR");
        allProducts.forEach(product => {
            const hasJean = 
                (product.name && product.name.toLowerCase().includes('jean')) ||
                (product.description && product.description.toLowerCase().includes('jean')) ||
                (product.name && product.name.toLowerCase().includes('kot')) ||
                (product.subcategory && product.subcategory.toLowerCase().includes('kot'));
            
            if (hasJean) {
                console.log(`Kot/Jean ürünü bulundu: ${product.name}`);
            }
        });
    }
    
    // Filtreleri uygula
    console.log("Filtreler uygulanıyor...");
    applyFilters();
    updateUrlParams();
    
    // Default tarayıcı davranışını engelle
    return false;
};

// Kategori başlıkları için açılır-kapanır fonksiyonalite
function setupCategoryCollapse() {
    console.log("Kategori açılır-kapanır özelliği ayarlanıyor...");
    
    const categoryTitles = document.querySelectorAll('.category-title');
    categoryTitles.forEach(title => {
        title.addEventListener('click', function() {
            // Bu başlığın hemen sonrasındaki subcategory-list elementini bul
            const subcategoryList = this.nextElementSibling;
            
            // Başlık ve liste sınıflarını değiştir
            this.classList.toggle('collapsed');
            subcategoryList.classList.toggle('collapsed');
            
            console.log(`${this.textContent.trim()} kategorisi ${this.classList.contains('collapsed') ? 'kapandı' : 'açıldı'}`);
        });
    });
} 