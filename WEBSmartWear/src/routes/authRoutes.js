const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const { requireAuth } = require('../middleware/authMiddleware');

// Register
router.post('/register', authController.register);

// Login
router.post('/login', authController.login);

// Get current user (protected route)
router.get('/me', requireAuth, authController.getCurrentUser);

// Update user profile (protected route)
router.put('/update-profile', requireAuth, authController.updateProfile);

module.exports = router; 