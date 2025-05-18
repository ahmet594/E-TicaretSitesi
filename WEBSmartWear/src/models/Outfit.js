const mongoose = require('mongoose');

const outfitSchema = new mongoose.Schema({
    productIds: [{
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Product',
        required: true
    }],
    createdAt: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('Outfit', outfitSchema); 