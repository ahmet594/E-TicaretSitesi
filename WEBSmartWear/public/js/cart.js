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
        try {
            const parsedCart = JSON.parse(savedCart);
            
            // Hatalı verileri temizle
            cart = parsedCart.filter(item => {
                // Gerekli alanların varlığını kontrol et
                const isValid = item && 
                               typeof item === 'object' && 
                               item.productId && 
                               item.name && 
                               !isNaN(Number(item.price)) &&
                               item.quantity > 0;
                
                return isValid;
            });
            
            // Eğer temizleme işlemi sonucunda kart değiştiyse kaydet
            if (cart.length !== parsedCart.length) {
                saveCart();
            }
        } catch (error) {
            console.error('Sepet yüklenirken hata oluştu:', error);
            cart = [];
            localStorage.removeItem('cart');
        }
    }
}

// Sepeti localStorage'a kaydet
function saveCart() {
    localStorage.setItem('cart', JSON.stringify(cart));
    
    // Debug için
    console.log('Sepet kaydedildi:', cart);
    console.log('Toplam ürün sayısı:', cart.reduce((total, item) => {
        return total + (item.quantity || 1);
    }, 0));
}

// Sepet UI'ını güncelle (navbar'daki sepet sayısı)
function updateCartUI() {
    const cartCountElements = document.querySelectorAll('.cart-count');
    if (!cartCountElements || cartCountElements.length === 0) return;
    
    try {
        // Sepetteki toplam ürün sayısını hesapla
        const itemCount = cart.reduce((total, item) => {
            // Quantity kontrolü (NaN veya negatif değerler için koruma)
            if (item && typeof item.quantity === 'number' && !isNaN(item.quantity) && item.quantity > 0) {
                return total + item.quantity;
            } else if (item) {
                return total + 1; // Eğer miktar bilgisi yoksa, 1 kabul et
            }
            return total;
        }, 0);
        
        console.log('Cart güncellendi, toplam ürün sayısı:', itemCount);
        
        // Tüm sepet sayı gösterimlerini güncelle
        cartCountElements.forEach(element => {
            if (element) {
                // Her zaman sayıyı göster (0 bile olsa)
                element.textContent = itemCount;
                
                // Badge'ı görünür yap (0 bile olsa)
                element.classList.remove('hidden');
                
                // Badge elementini sayfada görünür kıl
                const parentBadge = element.closest('.badge');
                if (parentBadge) {
                    parentBadge.classList.remove('hidden');
                }
            }
        });
        
        // localStorage'a güncel sepet içeriğini kaydet
        localStorage.setItem('cart', JSON.stringify(cart));
    } catch (error) {
        console.error('Sepet UI güncellenirken hata oluştu:', error);
    }
    
    // Ana sayfadaki sepet simgesini de güncelle (navbar global değişken)
    if (window.updateCartCount && typeof window.updateCartCount === 'function') {
        window.updateCartCount();
    }
}

// Ürünü sepete ekle
function addToCart(productId, name, price, image, quantity = 1) {
    if (!productId || !name || isNaN(Number(price))) {
        console.error('Sepete eklenemedi: Eksik veya geçersiz ürün bilgisi');
        return;
    }

    // Fiyatı sayıya dönüştür
    const numericPrice = Number(price);
    
    // Eğer ürün zaten sepette varsa miktarını arttır
    const existingItemIndex = cart.findIndex(item => item.productId === productId);
    
    if (existingItemIndex !== -1) {
        cart[existingItemIndex].quantity += quantity;
    } else {
        // Yeni ürün ekle
        cart.push({
            productId,
            name,
            price: numericPrice,
            image: image || '../img/product-placeholder.jpg',
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
        // Değerleri güvenli bir şekilde alalım
        const productId = item.productId || '';
        const name = item.name || 'Ürün Adı Yok';
        const safePrice = typeof item.price === 'number' && !isNaN(item.price) ? item.price : 0;
        const imageUrl = item.image || '../img/product-placeholder.jpg';
        const quantity = typeof item.quantity === 'number' && item.quantity > 0 ? item.quantity : 1;
        
        const itemTotal = safePrice * quantity;
        totalPrice += itemTotal;
        
        cartHTML += `
            <div class="cart-item" data-id="${productId}">
                <img src="${imageUrl}" alt="${name}" class="cart-item-img" onerror="this.src='../img/product-placeholder.jpg'">
                <div class="cart-item-details">
                    <h4>${name}</h4>
                    <div class="cart-item-price">${safePrice.toFixed(2)} TL</div>
                </div>
                <div class="cart-item-quantity">
                    <button class="quantity-btn decrease">-</button>
                    <input type="number" value="${quantity}" min="1" class="quantity-input">
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

// Sepeti tamamen temizler
function clearCart() {
    cart = [];
    saveCart();
    updateCartUI();
    if (window.location.pathname.includes('/views/cart.html')) {
        displayCartItems();
    }
}

window.addToCart = addToCart;
window.removeFromCart = removeFromCart;
window.updateQuantity = updateQuantity;
window.displayCartItems = displayCartItems;
window.clearCart = clearCart;

// Sayfa tam olarak yüklendiğinde sepet sayısını güncelle
window.onload = function() {
    updateCartUI();
    // Global updateCartCount fonksiyonunu da çağır (navbar.js'den)
    if (window.updateCartCount && typeof window.updateCartCount === 'function') {
        window.updateCartCount();
    }
};
