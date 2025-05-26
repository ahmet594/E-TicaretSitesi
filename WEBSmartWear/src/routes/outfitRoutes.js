const express = require('express');
const router = express.Router();
const Outfit = require('../models/Outfit');

// Tüm kombinleri getir
router.get('/', async (req, res) => {
    try {
        const outfits = await Outfit.find().sort({ createdAt: -1 }); // En yeni kombinleri önce getir
        res.json(outfits);
    } catch (error) {
        console.error('Kombinleri getirme hatası:', error);
        res.status(500).json({ error: 'Kombinler getirilemedi' });
    }
});

// Yeni kombin oluştur
router.post('/', async (req, res) => {
    try {
        const { productIds } = req.body;
        
        if (!productIds || !Array.isArray(productIds) || productIds.length === 0) {
            return res.status(400).json({ error: 'Geçersiz ürün ID\'leri' });
        }

        const outfit = new Outfit({
            productIds,
            createdAt: new Date()
        });

        await outfit.save();
        res.status(201).json(outfit);
    } catch (error) {
        res.status(500).json({ error: 'Kombin oluşturulamadı' });
    }
});

module.exports = router; 