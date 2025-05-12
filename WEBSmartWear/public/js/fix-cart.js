/**
 * Sepet düzeltme ve temizleme aracı
 * Bu script, sepetteki bozuk veya geçersiz öğeleri temizler ve düzeltir.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Mevcut sepet içeriğini al
    fixCartData();
});

// Sepet verilerini düzelt
function fixCartData() {
    try {
        const rawCart = localStorage.getItem('cart');
        if (!rawCart) return;
        
        // Veriyi parse et
        let cartData;
        try {
            cartData = JSON.parse(rawCart);
            
            // Geçerli bir dizi değilse boş bir dizi ile değiştir
            if (!Array.isArray(cartData)) {
                localStorage.setItem('cart', JSON.stringify([]));
                console.log('Sepet verisi düzeltildi: Geçersiz veri yapısı');
                return;
            }
        } catch (e) {
            // JSON parse hatası, sepeti temizle
            localStorage.setItem('cart', JSON.stringify([]));
            console.log('Sepet verisi düzeltildi: JSON parse hatası');
            return;
        }
        
        // Hatalı öğeleri filtrele
        const validItems = cartData.filter(item => {
            if (!item || typeof item !== 'object') return false;
            
            // Gerekli alanları kontrol et
            const hasValidId = !!item.productId;
            const hasValidName = !!item.name;
            const hasValidPrice = !isNaN(Number(item.price));
            const hasValidQuantity = !isNaN(Number(item.quantity)) && Number(item.quantity) > 0;
            
            return hasValidId && hasValidName && hasValidPrice && hasValidQuantity;
        });
        
        // Temizlenmiş sepeti geri kaydet
        if (validItems.length !== cartData.length) {
            localStorage.setItem('cart', JSON.stringify(validItems));
            console.log(`Sepet düzeltildi: ${cartData.length - validItems.length} geçersiz öğe temizlendi.`);
        }
        
        // Sayfa yenilenmeli mi?
        if (window.location.pathname.includes('/views/cart.html') && validItems.length !== cartData.length) {
            showNotification('Sepetiniz temizlendi. Geçersiz ürünler kaldırıldı.');
            setTimeout(() => {
                window.location.reload();
            }, 2000);
        }
    } catch (error) {
        console.error('Sepet düzeltme hatası:', error);
    }
}

// Bildirim göster
function showNotification(message, type = 'success') {
    // Bildirim konteynerı mevcut değilse oluştur
    let notificationContainer = document.getElementById('notification-container');
    if (!notificationContainer) {
        notificationContainer = document.createElement('div');
        notificationContainer.id = 'notification-container';
        document.body.appendChild(notificationContainer);
    }
    
    // Bildirim oluştur
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
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
