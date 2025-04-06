// Sepet için verileri localStorage'da tutacağız
let cart = [];

// Sayfa yüklendiğinde localStorage'dan sepeti yükle
document.addEventListener('DOMContentLoaded', () => {
    loadCart();
    updateCartUI();
    
    // Cart sayfasındaysa sepet içeriğini göster
    if (window.location.pathname.includes('/views/cart.html')) {
        displayCartItems();
    }
});

// Sepeti localStorage'dan yükle
function loadCart() {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
        cart = JSON.parse(savedCart);
    }
}

// Sepeti localStorage'a kaydet
function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Sepet UI'ını güncelle (navbar'daki sepet sayısı)
function updateCartUI() {
    const cartCountElements = document.querySelectorAll('.cart-count');
    const itemCount = cart.reduce((total, item) => total + item.quantity, 0);
    
    cartCountElements.forEach(element => {
        element.textContent = itemCount;
    });
}

// Ürünü sepete ekle
function addToCart(productId, name, price, image, quantity = 1) {
    // Eğer ürün zaten sepette varsa miktarını arttır
    const existingItemIndex = cart.findIndex(item => item.productId === productId);
    
    if (existingItemIndex !== -1) {
        cart[existingItemIndex].quantity += quantity;
    } else {
        // Yeni ürün ekle
        cart.push({
            productId,
            name,
            price,
            image,
            quantity
        });
    }
    
    // Sepeti kaydet ve UI'ı güncelle
    saveCart();
    updateCartUI();
    
    // Başarılı mesajı göster
    showNotification('Ürün sepete eklendi!');
}

// Sepetten ürün çıkar
function removeFromCart(productId) {
    cart = cart.filter(item => item.productId !== productId);
    saveCart();
    updateCartUI();
    
    // Eğer cart sayfasındaysak, görünümü güncelle
    if (window.location.pathname.includes('/views/cart.html')) {
        displayCartItems();
    }
}

// Ürün miktarını güncelle
function updateQuantity(productId, quantity) {
    const itemIndex = cart.findIndex(item => item.productId === productId);
    
    if (itemIndex !== -1) {
        if (quantity <= 0) {
            // Miktar 0 veya daha az ise ürünü sepetten kaldır
            removeFromCart(productId);
        } else {
            // Miktarı güncelle
            cart[itemIndex].quantity = quantity;
            saveCart();
            updateCartUI();
            
            // Cart sayfasındaysa görünümü güncelle
            if (window.location.pathname.includes('/views/cart.html')) {
                displayCartItems();
            }
        }
    }
}

// Sepet sayfasında ürünleri göster
function displayCartItems() {
    const cartContainer = document.getElementById('cart-items');
    if (!cartContainer) return;
    
    if (cart.length === 0) {
        cartContainer.innerHTML = '<div class="empty-cart">Sepetiniz boş.</div>';
        document.getElementById('cart-summary').style.display = 'none';
        return;
    }
    
    let cartHTML = '';
    let totalPrice = 0;
    
    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        totalPrice += itemTotal;
        
        cartHTML += `
            <div class="cart-item" data-id="${item.productId}">
                <img src="${item.image}" alt="${item.name}" class="cart-item-img">
                <div class="cart-item-details">
                    <h4>${item.name}</h4>
                    <div class="cart-item-price">${item.price.toFixed(2)} TL</div>
                </div>
                <div class="cart-item-quantity">
                    <button class="quantity-btn decrease">-</button>
                    <input type="number" value="${item.quantity}" min="1" class="quantity-input">
                    <button class="quantity-btn increase">+</button>
                </div>
                <div class="cart-item-total">${itemTotal.toFixed(2)} TL</div>
                <button class="remove-btn">Kaldır</button>
            </div>
        `;
    });
    
    cartContainer.innerHTML = cartHTML;
    
    // Sepet toplam bilgilerini göster
    document.getElementById('cart-summary').style.display = 'block';
    document.getElementById('subtotal').textContent = totalPrice.toFixed(2) + ' TL';
    document.getElementById('total').textContent = totalPrice.toFixed(2) + ' TL';
    
    // Event listener'ları ekle
    addCartEventListeners();
}

// Sepet sayfasındaki butonlara ve input'lara event listener'lar ekle
function addCartEventListeners() {
    // Ürün silme butonları
    document.querySelectorAll('.remove-btn').forEach(button => {
        button.addEventListener('click', (e) => {
            const cartItem = e.target.closest('.cart-item');
            const productId = cartItem.dataset.id;
            removeFromCart(productId);
        });
    });
    
    // Miktar azaltma butonları
    document.querySelectorAll('.quantity-btn.decrease').forEach(button => {
        button.addEventListener('click', (e) => {
            const cartItem = e.target.closest('.cart-item');
            const productId = cartItem.dataset.id;
            const inputElement = cartItem.querySelector('.quantity-input');
            const currentQuantity = parseInt(inputElement.value);
            if (currentQuantity > 1) {
                updateQuantity(productId, currentQuantity - 1);
                inputElement.value = currentQuantity - 1;
            }
        });
    });
    
    // Miktar arttırma butonları
    document.querySelectorAll('.quantity-btn.increase').forEach(button => {
        button.addEventListener('click', (e) => {
            const cartItem = e.target.closest('.cart-item');
            const productId = cartItem.dataset.id;
            const inputElement = cartItem.querySelector('.quantity-input');
            const currentQuantity = parseInt(inputElement.value);
            updateQuantity(productId, currentQuantity + 1);
            inputElement.value = currentQuantity + 1;
        });
    });
    
    // Miktar input'ları
    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', (e) => {
            const cartItem = e.target.closest('.cart-item');
            const productId = cartItem.dataset.id;
            const newQuantity = parseInt(e.target.value);
            if (newQuantity > 0) {
                updateQuantity(productId, newQuantity);
            } else {
                e.target.value = 1;
                updateQuantity(productId, 1);
            }
        });
    });
}

// Bildirim göster
function showNotification(message) {
    // Bildirim konteynerı mevcut değilse oluştur
    let notificationContainer = document.getElementById('notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'notification-container';
        document.body.appendChild(notificationContainer);
    }
    
    // Bildirim oluştur
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.innerHTML = message;
    
    // Bildirimi ekle
    notificationContainer.appendChild(notification);
    
    // 3 saniye sonra bildirim silinsin
    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => {
            notification.remove();
        }, 500);
    }, 3000);
}
