const mongoose = require('mongoose');
const Product = require('../models/Product');

// MongoDB bağlantısı
mongoose.connect('mongodb://localhost:27017/smartwear', {
    useNewUrlParser: true,
    useUnifiedTopology: true
});

// Varsayılan beden setleri
const defaultSizes = {
    'Giyim': ['XS', 'S', 'M', 'L', 'XL'],
    'Ayakkabı': ['36', '37', '38', '39', '40', '41', '42', '43', '44', '45'],
    'Aksesuar': [] // Aksesuarlar için beden yok
};

async function updateProducts() {
    try {
        // Tüm ürünleri getir
        const products = await Product.find();
        
        // Her ürün için beden setini güncelle
        for (const product of products) {
            // Ürünün kategorisine göre varsayılan bedenleri ata
            product.sizes = defaultSizes[product.category] || [];
            
            // Ürünü kaydet
            await product.save();
            console.log(`✅ Ürün güncellendi: ${product.name}`);
        }
        
        console.log('✨ Tüm ürünler başarıyla güncellendi!');
        process.exit(0);
    } catch (error) {
        console.error('❌ Hata:', error);
        process.exit(1);
    }
}

// Scripti çalıştır
updateProducts(); 