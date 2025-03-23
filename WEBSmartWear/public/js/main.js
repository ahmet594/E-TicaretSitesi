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
        const response = await fetch(`/api/products/category/${category}`);
        const products = await response.json();
        displayProducts(products);
    } catch (error) {
        console.error('Error fetching products by category:', error);
    }
}

// Display Products
function displayProducts(products) {
    featuredProducts.innerHTML = products.map(product => `
        <div class="product-card">
            <img src="${product.image}" alt="${product.name}">
            <div class="product-card-content">
                <h3>${product.name}</h3>
                <p class="price">${product.price.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}</p>
                <p class="stock ${product.stock > 0 ? 'in-stock' : 'out-of-stock'}">
                    ${product.stock > 0 ? 'Stokta' : 'Stokta Yok'}
                </p>
                <button onclick="addToCart('${product._id}')" class="add-to-cart" ${product.stock === 0 ? 'disabled' : ''}>
                    ${product.stock > 0 ? 'Sepete Ekle' : 'Stokta Yok'}
                </button>
            </div>
        </div>
    `).join('');
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

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    fetchFeaturedProducts();
});
