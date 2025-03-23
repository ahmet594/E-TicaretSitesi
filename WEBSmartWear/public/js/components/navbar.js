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
});

// Update cart count
function updateCartCount() {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    const cartCount = document.querySelector('.cart-count');
    if (cartCount) {
        let totalItems = 0;
        cart.forEach(item => {
            totalItems += (item.quantity || 1);
        });
        cartCount.textContent = totalItems;
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
                <input type="text" id="navbar-search-input" placeholder="Ürün adı veya kategori girin...">
                <button id="navbar-search-button"><i class="fas fa-search"></i></button>
                <button id="navbar-search-close"><i class="fas fa-times"></i></button>
            `;
            
            // Insert search form after search icon
            searchIcon.parentNode.insertBefore(searchForm, searchIcon.nextSibling);
            
            // Initially hide the search form
            searchForm.style.display = 'none';
            
            // Add event listener to close button
            const closeButton = searchForm.querySelector('#navbar-search-close');
            closeButton.addEventListener('click', () => {
                searchForm.style.display = 'none';
                searchIcon.style.display = 'inline-block';
                
                // Remove expanded class to reset spacing
                const navIcons = document.querySelector('.nav-icons');
                if (navIcons) {
                    navIcons.classList.remove('expanded');
                }
            });
            
            // Add event listener to search button
            const searchButton = searchForm.querySelector('#navbar-search-button');
            const searchInput = searchForm.querySelector('#navbar-search-input');
            
            searchButton.addEventListener('click', () => {
                const query = searchInput.value.trim();
                if (query) {
                    performSearchInline(query);
                }
            });
            
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    const query = searchInput.value.trim();
                    if (query) {
                        performSearchInline(query);
                    }
                }
            });
        }
        
        // Add click event to search icon
        searchIcon.addEventListener('click', (e) => {
            e.preventDefault();
            console.log('Search icon clicked');
            
            // Show search form and hide search icon
            searchForm.style.display = 'flex';
            searchIcon.style.display = 'none';
            
            // Adjust spacing in navbar to prevent crowding
            const navIcons = document.querySelector('.nav-icons');
            if (navIcons) {
                navIcons.classList.add('expanded');
            }
            
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
        console.log('Fetching from API...');
        const response = await fetch(`/api/products/search?query=${encodeURIComponent(query)}`);
        
        if (!response.ok) {
            throw new Error('Arama sırasında bir hata oluştu');
        }
        
        console.log('API response received');
        const data = await response.json();
        console.log('Search results:', data);
        
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
                // Determine icon based on category
                let categoryIcon = 'tshirt'; // Default icon
                if (product.category) {
                    if (product.category.toLowerCase().includes('kadın')) {
                        categoryIcon = 'female';
                    } else if (product.category.toLowerCase().includes('erkek')) {
                        categoryIcon = 'male';
                    } else if (product.category.toLowerCase().includes('aksesuar')) {
                        categoryIcon = 'gem';
                    }
                }
                
                const price = product.price ? `₺${product.price.toFixed(2)}` : 'Fiyat bilgisi yok';
                
                resultsHTML += `
                    <div class="product-card">
                        <a href="/views/product.html?id=${product._id}">
                            <div class="product-img">
                                ${product.imagePath 
                                    ? `<img src="${product.imagePath}" alt="${product.name}">` 
                                    : `<i class="fas fa-${categoryIcon} fa-3x"></i>`
                                }
                            </div>
                            <div class="product-info">
                                <h4>${product.name}</h4>
                                <span class="category-tag">${product.category || 'Genel'}</span>
                                <p class="price">${price}</p>
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
    } catch (error) {
        console.error('Search error:', error);
        searchResults.innerHTML = `
            <div class="search-results-container">
                <h2>Arama Sonuçları: "${query}"</h2>
                <div class="error">
                    <i class="fas fa-exclamation-triangle"></i>
                    <p>Hata: ${error.message}</p>
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
    
    const authLinks = document.querySelectorAll('.auth-link');
    const profileLinks = document.querySelectorAll('.profile-link');
    
    if (token && userName) {
        // User is logged in
        authLinks.forEach(link => link.style.display = 'none');
        profileLinks.forEach(link => {
            link.style.display = 'block';
            if (link.querySelector('.user-name')) {
                link.querySelector('.user-name').textContent = userName;
            }
        });
    } else {
        // User is not logged in
        authLinks.forEach(link => link.style.display = 'block');
        profileLinks.forEach(link => link.style.display = 'none');
    }
} 