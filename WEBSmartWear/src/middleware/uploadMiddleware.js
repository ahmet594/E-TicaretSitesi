const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Yükleme dizinini kontrol et ve oluştur
const createUploadDir = (dir) => {
    console.log(`Dizin kontrol ediliyor: ${dir}`);
    if (!fs.existsSync(dir)) {
        try {
            fs.mkdirSync(dir, { recursive: true });
            console.log(`Dizin oluşturuldu: ${dir}`);
        } catch (error) {
            console.error(`Dizin oluşturma hatası: ${error.message}`);
        }
    } else {
        console.log(`Dizin zaten mevcut: ${dir}`);
        // Dizin yazma izinlerini kontrol et
        try {
            const testFile = path.join(dir, '.test-write-permission');
            fs.writeFileSync(testFile, 'test');
            fs.unlinkSync(testFile);
            console.log(`Dizin yazılabilir: ${dir}`);
        } catch (error) {
            console.error(`Dizin yazma izni hatası: ${dir}, ${error.message}`);
        }
    }
};

// Ürün görselleri için depolama ayarları
const productStorage = multer.diskStorage({
    destination: function (req, file, cb) {
        console.log('Dosya yükleme isteği alındı:', file.originalname);
        const uploadDir = path.join(__dirname, '../../public/images/products');
        console.log('Yükleme dizini:', uploadDir);
        createUploadDir(uploadDir);
        cb(null, uploadDir);
    },
    filename: function (req, file, cb) {
        // Dosya adını düzenle: orijinal adı koru, boşlukları tire ile değiştir
        const fileName = file.originalname.replace(/\s+/g, '-');
        // Aynı isimde dosya olmaması için timestamp ekle
        const uniqueFileName = Date.now() + '-' + fileName;
        console.log('Oluşturulan dosya adı:', uniqueFileName);
        cb(null, uniqueFileName);
    }
});

// Dosya filtreleme (sadece görseller)
const fileFilter = (req, file, cb) => {
    // Kabul edilen dosya tipleri
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    
    console.log('Dosya tipi kontrolü:', file.mimetype);
    
    if (allowedTypes.includes(file.mimetype)) {
        console.log('Dosya kabul edildi:', file.originalname);
        cb(null, true); // Dosyayı kabul et
    } else {
        console.error('Dosya reddedildi:', file.originalname, file.mimetype);
        cb(new Error('Sadece resim dosyaları yüklenebilir (JPEG, JPG, PNG, GIF, WEBP)'), false);
    }
};

// Ürün görselleri için yükleme middleware'i
const uploadProductImages = multer({
    storage: productStorage,
    limits: {
        fileSize: 5 * 1024 * 1024 // 5MB
    },
    fileFilter: fileFilter
});

module.exports = {
    uploadProductImages
}; 