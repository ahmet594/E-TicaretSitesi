const mongoose = require('mongoose');
const Product = require('../models/Product');
require('dotenv').config();

const sampleProducts = [
    {
        name: "Minimalist Beyaz Blazer",
        description: "Modern kesimli, rahat ve şık beyaz blazer",
        price: 1299.99,
        category: "Kadın",
        subcategory: "ceket",
        image: "/images/products/blazer.jpg",
        stock: 15,
        featured: true
    },
    {
        name: "Slim Fit Siyah Jean",
        description: "Yüksek kaliteli slim fit siyah jean",
        price: 799.99,
        category: "Erkek",
        subcategory: "kot-pantolon",

        image: "/images/products/jean.jpg",
        stock: 20,
        featured: true
    },
    {
        name: "Deri Cüzdan",
        description: "El yapımı deri cüzdan",
        price: 399.99,
        category: "Erkek",
        subcategory: "cüzdan",
        image: "/images/products/wallet.jpg",
        stock: 30,
        featured: true
    },
    {
        name: "Oversize T-shirt",
        description: "Rahat kesimli pamuklu t-shirt",
        price: 299.99,
        category: "Kadın",
        subcategory: "tshirt",
        image: "/images/products/tshirt.jpg",
        stock: 25,
        featured: true
    },
    {
        name: "Deri Ceket",
        description: "Klasik kesimli deri ceket",
        price: 2499.99,
        category: "Erkek",
        subcategory: "ceket",
        image: "/images/products/leather-jacket.jpg",
        stock: 10,
        featured: true
    },
    {
        name: "Minimalist Saat",
        description: "Modern tasarımlı minimalist saat",
        price: 899.99,
        category: "Erkek",
        subcategory: "saat",
        image: "/images/products/watch.jpg",
        stock: 15,
        featured: true
    },
    {
        name: "Mavi Kot Pantolon",
        description: "Rahat kesimli mavi kot pantolon",
        price: 599.99,
        category: "Erkek",
        subcategory: "kot-pantolon",
        image: "/images/products/jean.jpg",
        stock: 25,
        featured: false
    }
];

const seedProducts = async () => {
    try {
        await mongoose.connect(process.env.MONGODB_URI);
        console.log('Connected to MongoDB');

        // Clear existing products
        await Product.deleteMany({});
        console.log('Cleared existing products');

        // Insert new products
        const products = await Product.insertMany(sampleProducts);
        console.log(`Added ${products.length} products`);

        console.log('Database seeding completed');
        process.exit(0);
    } catch (error) {
        console.error('Error seeding database:', error);
        process.exit(1);
    }
};

seedProducts(); 