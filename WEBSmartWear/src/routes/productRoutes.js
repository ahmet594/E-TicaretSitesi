const express = require('express');
const router = express.Router();
const productController = require('../controllers/productController');
const cors = require('cors');

// CORS ayarları - Mobil uygulama için gerekli
const corsOptions = {
    origin: '*', // Tüm kaynaklara izin ver
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
};

// Tüm rotalar için CORS'u etkinleştir
router.use(cors(corsOptions));

// Hata işleme middleware'i
const asyncHandler = fn => (req, res, next) => {
    Promise.resolve(fn(req, res, next)).catch(next);
};

// Get all products (with optional category filter)
router.get('/', asyncHandler(productController.getAllProducts));

// Get featured products
router.get('/featured', asyncHandler(productController.getFeaturedProducts));

// Search products
router.get('/search', asyncHandler(productController.searchProducts));

// Get products by category
router.get('/category/:category', asyncHandler(productController.getProductsByCategory));

// Get products by subcategory
router.get('/category/:category/subcategory/:subcategory', asyncHandler(productController.getProductsBySubcategory));

// Get single product
router.get('/:id', asyncHandler(productController.getProductById));

// Create product
router.post('/', asyncHandler(productController.createProduct));

// Update product
router.put('/:id', asyncHandler(productController.updateProduct));

// Delete product
router.delete('/:id', asyncHandler(productController.deleteProduct));

module.exports = router;
