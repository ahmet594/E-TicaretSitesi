// DOM Elements
const categoryFilter = document.getElementById('category-filter');
const minPriceInput = document.getElementById('min-price');
const maxPriceInput = document.getElementById('max-price');
const sortOption = document.getElementById('sort-option');
const applyFiltersBtn = document.getElementById('apply-filters');
const resetFiltersBtn = document.getElementById('reset-filters');
const activeFiltersContainer = document.getElementById('active-filters');
const productsContainer = document.getElementById('products-container');

// Global variables
let allProducts = [];
let filteredProducts = [];
let activeFilters = {
    category: '',
    minPrice: null,
    maxPrice: null,
    sort: 'featured'
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Fetch all products once on page load
    fetchAllProducts();
    
    // Add event listeners to filter elements
    setupFilterEventListeners();
    
    // Check URL parameters for initial filters
    checkUrlParams();
});

// Fetch all products
async function fetchAllProducts() {
    showLoading();
    
    try {
        const response = await fetch('/api/products');
        if (!response.ok) {
            throw new Error('Ürünler yüklenirken hata oluştu');
        }
        
        allProducts = await response.json();
        filteredProducts = [...allProducts];
        
        // Apply initial filters if any
        applyFilters();
        
    } catch (error) {
        showError(error.message);
    }
}

// Setup event listeners for filter elements
function setupFilterEventListeners() {
    // Apply filters button
    applyFiltersBtn.addEventListener('click', () => {
        updateActiveFilters();
        applyFilters();
        updateUrlParams();
    });
    
    // Reset filters button
    resetFiltersBtn.addEventListener('click', () => {
        resetFilters();
    });
    
    // Individual filter elements (for real-time preview/suggestions in the future)
    categoryFilter.addEventListener('change', () => {
        // For future: Show relevant price ranges based on selected category
    });
    
    // Allow pressing Enter in price inputs to apply filters
    minPriceInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') applyFiltersBtn.click();
    });
    
    maxPriceInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') applyFiltersBtn.click();
    });
    
    // Sort option change immediately applies
    sortOption.addEventListener('change', () => {
        activeFilters.sort = sortOption.value;
        applyFilters();
        updateUrlParams();
    });
}

// Update active filters based on UI elements
function updateActiveFilters() {
    activeFilters.category = categoryFilter.value || '';
    activeFilters.minPrice = minPriceInput.value ? parseFloat(minPriceInput.value) : null;
    activeFilters.maxPrice = maxPriceInput.value ? parseFloat(maxPriceInput.value) : null;
    activeFilters.sort = sortOption.value;
}

// Apply all active filters
function applyFilters() {
    // Start with all products
    filteredProducts = [...allProducts];
    
    // Apply category filter
    if (activeFilters.category) {
        filteredProducts = filteredProducts.filter(product => 
            product.category === activeFilters.category ||
            product.subcategory === activeFilters.category
        );
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
    
    // Category filter tag
    if (activeFilters.category) {
        const categoryOption = categoryFilter.querySelector(`option[value="${activeFilters.category}"]`);
        const categoryName = categoryOption ? categoryOption.textContent : activeFilters.category;
        
        addFilterTag('Kategori', categoryName, () => {
            categoryFilter.value = '';
            activeFilters.category = '';
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
            minPriceInput.value = '';
            maxPriceInput.value = '';
            activeFilters.minPrice = null;
            activeFilters.maxPrice = null;
            applyFilters();
            updateUrlParams();
        });
    }
    
    // Sort order filter tag (only if not default)
    if (activeFilters.sort && activeFilters.sort !== 'featured') {
        const sortOptionElement = sortOption.querySelector(`option[value="${activeFilters.sort}"]`);
        const sortName = sortOptionElement ? sortOptionElement.textContent : activeFilters.sort;
        
        addFilterTag('Sıralama', sortName, () => {
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
    categoryFilter.value = '';
    minPriceInput.value = '';
    maxPriceInput.value = '';
    sortOption.value = 'featured';
    
    // Reset active filters
    activeFilters = {
        category: '',
        minPrice: null,
        maxPrice: null,
        sort: 'featured'
    };
    
    // Update display
    applyFilters();
    updateUrlParams();
}

// Update URL parameters to reflect current filters
function updateUrlParams() {
    const url = new URL(window.location);
    
    // Clear existing params
    url.searchParams.delete('category');
    url.searchParams.delete('min_price');
    url.searchParams.delete('max_price');
    url.searchParams.delete('sort');
    
    // Add current filters as params
    if (activeFilters.category) {
        url.searchParams.set('category', activeFilters.category);
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
    
    // Update URL without reloading the page
    window.history.replaceState({}, '', url);
}

// Check URL parameters for initial filters
function checkUrlParams() {
    const params = new URLSearchParams(window.location.search);
    
    // Check and set category filter
    if (params.has('category')) {
        const category = params.get('category');
        categoryFilter.value = category;
        activeFilters.category = category;
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

// Display products
function displayProducts(products) {
    if (!productsContainer) return;
    
    if (products.length === 0) {
        productsContainer.innerHTML = `
            <div class="no-products">
                <p>Bu kriterlere uygun ürün bulunamadı.</p>
                <button id="clear-filters" class="filter-btn">Filtreleri Temizle</button>
            </div>
        `;
        
        document.getElementById('clear-filters').addEventListener('click', resetFilters);
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
    
    // Favorileri kontrol edip işaretle (products.js'den)
    if (typeof checkFavorites === 'function') {
        checkFavorites();
    }
}

// Kategorilerin görünen adlarını döndürür
function getCategoryDisplayName(category) {
    const categoryDisplayNames = {
        'Kadın': 'Kadın',
        'Erkek': 'Erkek',
        'akıllı-saat': 'Akıllı Saatler',
        'akıllı-giyim': 'Akıllı Giyim',
        'fitness-tracker': 'Fitness Takipçileri',
        'aksesuar': 'Aksesuarlar'
    };
    
    return categoryDisplayNames[category] || category;
} 