const Product = require('../models/Product');
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

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

// Tek bir görsel yükleme
exports.uploadProductImage = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'Lütfen bir görsel seçin' });
        }

        // Yüklenen dosyanın URL'sini oluştur
        const imageUrl = `/images/products/${req.file.filename}`;
        
        res.status(200).json({ 
            message: 'Görsel başarıyla yüklendi', 
            imageUrl: imageUrl,
            filename: req.file.filename
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// Yeni ürün ekle
exports.createProduct = async (req, res) => {
    try {
        // Gelen verileri kontrol et
        console.log('createProduct fonksiyonu çağrıldı');
        console.log('Gelen ürün verileri:', req.body);
        console.log('Yüklenen görseller:', req.files);
        
        // Dosya yükleme kontrolü
        if (!req.files || req.files.length === 0) {
            console.error('Hiç görsel yüklenmedi!');
        } else {
            console.log('Yüklenen dosya sayısı:', req.files.length);
            req.files.forEach((file, index) => {
                console.log(`Dosya ${index + 1}:`, {
                    filename: file.filename,
                    originalname: file.originalname,
                    path: file.path,
                    size: file.size,
                    mimetype: file.mimetype
                });
            });
        }
        
        // Ürün verilerini al
        const productData = { ...req.body };
        
        // Yüklenen görselleri kontrol et
        if (req.files && req.files.length > 0) {
            // İlk görseli ana görsel olarak kullan
            productData.image = `/images/products/${req.files[0].filename}`;
            console.log('Ürün görseli olarak ayarlandı:', productData.image);
            
            // Diğer görseller için ek işlemler yapılabilir
            // Örneğin: productData.additionalImages = req.files.slice(1).map(file => `/images/products/${file.filename}`);
        } else {
            // Eğer görsel yüklenmemişse ve frontend'den image bilgisi geldiyse onu kullan
            if (!productData.image) {
                console.error('Ürün için görsel bulunamadı!');
                return res.status(400).json({ message: 'Ürün için en az bir görsel gereklidir' });
            }
        }
        
        console.log('Veritabanına kaydedilecek ürün verileri:', productData);
        
        const newProduct = new Product(productData);
        const savedProduct = await newProduct.save();
        console.log('Ürün başarıyla kaydedildi:', savedProduct._id);
        
        res.status(201).json(savedProduct);
    } catch (error) {
        console.error('Ürün ekleme hatası:', error);
        
        // Hata durumunda yüklenen görselleri temizle
        if (req.files && req.files.length > 0) {
            req.files.forEach(file => {
                console.log('Hata nedeniyle dosya siliniyor:', file.path);
                fs.unlink(file.path, (err) => {
                    if (err) console.error('Görsel silinirken hata:', err);
                });
            });
        }
        
        // Validasyon hatası kontrolü
        if (error.name === 'ValidationError') {
            const validationErrors = Object.values(error.errors).map(err => err.message);
            console.error('Validasyon hataları:', validationErrors);
            return res.status(400).json({ message: 'Validasyon hatası', errors: validationErrors });
        }
        res.status(500).json({ message: error.message });
    }
};

// Ürün güncelle
exports.updateProduct = async (req, res) => {
    try {
        // Gelen verileri kontrol et
        console.log('Güncelleme verileri:', req.body);
        console.log('Yüklenen görseller:', req.files);
        
        // Ürün verilerini al
        const productData = { ...req.body };
        
        // Yüklenen görselleri kontrol et
        if (req.files && req.files.length > 0) {
            // İlk görseli ana görsel olarak kullan
            productData.image = `/images/products/${req.files[0].filename}`;
            
            // Diğer görseller için ek işlemler yapılabilir
            // Örneğin: productData.additionalImages = req.files.slice(1).map(file => `/images/products/${file.filename}`);
        }
        
        const updatedProduct = await Product.findByIdAndUpdate(
            req.params.id,
            productData,
            { new: true, runValidators: true }
        );
        
        if (!updatedProduct) {
            return res.status(404).json({ message: 'Güncellenecek ürün bulunamadı' });
        }
        
        res.json(updatedProduct);
    } catch (error) {
        // Hata durumunda yüklenen görselleri temizle
        if (req.files && req.files.length > 0) {
            req.files.forEach(file => {
                fs.unlink(file.path, (err) => {
                    if (err) console.error('Görsel silinirken hata:', err);
                });
            });
        }
        
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
        
        // Ürün görselini sil
        if (deletedProduct.image) {
            const imagePath = path.join(__dirname, '../../public', deletedProduct.image);
            if (fs.existsSync(imagePath)) {
                fs.unlinkSync(imagePath);
            }
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
