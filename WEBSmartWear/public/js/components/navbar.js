// Navbar functionality
document.addEventListener('DOMContentLoaded', () => {
    // Mobile menu toggle
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const navigationLinks = document.querySelector('.nav-links');

    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', () => {
            navigationLinks.classList.toggle('active');
        });
    }

    // Set active nav link based on current page
    const currentPage = window.location.pathname;
    const navLinks = document.querySelectorAll('.nav-link');

    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });

    // Update cart count from localStorage
    updateCartCount();
    
    // Search functionality
    setupSearchFunctionality();
    
    // Close mobile menu when clicking outside
    document.addEventListener('click', (e) => {
        if (navigationLinks.classList.contains('active') && 
            !e.target.closest('.nav-links') && 
            !e.target.closest('.mobile-menu-btn')) {
            navigationLinks.classList.remove('active');
        }
    });
    
    // Setup dropdown interactions
    setupDropdowns();
    
    // Update auth UI elements based on login state
    updateNavbarAuthUI();
    
    // Activate current category link if function exists
    if (typeof activateCurrentCategoryLink === 'function') {
        activateCurrentCategoryLink();
    }
    
    // Add click event to category nav links
    setupCategoryNavLinks();
});

// Export updateCartCount function to global scope for other scripts to use
window.updateCartCount = updateCartCount;

// Update cart count
function updateCartCount() {
    try {
        const cartCountElements = document.querySelectorAll('.cart-count');
        let cart = [];
        
        try {
            // localStorage'dan cart verilerini al ve doğru şekilde parse et
            const savedCart = localStorage.getItem('cart');
            if (savedCart) {
                const parsedCart = JSON.parse(savedCart);
                // Geçerli bir dizi olduğundan emin ol
                if (Array.isArray(parsedCart)) {
                    cart = parsedCart;
                }
            }
        } catch (error) {
            console.error('Sepet verisi parse edilemedi:', error);
        }
        
        // Toplam ürün sayısını hesapla
        let totalItems = 0;
        cart.forEach(item => {
            if (item && typeof item === 'object') {
                const quantity = typeof item.quantity === 'number' && item.quantity > 0 ? item.quantity : 1;
                totalItems += quantity;
            } else if (typeof item === 'string') {
                // Eski format desteği (eğer sadece ID'ler varsa)
                totalItems += 1;
            }
        });
        
        // Tüm cart-count elementlerini güncelle
        cartCountElements.forEach(element => {
            if (element) {
                // Her zaman sayıyı güncelle, görünür/gizli durumunu sonra yönet
                element.textContent = totalItems;
                
                // Eğer ürün varsa badge'i göster, yoksa gizle
                if (totalItems > 0) {
                    element.classList.remove('hidden');
                    
                    // Parent elementindeki badge'i kontrol et
                    const parentElement = element.parentElement;
                    if (parentElement && parentElement.classList.contains('badge')) {
                        parentElement.classList.remove('hidden');
                    }
                } else {
                    // Sepet boşsa sayı 0 olarak kalacak ama gizleme yapmayacağız
                    // Badge elementini görünür tut ama sadece 0 göster
                    // element.classList.add('hidden'); - Bu satırı kaldırdık
                    
                    // Parent badge'i kontrol et
                    const parentElement = element.parentElement;
                    if (parentElement && parentElement.classList.contains('badge')) {
                        parentElement.classList.remove('hidden'); // Her zaman görünür olsun
                    }
                }
            }
        });
    } catch (error) {
        console.error('Sepet sayısı güncellenirken hata oluştu:', error);
    }
}

