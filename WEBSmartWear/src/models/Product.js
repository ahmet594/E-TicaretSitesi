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
        type: Number,
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
    sizes: {
        type: [String],
        default: [],
        validate: {
            validator: function(sizes) {
                // Giyim kategorisi için zorunlu
                if (this.category === 'Giyim' && (!sizes || sizes.length === 0)) {
                    return false;
                }
                return true;
            },
            message: 'Giyim ürünleri için en az bir beden seçeneği gereklidir.'
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
});

// Virtuals
productSchema.virtual('availableSizes').get(function() {
    return this.sizes || [];
});

// Middleware
productSchema.pre('save', function(next) {
    // Giyim kategorisi için varsayılan bedenler
    if (this.category === 'Giyim' && (!this.sizes || this.sizes.length === 0)) {
        this.sizes = ['XS', 'S', 'M', 'L', 'XL'];
    }
    next();
});

module.exports = mongoose.model('Product', productSchema);
