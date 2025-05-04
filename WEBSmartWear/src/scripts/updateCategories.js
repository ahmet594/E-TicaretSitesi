const mongoose = require('mongoose');
const Product = require('../models/Product');
require('dotenv').config();

const updateCategories = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('MongoDB\'ye bağlanıldı');

        // Tüm ürünleri getir
        const products = await Product.find();
        console.log(`${products.length} ürün bulundu`);

        let updatedCount = 0;

        // Her ürün için kategori güncellemesi yap
        for (const product of products) {
            if (product.category === 'Erkek') {
                product.category = 'Giyim';
                await product.save();
                updatedCount++;
                console.log(`✅ Güncellendi: ${product.name}`);
            }
        }

        console.log(`\n✨ Toplam ${updatedCount} ürün güncellendi`);
        process.exit(0);
    } catch (error) {
        console.error('Hata:', error);
        process.exit(1);
    }
};

// Scripti çalıştır
updateCategories(); 