// Setup search functionality
function setupSearchFunctionality() {
    console.log('Setting up search functionality');
    const searchIcon = document.querySelector('.search-icon');
    
    if (searchIcon) {
        console.log('Search icon found, adding click event');
        
        // Create search form if it doesn't exist
        let searchForm = document.querySelector('.navbar-search-form');
        if (!searchForm) {
            // Create the search form element
            searchForm = document.createElement('div');
            searchForm.className = 'navbar-search-form';
            searchForm.innerHTML = `
                <input type="text" id="navbar-search-input" placeholder="Ürün adı veya kategori ara...">
                <button id="navbar-search-button"><i class="fas fa-search"></i></button>
                <button id="navbar-search-close"><i class="fas fa-times"></i></button>
            `;
            
            // Search form'u body'ye ekle
            document.body.appendChild(searchForm);
            
            // Initially hide the search form
            searchForm.style.display = 'none';
            
            // Add event listener to close button
            const closeButton = searchForm.querySelector('#navbar-search-close');
            closeButton.addEventListener('click', () => {
                closeSearchForm(searchForm, searchIcon);
            });
            
            // Add event listener to search button
            const searchButton = searchForm.querySelector('#navbar-search-button');
            const searchInput = searchForm.querySelector('#navbar-search-input');
            
            searchButton.addEventListener('click', () => {
                const query = searchInput.value.trim();
                if (query) {
                    performSearchInline(query);
                    closeSearchForm(searchForm, searchIcon);
                }
            });
            
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    const query = searchInput.value.trim();
                    if (query) {
                        performSearchInline(query);
                        closeSearchForm(searchForm, searchIcon);
                    }
                }
            });
            
            // Close form when clicking outside
            document.addEventListener('click', (e) => {
                if (searchForm.style.display === 'flex' && 
                    !searchForm.contains(e.target) && 
                    e.target !== searchIcon) {
                    closeSearchForm(searchForm, searchIcon);
                }
            });
        }
        
        // Add click event to search icon
        searchIcon.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            console.log('Search icon clicked');
            
            // Show search form and hide search icon
            const navContainer = document.querySelector('.nav-container');
            if (navContainer) {
                // Konumlandırma için: search form'u nav-container'a göre konumlandır
                const rect = navContainer.getBoundingClientRect();
                searchForm.style.top = `${rect.top + rect.height / 2}px`;
                searchForm.style.display = 'flex';
            } else {
                searchForm.style.display = 'flex';
            }
            
            searchIcon.style.display = 'none';
            
            // Focus on input
            setTimeout(() => {
                const searchInput = document.getElementById('navbar-search-input');
                if (searchInput) {
                    searchInput.focus();
                }
            }, 100);
        });
    } else {
        console.error('Search icon not found! Check if .search-icon exists in the DOM');
    }
}

// Close search form helper function
function closeSearchForm(searchForm, searchIcon) {
    searchForm.style.display = 'none';
    searchIcon.style.display = 'inline-block';
}

