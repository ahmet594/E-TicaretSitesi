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
        
        // Eğer "Tümü" seçildiyse tüm kategori ürünlerini göster
        if (subcategory === "Tümü") {
            const response = await fetch(`/api/products/category/${mainCategory}`);
            
            if (!response.ok) {
                throw new Error('Ürünler yüklenirken hata oluştu');
            }
            
            const products = await response.json();
            displayProducts(products);
            updateProductCount(products.length);
            updateSubcategoryCounts(products);
            return;
        }
        
        // Alt kategoriye göre API çağrısı yap
        const response = await fetch(`/api/products/category/${mainCategory}/subcategory/${subcategory}`);
        
        if (!response.ok) {
            throw new Error('Alt kategori ürünleri yüklenirken hata oluştu');
        }
        
        const products = await response.json();
        
        // Filtrelenmiş ürünleri göster
        displayProducts(products);
        
        // Ürün sayısını güncelle
        updateProductCount(products.length);
        updateSubcategoryCounts(products);
    } catch (error) {
        showError(error.message);
    }
}

// Ürün sayısını güncelle
function updateProductCount(count) {
    const resultsCountElement = document.querySelector('.results-count span');
    if (resultsCountElement) {
        resultsCountElement.textContent = `${count} ürün bulundu`;
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
        if (product.subcategory && subcategoryCounts[product.subcategory] !== undefined) {
            subcategoryCounts[product.subcategory]++;
        }
    });
    
    // Sayıları güncelle
    subcategoryLinks.forEach(link => {
        const subcategory = link.getAttribute('data-category');
        const countElement = link.querySelector('.subcategory-count');
        
        if (countElement && subcategoryCounts[subcategory] !== undefined) {
            countElement.textContent = subcategoryCounts[subcategory];
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