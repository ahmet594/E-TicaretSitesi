// Seçili ürünleri saklamak için array
let selectedProducts = [];

// DOM elementleri
const categoryFilter = document.getElementById('category-filter');
const colorFilter = document.getElementById('color-filter');
const styleFilter = document.getElementById('style-filter');
const applyFiltersBtn = document.getElementById('apply-filters');
const productsGrid = document.getElementById('products-grid');
const outfitSlots = document.querySelectorAll('.outfit-slot');
const saveOutfitBtn = document.getElementById('save-outfit');
const clearOutfitBtn = document.getElementById('clear-outfit');

// DOM yüklendiğinde
document.addEventListener('DOMContentLoaded', () => {
    // Navbar'ı yükle
    loadNavbar();
    
    // LocalStorage'dan seçili ürünleri al ve görüntüle
    loadSelectedProducts();
    
    // Önerilen ürünleri yükle
    loadSuggestedProducts();

    // İlk ürünleri yükle
    loadProducts();

    // Event listeners
    applyFiltersBtn.addEventListener('click', loadProducts);
    clearOutfitBtn.addEventListener('click', clearOutfit);
    saveOutfitBtn.addEventListener('click', saveOutfit);

    // Outfit slotları için event listeners
    outfitSlots.forEach(slot => {
        slot.addEventListener('click', () => {
            const category = slot.dataset.category;
            if (!slot.classList.contains('filled')) {
                // Kategori filtresini otomatik olarak seç
                categoryFilter.value = category;
                // Ürünleri yükle
                loadProducts();
            }
        });
    });
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

// Seçili ürünleri yükle ve görüntüle
function loadSelectedProducts() {
    const selectedProductsList = document.getElementById('selected-products-list');
    selectedProducts = JSON.parse(localStorage.getItem('outfitProducts') || '[]');
    
    if (selectedProducts.length === 0) {
        selectedProductsList.innerHTML = ''; // Empty state CSS ile gösterilecek
        return;
    }
    
    selectedProductsList.innerHTML = selectedProducts.map(product => `
        <div class="selected-product-card" data-id="${product.id}">
            <img src="${product.image}" alt="${product.name}">
            <div class="product-info">
                <h4 class="product-name">${product.name}</h4>
                <p class="product-price">${product.price}</p>
                <p class="product-category">${product.category}</p>
            </div>
            <button class="remove-btn" onclick="removeFromOutfit('${product.id}')">
                <i class="fas fa-times"></i>
            </button>
        </div>
    `).join('');
}

// Önerilen ürünleri yükle
function loadSuggestedProducts() {
    const suggestedProducts = document.getElementById('suggested-products');
    
    // API'den önerilen ürünleri al
    // TODO: Backend entegrasyonu yapılacak
    // Şimdilik örnek veriler
    const dummyProducts = [
        {
            id: 1,
            name: "Örnek Ürün 1",
            price: "199.99",
            image: "/images/products/dummy1.jpg"
        },
        // Diğer örnek ürünler...
    ];
    
    suggestedProducts.innerHTML = dummyProducts.map(product => `
        <div class="product-card">
            <img src="${product.image}" alt="${product.name}">
            <h4>${product.name}</h4>
            <p class="price">₺${product.price}</p>
            <button onclick="addToOutfit(${JSON.stringify(product)})">
                <i class="fas fa-plus"></i> Kombine Ekle
            </button>
        </div>
    `).join('');
}

// Ürünleri yükle
async function loadProducts() {
    // Loading durumunu göster
    productsGrid.innerHTML = `
        <div class="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Ürünler yükleniyor...</span>
        </div>
    `;

    // Filtreleri al
    const filters = {
        category: categoryFilter.value,
        color: colorFilter.value,
        style: styleFilter.value
    };

    try {
        // API'den ürünleri al (örnek veri kullanıyoruz)
        const products = await getMockProducts(filters);
        displayProducts(products);
    } catch (error) {
        productsGrid.innerHTML = `
            <div class="error">
                <i class="fas fa-exclamation-circle"></i>
                <p>Ürünler yüklenirken bir hata oluştu.</p>
            </div>
        `;
    }
}

// Ürünleri görüntüle
function displayProducts(products) {
    if (products.length === 0) {
        productsGrid.innerHTML = `
            <div class="no-results">
                <i class="fas fa-search"></i>
                <p>Bu kriterlere uygun ürün bulunamadı.</p>
            </div>
        `;
        return;
    }

    productsGrid.innerHTML = products.map(product => `
        <div class="product-card" data-id="${product.id}" data-category="${product.category}">
            <img src="${product.image}" alt="${product.name}">
            <div class="product-info">
                <h4>${product.name}</h4>
                <span class="price">${product.price} TL</span>
            </div>
        </div>
    `).join('');

    // Ürün kartları için event listeners
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.addEventListener('click', () => addToOutfit(card));
    });
}

