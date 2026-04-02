// src/services/auth.service.js
import api from './api'

export const authService = {
    /**
     * POST /api/auth/login
     * Expected response: { token, user: { id, name|fullName, email, role, avatar } }
     */
    async login(email, password) {
        const res = await api.post('/api/auth/login', { email, password })
        return res.data
    },

    /**
     * POST /api/auth/register
     * Expected response: { token, user: {...} }  OR just the user object
     */
    async register(data) {
        const res = await api.post('/api/auth/register', data)
        return res.data
    },

    /**
     * GET /api/auth/me — returns the authenticated user
     */
    async getMe() {
        const res = await api.get('/api/auth/me')
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