// Perform inline search function
async function performSearchInline(query) {
    console.log('Inline search started with query:', query);
    if (!query.trim()) return;
    
    // Create or get the search results container
    let mainSection = document.querySelector('section.hero, section.featured-products, section.product-display');
    let searchResults = document.getElementById('inline-search-results');
    
    if (!searchResults) {
        searchResults = document.createElement('section');
        searchResults.id = 'inline-search-results';
        searchResults.className = 'search-results-section';
        
        // Insert before the first main section
        if (mainSection) {
            mainSection.parentNode.insertBefore(searchResults, mainSection);
        } else {
            // If no main section found, insert after navbar
            const navbar = document.getElementById('navbar-container');
            if (navbar) {
                navbar.parentNode.insertBefore(searchResults, navbar.nextSibling);
            } else {
                // Last resort, append to body
                document.body.appendChild(searchResults);
            }
        }
    }
    
    // Show loading state
    searchResults.innerHTML = `
        <div class="search-results-container">
            <h2>Arama Sonuçları: "${query}"</h2>
            <div class="loading"><i class="fas fa-spinner fa-spin"></i> Aranıyor...</div>
        </div>
    `;
    
    // Make search results visible
    searchResults.style.display = 'block';
    
    // Scroll to search results
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
                const imagePath = product.imagePath || `/public/images/products/default.jpg`;
                
                resultsHTML += `
                    <div class="product-card">
                        <a href="/views/product.html?id=${product._id}" class="product-link">
                            <div class="product-img-container">
                                ${product.imagePath 
                                    ? `<img src="${product.imagePath}" alt="${product.name}" loading="lazy">` 
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

// Setup dropdown interactions
function setupDropdowns() {
    const dropdowns = document.querySelectorAll('.dropdown');
    
    dropdowns.forEach(dropdown => {
        const trigger = dropdown.querySelector('a');
        const content = dropdown.querySelector('.dropdown-content');
        
        // For touch devices
        if (trigger && content && window.innerWidth <= 768) {
            trigger.addEventListener('click', (e) => {
                e.preventDefault();
                content.classList.toggle('show');
                e.stopPropagation();
            });
            
            // Close when clicking outside
            document.addEventListener('click', (e) => {
                if (!dropdown.contains(e.target)) {
                    content.classList.remove('show');
                }
            });
        }
    });
}

// Update navbar auth UI
function updateNavbarAuthUI() {
    const token = localStorage.getItem('token');
    const userName = localStorage.getItem('userName');
    const userRole = localStorage.getItem('userRole');
    
    const authLinks = document.querySelectorAll('.auth-link');
    const profileLinks = document.querySelectorAll('.profile-link');
    
    if (token && userName) {
        // User is logged in
        authLinks.forEach(link => link.style.display = 'none');
        profileLinks.forEach(link => {
            link.style.display = 'block';
            
            // Kullanıcı admin ise profil linki yerine admin paneli linki göster
            const profileLink = link.querySelector('.user-profile-btn');
            if (profileLink) {
                if (userRole === 'admin') {
                    // Admin için özel dropdown menü
                    const adminDropdown = document.createElement('div');
                    adminDropdown.className = 'dropdown';
                    adminDropdown.innerHTML = `
                        <a href="#" class="user-profile-btn">
                            <i class="fas fa-user-shield"></i>
                            <span class="user-name">Admin İşlemleri</span>
                            <i class="fas fa-chevron-down"></i>
                        </a>
                        <div class="dropdown-content">
                            <a href="/views/admin/add-product.html"><i class="fas fa-plus-circle"></i> Ürün Ekle</a>
                            <a href="/views/admin/manage-products.html"><i class="fas fa-box"></i> Ürünleri Yönet</a>
                            <a href="/views/admin/create-outfit.html"><i class="fas fa-tshirt"></i> Kombin Oluştur</a>
                            <a href="#" id="admin-logout-link"><i class="fas fa-sign-out-alt"></i> Çıkış Yap</a>
                        </div>
                    `;
                    
                    // Mevcut profil linkini dropdown ile değiştir
                    profileLink.parentNode.replaceChild(adminDropdown, profileLink);
                    
                    // Çıkış yapma işlevini ekle
                    const logoutLink = adminDropdown.querySelector('#admin-logout-link');
                    logoutLink.addEventListener('click', (e) => {
                        e.preventDefault();
                        localStorage.removeItem('token');
                        localStorage.removeItem('userId');
                        localStorage.removeItem('userName');
                        localStorage.removeItem('userRole');
                        window.location.href = '/views/index.html';
                    });
                } else {
                    profileLink.href = '/views/profile.html';
                    if (profileLink.querySelector('.user-name')) {
                        profileLink.querySelector('.user-name').textContent = userName;
                    }
                }
            }
        });
    } else {
        // User is not logged in
        authLinks.forEach(link => link.style.display = 'block');
        profileLinks.forEach(link => link.style.display = 'none');
    }
}

// Setup category nav links
function setupCategoryNavLinks() {
    const categoryLinks = document.querySelectorAll('.category-nav-link');
    
    categoryLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            const category = link.getAttribute('data-category');
            
            // Store category in sessionStorage for page changes
            if (category) {
                sessionStorage.setItem('currentCategory', category);
            }
        });
    });
} 