// Kombine ürün ekle
function addToOutfit(productCard) {
    const productId = productCard.dataset.id;
    const category = productCard.dataset.category;
    const productImage = productCard.querySelector('img').src;
    const productName = productCard.querySelector('h4').textContent;
    const productPrice = productCard.querySelector('.price').textContent;

    // İlgili slot'u bul
    const targetSlot = document.querySelector(`.outfit-slot[data-category="${category}"]`);
    
    if (targetSlot) {
        // Slot'u güncelle
        targetSlot.innerHTML = `
            <img src="${productImage}" alt="${productName}">
            <div class="remove-item">
                <i class="fas fa-times"></i>
            </div>
        `;
        targetSlot.classList.add('filled');

        // Seçili ürünü kaydet
        selectedProducts.push({
            id: productId,
            name: productName,
            image: productImage,
            price: productPrice
        });
        localStorage.setItem('outfitProducts', JSON.stringify(selectedProducts));

        // Remove butonuna event listener ekle
        const removeBtn = targetSlot.querySelector('.remove-item');
        removeBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            removeFromOutfit(productId);
        });

        // Kaydet butonunu güncelle
        updateSaveButton();
    }
}

// Kombinden ürün çıkar
function removeFromOutfit(productId) {
    // Ürünü seçili ürünler listesinden kaldır
    selectedProducts = selectedProducts.filter(product => product.id !== productId);
    
    // LocalStorage'ı güncelle
    localStorage.setItem('outfitProducts', JSON.stringify(selectedProducts));
    
    // UI'ı güncelle
    loadSelectedProducts();
    
    // Bildirim göster
    showNotification('Ürün kombinden çıkarıldı', 'info');
}

// Kombini temizle
function clearOutfit() {
    outfitSlots.forEach(slot => {
        const category = slot.dataset.category;
        removeFromOutfit(category, slot);
    });
}

// Kombini kaydet
function saveOutfit() {
    // Kombini localStorage'a kaydet
    const outfits = JSON.parse(localStorage.getItem('savedOutfits') || '[]');
    outfits.push({
        id: Date.now(),
        date: new Date().toISOString(),
        items: selectedProducts
    });
    localStorage.setItem('savedOutfits', JSON.stringify(outfits));

    // Kullanıcıya bildirim göster
    alert('Kombininiz başarıyla kaydedildi!');
}

// Kaydet butonunu güncelle
function updateSaveButton() {
    const isComplete = selectedProducts.length > 0;
    saveOutfitBtn.disabled = !isComplete;
}

// Kategori ikonu al
function getCategoryIcon(category) {
    const icons = {
        'ust-giyim': 'tshirt',
        'alt-giyim': 'socks',
        'ayakkabi': 'shoe-prints',
        'aksesuar': 'glasses'
    };
    return icons[category] || 'tshirt';
}

// Kategori adı al
function getCategoryName(category) {
    const names = {
        'ust-giyim': 'Üst Giyim',
        'alt-giyim': 'Alt Giyim',
        'ayakkabi': 'Ayakkabı',
        'aksesuar': 'Aksesuar'
    };
    return names[category] || category;
}

// Mock ürün verileri (gerçek API yerine)
async function getMockProducts(filters) {
    // API simülasyonu için timeout
    await new Promise(resolve => setTimeout(resolve, 500));

    const mockProducts = [
        {
            id: 1,
            name: 'Basic Beyaz T-Shirt',
            category: 'ust-giyim',
            price: '199.90',
            color: 'beyaz',
            style: 'casual',
            image: 'https://picsum.photos/200'
        },
        {
            id: 2,
            name: 'Slim Fit Jean',
            category: 'alt-giyim',
            price: '399.90',
            color: 'lacivert',
            style: 'casual',
            image: 'https://picsum.photos/201'
        },
        {
            id: 3,
            name: 'Deri Sneaker',
            category: 'ayakkabi',
            price: '599.90',
            color: 'beyaz',
            style: 'casual',
            image: 'https://picsum.photos/202'
        },
        {
            id: 4,
            name: 'Deri Kemer',
            category: 'aksesuar',
            price: '299.90',
            color: 'kahverengi',
            style: 'business',
            image: 'https://picsum.photos/203'
        }
    ];

    // Filtreleme
    return mockProducts.filter(product => {
        if (filters.category && filters.category !== product.category) return false;
        if (filters.color && filters.color !== product.color) return false;
        if (filters.style && filters.style !== product.style) return false;
        return true;
    });
}

// Bildirim göster
function showNotification(message, type = 'success') {
    const notificationContainer = document.createElement('div');
    notificationContainer.className = `notification ${type}`;
    notificationContainer.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i> ${message}`;
    
    document.body.appendChild(notificationContainer);
    
    // 3 saniye sonra bildirimi kaldır
    setTimeout(() => {
        notificationContainer.style.opacity = '0';
        setTimeout(() => {
            notificationContainer.remove();
        }, 300);
    }, 2700);
} 