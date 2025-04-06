const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const morgan = require('morgan');
const path = require('path');
require('dotenv').config();

const connectDB = require('./src/config/db');
const productRoutes = require('./src/routes/productRoutes');
const authRoutes = require('./src/routes/authRoutes');

const app = express();

// CORS ayarları - Tüm kaynaklara izin ver (mobil uygulama için)
const corsOptions = {
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
};

// Connect to MongoDB
connectDB();

// Middleware
app.use(cors(corsOptions));
app.use(express.json());
app.use(morgan('dev'));

// Serve static files
app.use(express.static(path.join(__dirname, 'public')));

// Routes
app.use('/api/products', productRoutes);
app.use('/api/auth', authRoutes);

// API durum kontrolü
app.get('/api/status', (req, res) => {
    res.json({ 
        status: 'online', 
        message: 'SmartWear API is running', 
        timestamp: new Date() 
    });
});

// Serve index.html for root route
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'views', 'index.html'));
});

// Serve other HTML files from views directory
app.get('/views/:page', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'views', req.params.page));
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error(err.stack);
    res.status(500).json({ 
        message: 'Something went wrong!',
        error: process.env.NODE_ENV === 'development' ? err.message : 'Internal Server Error'
    });
});

// Server configuration
const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server is running on port ${PORT}`);
    console.log(`API is available at http://localhost:${PORT}/api`);
});
