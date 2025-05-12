// Global Sepet İşlemleri
// Bu dosya tüm sayfalarda yüklenir ve sepet bilgisini doğru şekilde günceller

document.addEventListener('DOMContentLoaded', function() {
    // Sayfa yüklendikten sonra sepet sayısını güncelle
    updateGlobalCartCount();
    
    // Sayfada olası görünüm değişiklikleri için periyodik kontrol (SPA benzeri davranış için)
    setInterval(updateGlobalCartCount, 2000);
});

// Global sepet sayısı güncelleyici
function updateGlobalCartCount() {
    try {
        const cartCountElements = document.querySelectorAll('.cart-count');
        if (!cartCountElements || cartCountElements.length === 0) return;
        
        const cartData = localStorage.getItem('cart');
        if (!cartData) {
            // Sepet boş veya mevcut değil
            updateCartCountBadges(0);
            return;
        }
        
        // Sepet verilerini ayrıştır
        const parsedCart = JSON.parse(cartData);
        if (!Array.isArray(parsedCart)) {
            console.error('Sepet verisi bir dizi değil:', parsedCart);
            updateCartCountBadges(0);
            return;
        }
        
        // Toplam ürün sayısını hesapla
        let totalItemCount = 0;
        parsedCart.forEach(item => {
            if (item && typeof item === 'object' && item.quantity && !isNaN(item.quantity)) {
                totalItemCount += Number(item.quantity);
            } else {
                totalItemCount += 1; // Eski format desteği
            }
        });
        
        // Tüm sepet badge'lerini güncelle
        updateCartCountBadges(totalItemCount);
        
        console.log('Global sepet güncellendi. Toplam ürün sayısı:', totalItemCount);
    } catch (error) {
        console.error('Global sepet güncellemesi sırasında hata:', error);
    }
}

// Tüm sepet badge'lerini günceller
function updateCartCountBadges(count) {
    const cartCountElements = document.querySelectorAll('.cart-count');
    cartCountElements.forEach(element => {
        element.textContent = count;
        
        // Badge'ı görünür yap
        element.classList.remove('hidden');
        
        // Badge'ın parent elementini de görünür yap (eğer bir badge sınıfı varsa)
        const parentBadge = element.closest('.badge');
        if (parentBadge) {
            parentBadge.classList.remove('hidden');
        }
    });
}

// Sayfa ziyaretinden sonra da her halükarda güncelle
window.addEventListener('load', function() {
    setTimeout(updateGlobalCartCount, 500);
}); 