// Global Dependencies Manager
// Bu dosya tüm sayfalara dahil edilen bir JavaScript dosyasıdır
// Projenin ortak bağımlılıklarını yönetir

document.addEventListener('DOMContentLoaded', function() {
    // Global sepet yöneticisini yükle
    loadDependency('../js/global-cart.js');
    
    // Diğer küresel bağımlılıklar buraya eklenebilir
});

// Bağımlılık yükleyici
function loadDependency(path) {
    return new Promise((resolve, reject) => {
        // Eğer bu bağımlılık zaten yüklenmişse, yeniden yükleme
        if (document.querySelector(`script[src="${path}"]`)) {
            return resolve();
        }
        
        const script = document.createElement('script');
        script.src = path;
        script.onload = () => resolve();
        script.onerror = (err) => {
            console.error(`Bağımlılık yüklenemedi: ${path}`, err);
            reject(err);
        };
        
        document.body.appendChild(script);
    });
}

// Bu dosya kendisini sayfa tamamen yüklendiğinde çalıştıracak
window.addEventListener('load', function() {
    console.log('Global bağımlılıklar yüklendi.');
}); 