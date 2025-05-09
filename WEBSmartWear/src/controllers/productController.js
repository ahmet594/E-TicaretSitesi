const Product = require('../models/Product');
const mongoose = require('mongoose');

// Geçerli alt kategori listesi
const SubcategoryLookup = {
    'Giyim': [
        't-shirt', 'gömlek', 'kazak', 'hırka', 'sweatshirt', 
        'mont', 'kaban-parka', 'ceket', 'kot-pantolon', 
        'kumaş-pantolon', 'şortlar', 'takım-elbise', 'eşofman', 'yelek'
    ],
    'Ayakkabı': [
        'Spor Ayakkabı', 'Sneaker', 'Bot', 'Klasik'
    ],
    'Aksesuar': [
        'Kemer', 'Kravat', 'Şapka', 'Atkı', 'Eldiven', 'Saat', 'Cüzdan'
    ]
};

// Tüm ürünleri getir
exports.getAllProducts = async (req, res) => {
    try {
        const products = await Product.find();
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Belirli bir ürünü ID'ye göre getir
exports.getProductById = async (req, res) => {
    try {
        const product = await Product.findById(req.params.id);
        
        if (!product) {
            return res.status(404).json({ message: 'Ürün bulunamadı' });
        }
        
        res.json(product);
    } catch (error) {
        // ID format hatası kontrolü
        if (error instanceof mongoose.Error.CastError) {
            return res.status(400).json({ message: 'Geçersiz ürün ID formatı' });
        }
        res.status(500).json({ message: error.message });
    }
};

// Kategoriye göre ürün getir
exports.getProductsByCategory = async (req, res) => {
    try {
        const category = req.params.category;
        
        // Geçerli kategorileri kontrol et
        const validCategories = ['Giyim', 'Ayakkabı', 'Aksesuar'];
        
        if (!validCategories.includes(category)) {
            return res.status(400).json({ message: 'Geçersiz kategori' });
        }
        
        const products = await Product.find({ category });
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Alt kategoriye göre ürün getir
exports.getProductsBySubcategory = async (req, res) => {
    try {
        const category = req.params.category;
        const subcategory = req.params.subcategory;
        
        // Geçerli kategorileri kontrol et
        const validCategories = ['Giyim', 'Ayakkabı', 'Aksesuar'];
        
        if (!validCategories.includes(category)) {
            return res.status(400).json({ message: 'Geçersiz kategori' });
        }
        
        // Geçerli alt kategori kontrolü
        if (subcategory !== 'Tümü' && SubcategoryLookup[category]) {
            if (!SubcategoryLookup[category].includes(subcategory)) {
                return res.status(400).json({ message: 'Geçersiz alt kategori' });
            }
        }
        
        // Hem kategori hem de alt kategori ile eşleşen ürünleri bul
        const products = await Product.find({ 
            category: category,
            subcategory: subcategory 
        });
        
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Yeni ürün ekle
exports.createProduct = async (req, res) => {
    try {
        const newProduct = new Product(req.body);
        const savedProduct = await newProduct.save();
        res.status(201).json(savedProduct);
    } catch (error) {
        // Validasyon hatası kontrolü
        if (error.name === 'ValidationError') {
            const validationErrors = Object.values(error.errors).map(err => err.message);
            return res.status(400).json({ message: 'Validasyon hatası', errors: validationErrors });
        }
        res.status(500).json({ message: error.message });
    }
};

// Ürün güncelle
exports.updateProduct = async (req, res) => {
    try {
        const updatedProduct = await Product.findByIdAndUpdate(
            req.params.id,
            req.body,
            { new: true, runValidators: true }
        );
        
        if (!updatedProduct) {
            return res.status(404).json({ message: 'Güncellenecek ürün bulunamadı' });
        }
        
        res.json(updatedProduct);
    } catch (error) {
        // ID format hatası kontrolü
        if (error instanceof mongoose.Error.CastError) {
            return res.status(400).json({ message: 'Geçersiz ürün ID formatı' });
        }
        
        // Validasyon hatası kontrolü
        if (error.name === 'ValidationError') {
            const validationErrors = Object.values(error.errors).map(err => err.message);
            return res.status(400).json({ message: 'Validasyon hatası', errors: validationErrors });
        }
        
        res.status(500).json({ message: error.message });
    }
};

// Ürün sil
exports.deleteProduct = async (req, res) => {
    try {
        const deletedProduct = await Product.findByIdAndDelete(req.params.id);
        
        if (!deletedProduct) {
            return res.status(404).json({ message: 'Silinecek ürün bulunamadı' });
        }
        
        res.json({ message: 'Ürün başarıyla silindi' });
    } catch (error) {
        // ID format hatası kontrolü
        if (error instanceof mongoose.Error.CastError) {
            return res.status(400).json({ message: 'Geçersiz ürün ID formatı' });
        }
        
        res.status(500).json({ message: error.message });
    }
};

// Ürün ara
exports.searchProducts = async (req, res) => {
    try {
        const searchQuery = req.query.query || req.query.q;
        
        if (!searchQuery) {
            return res.status(400).json({ message: 'Arama sorgusu gerekli' });
        }
        
        // İsim, açıklama ve markaya göre arama yap
        const products = await Product.find({
            $or: [
                { name: { $regex: searchQuery, $options: 'i' } },
                { description: { $regex: searchQuery, $options: 'i' } },
                { brand: { $regex: searchQuery, $options: 'i' } },
                { category: { $regex: searchQuery, $options: 'i' } },
                { subcategory: { $regex: searchQuery, $options: 'i' } }
            ]
        });
        
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Anlık arama önerileri (live search)
exports.liveSearchProducts = async (req, res) => {
    try {
        const searchQuery = req.query.query || req.query.q;
        
        if (!searchQuery || searchQuery.length < 2) {
            return res.status(400).json({ message: 'Arama sorgusu en az 2 karakter olmalıdır' });
        }
        
        // İsim, marka veya kategoriye göre sınırlı sayıda sonuç döndür
        const products = await Product.find({
            $or: [
                { name: { $regex: searchQuery, $options: 'i' } },
                { brand: { $regex: searchQuery, $options: 'i' } },
                { category: { $regex: searchQuery, $options: 'i' } }
            ]
        })
        .select('name image brand category price') // Sadece gerekli alanları seç
        .limit(5); // Maksimum 5 sonuç göster
        
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Öne çıkan ürünleri getir
exports.getFeaturedProducts = async (req, res) => {
    try {
        const featuredProducts = await Product.find({ featured: true }).limit(6);
        res.json(featuredProducts);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Yeni ürünleri getir (en son eklenenleri)
exports.getNewArrivals = async (req, res) => {
    try {
        const newArrivals = await Product.find()
            .sort({ createdAt: -1 })
            .limit(6);
        res.json(newArrivals);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// En çok satanları getir (satış sayısına göre)
exports.getBestSellers = async (req, res) => {
    try {
        const bestSellers = await Product.find()
            .sort({ salesCount: -1 })
            .limit(6);
        res.json(bestSellers);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};
