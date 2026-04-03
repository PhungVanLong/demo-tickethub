// src/services/auth.service.js
import api from './api'

export const authService = {
    /**
     * POST /api/auth/login
     * Expected response: { token, user: { id, name|fullName, email, role, avatar }, refreshToken? }
     * JWT must include: {sub, email, role, exp, iat}
     */
    async login(email, password) {
        const res = await api.post('/api/auth/login', { email, password })
        return res.data
    },

    /**
     * POST /api/auth/register
     * Expected response: { token, user: {...}, refreshToken? }
     */
    async register(data) {
        const res = await api.post('/api/auth/register', data)
        return res.data
    },

    /**
     * POST /api/auth/refresh
     * Expected response: { token, refreshToken? }
     * Use to refresh access token before expiration
     */
    async refreshToken(refreshToken) {
        const res = await api.post('/api/auth/refresh', { refreshToken })
        return res.data
    },

    /**
     * POST /api/auth/logout
     * Invalidates token on backend
     */
    async logout() {
        try {
            await api.post('/api/auth/logout')
        } catch (error) {
            // Ignore logout errors, client-side cleanup still happens
            console.warn('Logout request failed:', error.message)
        }
    },

    /**
     * GET /api/dashboard — returns authenticated user context + role-aware stats
     */
    async getMe() {
        const res = await api.get('/api/dashboard')
        return res.data
    },

    /**
     * GET /api/user/profile
     */
    async getProfile() {
        const res = await api.get('/api/user/profile')
        return res.data
    },

    /**
     * PUT /api/user/profile
     */
    async updateProfile(data) {
        const res = await api.put('/api/user/profile', data)
        return res.data
    },
}
