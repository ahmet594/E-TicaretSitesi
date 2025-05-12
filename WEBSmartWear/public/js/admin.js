// Admin Panel JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Kullanıcı rolünü kontrol et - sadece adminler erişebilir
    checkAdminAccess();

    // Admin paneli başlangıç işlemleri
    initAdminPanel();
});

// Sadece admin rolündeki kullanıcıların erişimine izin ver
function checkAdminAccess() {
    const userRole = localStorage.getItem('userRole');
    const isAdminPage = window.location.pathname.includes('/views/admin/');

    if (isAdminPage && userRole !== 'admin') {
        // Admin olmayan kullanıcıyı ana sayfaya yönlendir
        window.location.href = '/views/index.html';
        return false;
    }

    return true;
}

// Admin paneli başlangıç işlemleri
function initAdminPanel() {
    // Aktif sayfayı belirle
    highlightActivePage();
    
    // Çıkış butonunu ayarla
    setupLogoutButton();
    
    // Tarih bilgisini güncelle
    updateDateDisplay();
    
    // Dashboard verileri için canlı bir uygulama geliştirirken API ile bağlantı kurulacak
    // Bu bir demo olduğu için statik değerleri gösteriyoruz
    console.log('Admin paneli başlatıldı');
}

// Aktif sayfayı belirle ve menüde vurgula
function highlightActivePage() {
    const currentPage = window.location.pathname;
    const navLinks = document.querySelectorAll('.admin-nav li a');
    
    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.parentElement.classList.add('active');
        } else {
            link.parentElement.classList.remove('active');
        }
    });
}

// Çıkış butonunu ayarla
function setupLogoutButton() {
    const logoutBtn = document.getElementById('admin-logout-btn');
    
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            // LocalStorage'dan kimlik bilgilerini temizle
            localStorage.removeItem('token');
            localStorage.removeItem('userId');
            localStorage.removeItem('userName');
            localStorage.removeItem('userRole');
            
            // Ana sayfaya yönlendir
            window.location.href = '/views/index.html';
        });
    }
}

// Tarih bilgisini güncelle
function updateDateDisplay() {
    const dateElement = document.querySelector('#admin-date span');
    
    if (dateElement) {
        const now = new Date();
        dateElement.textContent = now.toLocaleDateString('tr-TR', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }
}

// Ürün istatistiklerini yükle (gerçek bir uygulamada API'den alınacak)
function loadProductStats() {
    // Burada gerçek bir uygulama için API çağrısı yapılacak
    // Demo için statik veriler kullanılıyor
    return {
        totalProducts: 278,
        outOfStock: 15,
        lowStock: 32,
        categories: {
            'Giyim': 68,
            'Ayakkabı': 42,
            'Aksesuar': 85
        }
    };
}

// Sipariş istatistiklerini yükle (gerçek bir uygulamada API'den alınacak)
function loadOrderStats() {
    // Burada gerçek bir uygulama için API çağrısı yapılacak
    // Demo için statik veriler kullanılıyor
    return {
        totalOrders: 42,
        processing: 8,
        shipped: 12,
        delivered: 20,
        canceled: 2
    };
}

// Kullanıcı istatistiklerini yükle (gerçek bir uygulamada API'den alınacak)
function loadUserStats() {
    // Burada gerçek bir uygulama için API çağrısı yapılacak
    // Demo için statik veriler kullanılıyor
    return {
        totalUsers: 155,
        newUsersToday: 5,
        newUsersThisWeek: 22,
        activeUsers: 98
    };
} 