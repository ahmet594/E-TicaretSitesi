const express = require('express');
const router = express.Router();
const Outfit = require('../models/Outfit');

// Yeni kombin oluştur
router.post('/', async (req, res) => {
    try {
        const { productIds } = req.body;
        
        if (!productIds || !Array.isArray(productIds) || productIds.length === 0) {
            return res.status(400).json({ message: 'Geçerli ürün ID\'leri gerekli' });
        }

        // ID'lerin geçerli MongoDB ObjectId formatında olduğunu kontrol et
        const isValidIds = productIds.every(id => id.match(/^[0-9a-fA-F]{24}$/));
        if (!isValidIds) {
            return res.status(400).json({ message: 'Geçersiz ürün ID formatı' });
        }

        console.log('Kaydedilecek ürün ID\'leri:', productIds); // Debug log

        const outfit = new Outfit({
            productIds: productIds
        });

        const savedOutfit = await outfit.save();
        console.log('Kaydedilen kombin:', savedOutfit); // Debug log

        res.status(201).json(savedOutfit);
    } catch (error) {
        console.error('Kombin kaydetme hatası:', error); // Debug log
        res.status(500).json({ 
            message: 'Kombin kaydedilirken bir hata oluştu',
            error: error.message 
        });
    }
});

// Tüm kombinleri getir
router.get('/', async (req, res) => {
    try {
        const outfits = await Outfit.find().populate('productIds');
        res.json(outfits);
    } catch (error) {
        console.error('Kombinleri getirme hatası:', error); // Debug log
        res.status(500).json({ message: error.message });
    }
});

module.exports = router; 