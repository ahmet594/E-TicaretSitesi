const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
    name: {
        type: String,
        required: true,
        trim: true
    },
    description: {
        type: String,
        required: true
    },
    price: {
        type: 'Double', // MongoDB Double veri tipi
        required: true,
        min: 0
    },
    category: {
        type: String,
        required: true,
        enum: ['Giyim', 'Ayakkabı', 'Aksesuar']
    },
    subcategory: {
        type: String,
        default: ''
    },
    image: {
        type: String,
        required: true
    },
    stock: {
        type: Number,
        required: true,
        min: 0
    },
    featured: {
        type: Boolean,
        default: false
    },
    color: {
        type: String,
        default: ''
    },
    size: {
        type: String,
        default: '',
        validate: {
            validator: function(size) {
                // Giyim kategorisi için zorunlu
                if (this.category === 'Giyim' && (!size || size === '')) {
                    return false;
                }
                return true;
            },
            message: 'Giyim ürünleri için beden bilgisi gereklidir.'
        }
    },
    brand: {
        type: String,
        default: 'SmartWear'
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
}, {
    toJSON: { getters: true },
    toObject: { getters: true }
});

// Virtuals
productSchema.virtual('availableSizes').get(function() {
    return this.size ? this.size.split(',') : [];
});

// Middleware
productSchema.pre('save', function(next) {
    // Giyim kategorisi için varsayılan bedenler
    if (this.category === 'Giyim' && (!this.size || this.size === '')) {
        this.size = 'XS,S,M,L,XL';
    }
    next();
});

module.exports = mongoose.model('Product', productSchema);
