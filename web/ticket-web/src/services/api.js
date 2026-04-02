// src/services/api.js  — Axios instance with JWT interceptors
import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8081',
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
})

// ── Request: attach Bearer token ──────────────────────────────────────────────
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('auth_token')
        if (token) config.headers.Authorization = `Bearer ${token}`
        return config
    },
    (error) => Promise.reject(error)
)

// ── Response: normalise errors, auto-logout on 401 ────────────────────────────
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('auth_token')
            // Only redirect if not already on /login
            if (!window.location.pathname.startsWith('/login')) {
                window.location.href = '/login'
            }
        }
        return Promise.reject(error)
    }
)

export default api
