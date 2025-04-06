// Global filtreleme fonksiyonu
function filterByCategory(category) {
    console.log("Kategori filtre çağrıldı:", category);
    
    // Kategori seçimi işlemi için bir mini olay başlat
    setTimeout(function() {
        // window.categoryFilters varsa çağır (men-filters.js, women-filters.js, vb. yüklendikten sonra)
        if (window.filterByCategory) {
            window.filterByCategory(category);
        } else {
            console.log("filterByCategory henüz yüklenmedi, kategori seçimi kaydedildi");
            // Sayfa tam yüklendiğinde çalışacak bir event listener ekle
            window.addEventListener('load', function() {
                if (window.filterByCategory) {
                    window.filterByCategory(category);
                } else {
                    console.error("filterByCategory fonksiyonu bulunamadı!");
                }
            });
        }
    }, 100);
    
    // Varsayılan link davranışını engelle
    return false;
}

// Category filters fonksiyonunu global olarak tanımla
window.categoryFilters = {
    // Kategori id'sine göre filtreleme yapacak fonksiyon
    filterByCategory: filterByCategory,
    
    // Yardımcı fonksiyonlar
    initCategoryLinks: function(prefix) {
        // Tüm kategori linklerine tıklama olayı ekle
        const categoryLinks = document.querySelectorAll('.subcategory-filter');
        
        categoryLinks.forEach(link => {
            const category = link.getAttribute('data-category');
            
            // Eğer link zaten onclick özelliğine sahip değilse ekle
            if (!link.hasAttribute('onclick')) {
                link.setAttribute('onclick', `return filterByCategory('${category}')`);
                
                // Link tıklama olayını ekle
                link.addEventListener('click', function(e) {
                    e.preventDefault();
                    filterByCategory(category);
                });
            }
        });
        
        console.log(`${categoryLinks.length} kategori linki başlatıldı`);
    }
};

// Sayfa yüklendiğinde tüm kategori linklerini otomatik olarak başlat
document.addEventListener('DOMContentLoaded', function() {
    window.categoryFilters.initCategoryLinks();
}); 