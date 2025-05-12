// Alt kategori seçenekleri
const subcategoryOptions = {
    'Giyim': [
        't-shirt', 'gömlek', 'kazak', 'hırka', 'sweatshirt', 
        'mont', 'kaban-parka', 'ceket', 'kot-pantolon', 
        'kumaş-pantolon', 'şortlar', 'takım-elbise', 'eşofman', 'yelek'
    ],
    'Ayakkabı': [
        'Spor Ayakkabı', 'Sneaker', 'Bot', 'Klasik'
    ],
    'Aksesuar': [
        'Kemer', 'Kravat', 'Şapka', 'Atkı', 'Eldiven', 'Saat', 'Cüzdan'
    ]
};

// DOM elementleri
const productsTableBody = document.getElementById('products-table-body');
const searchInput = document.getElementById('search-input');
const searchBtn = document.getElementById('search-btn');
const categoryFilter = document.getElementById('category-filter');
const statusFilter = document.getElementById('status-filter');
const productModal = document.getElementById('product-modal');
const editProductForm = document.getElementById('edit-product-form');
const closeModalBtn = document.querySelector('.close-modal');

// Filtreleme değişkenleri
let currentPage = 1;
let searchQuery = '';
let categoryFilterValue = '';
let statusFilterValue = '';

// Sayfa yüklendiğinde
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    setupEventListeners();
});

// Event listener'ları ayarla
function setupEventListeners() {
    // Arama
    searchBtn.addEventListener('click', () => {
        searchQuery = searchInput.value.trim();
        currentPage = 1;
        loadProducts();
    });

    searchInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchQuery = searchInput.value.trim();
            currentPage = 1;
            loadProducts();
        }
    });

    // Filtreler
    categoryFilter.addEventListener('change', () => {
        categoryFilterValue = categoryFilter.value;
        currentPage = 1;
        loadProducts();
    });

    statusFilter.addEventListener('change', () => {
        statusFilterValue = statusFilter.value;
        currentPage = 1;
        loadProducts();
    });

    // Modal kapatma
    closeModalBtn.addEventListener('click', () => {
        productModal.style.display = 'none';
    });

    // Modal dışına tıklama
    window.addEventListener('click', (e) => {
        if (e.target === productModal) {
            productModal.style.display = 'none';
        }
    });

    // Form gönderimi
    editProductForm.addEventListener('submit', handleProductUpdate);
}

// Ürünleri yükle
async function loadProducts() {
    try {
        productsTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="loading-message">
                    <i class="fas fa-spinner fa-spin"></i> Ürünler yükleniyor...
                </td>
            </tr>
        `;

        const response = await fetch('/api/products');
        const products = await response.json();

        if (products.length === 0) {
            productsTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="no-results">
                        <i class="fas fa-box-open"></i>
                        <p>Henüz ürün bulunmuyor</p>
                    </td>
                </tr>
            `;
            return;
        }

        displayProducts(products);
    } catch (error) {
        console.error('Ürünler yüklenirken hata:', error);
        productsTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="error-message">
                    <i class="fas fa-exclamation-circle"></i>
                    <p>Ürünler yüklenirken bir hata oluştu</p>
                </td>
            </tr>
        `;
    }
}

// Ürünleri tabloda göster
function displayProducts(products) {
    productsTableBody.innerHTML = '';

    products.forEach(product => {
        const statusClass = product.featured ? 'status-featured' : 
                          product.stock > 0 ? 'status-active' : 'status-inactive';
        const statusText = product.featured ? 'Öne Çıkan' : 
                          product.stock > 0 ? 'Stokta Var' : 'Stokta Yok';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${product._id.substring(0, 8)}...</td>
            <td>
                <div class="product-img">
                    <img src="${product.image}" alt="${product.name}">
                </div>
            </td>
            <td>${product.name}</td>
            <td>${product.category}</td>
            <td>₺${product.price.toFixed(2)}</td>
            <td>${product.stock}</td>
            <td><span class="status ${statusClass}">${statusText}</span></td>
            <td>
                <div class="action-buttons">
                    <button class="edit-product" data-id="${product._id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="delete-product" data-id="${product._id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;

        // Düzenleme butonu
        row.querySelector('.edit-product').addEventListener('click', () => {
            openEditModal(product);
        });

        // Silme butonu
        row.querySelector('.delete-product').addEventListener('click', () => {
            if (confirm('Bu ürünü silmek istediğinizden emin misiniz?')) {
                deleteProduct(product._id);
            }
        });

        productsTableBody.appendChild(row);
    });
}

// Düzenleme modalını aç
function openEditModal(product) {
    document.getElementById('edit-product-id').value = product._id;
    document.getElementById('edit-product-name').value = product.name;
    document.getElementById('edit-product-description').value = product.description;
    document.getElementById('edit-product-price').value = product.price;
    document.getElementById('edit-product-stock').value = product.stock;
    document.getElementById('edit-product-category').value = product.category;
    document.getElementById('edit-product-subcategory').value = product.subcategory;
    document.getElementById('edit-product-color').value = product.color;
    document.getElementById('edit-product-brand').value = product.brand;
    document.getElementById('edit-product-featured').checked = product.featured;

    // Alt kategori seçeneklerini güncelle
    updateSubcategoryOptions(product.category);

    productModal.style.display = 'block';
}

// Alt kategori seçeneklerini güncelle
function updateSubcategoryOptions(category) {
    const subcategorySelect = document.getElementById('edit-product-subcategory');
    subcategorySelect.innerHTML = '<option value="">Alt Kategori Seçin</option>';

    if (subcategoryOptions[category]) {
        subcategoryOptions[category].forEach(subcategory => {
            const option = document.createElement('option');
            option.value = subcategory;
            option.textContent = subcategory;
            subcategorySelect.appendChild(option);
        });
    }
}

// Ürün güncelleme
async function handleProductUpdate(e) {
    e.preventDefault();

    const productId = document.getElementById('edit-product-id').value;
    const formData = new FormData(editProductForm);
    const productData = Object.fromEntries(formData.entries());

    try {
        const response = await fetch(`/api/products/${productId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(productData)
        });

        if (!response.ok) {
            throw new Error('Ürün güncellenirken bir hata oluştu');
        }

        productModal.style.display = 'none';
        loadProducts();
        alert('Ürün başarıyla güncellendi');
    } catch (error) {
        console.error('Ürün güncellenirken hata:', error);
        alert('Ürün güncellenirken bir hata oluştu');
    }
}

// Ürün silme
async function deleteProduct(productId) {
    try {
        const response = await fetch(`/api/products/${productId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Ürün silinirken bir hata oluştu');
        }

        loadProducts();
        alert('Ürün başarıyla silindi');
    } catch (error) {
        console.error('Ürün silinirken hata:', error);
        alert('Ürün silinirken bir hata oluştu');
    }
} 