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
    combinationCode: {
        type: String,
        enum: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13'],
        required: true,
        description: {
            '1': 'Ayakkabı',
            '2': 'Pantolon',
            '3': 'Kemer',
            '4': 'Basic Tişört',
            '5': 'Basic Üstüne Giyilebilir',
            '6': 'Tekli Üst Giyim',
            '7': 'Ceket/Mont',
            '8': 'Atkı',
            '9': 'Şapka',
            '10': 'Saat',
            '11': 'Takım',
            '12': 'Cüzdan',
            '13': 'Eldiven'
        }
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
