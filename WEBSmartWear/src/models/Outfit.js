const mongoose = require('mongoose');

const outfitSchema = new mongoose.Schema({
    productIds: {
        type: [String],
        required: true,
        validate: {
            validator: function(v) {
                return v.length > 0 && v.length <= 10; // Bir kombinde en az 1, en fazla 10 ürün olabilir
            },
            message: 'Bir kombinde en az 1, en fazla 10 ürün olmalıdır'
        }
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
});

// Aynı ürün kombinasyonunun tekrar oluşturulmasını engelle
outfitSchema.index({ productIds: 1 }, { unique: true });

module.exports = mongoose.model('Outfit', outfitSchema); 