// src/services/api.js — Axios instance with JWT interceptors + auto-refresh
import axios from 'axios'
import { isTokenExpired } from '@/utils/jwt'

const api = axios.create({
    baseURL: 'http://localhost:8081',
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
})

// Flag to prevent multiple refresh requests
let isRefreshing = false
let refreshSubscribers = []

const onRefreshed = (token) => {
    refreshSubscribers.forEach(cb => cb(token))
    refreshSubscribers = []
}

const addRefreshSubscriber = (callback) => {
    refreshSubscribers.push(callback)
}

// ── Request interceptor: attach Bearer token + check expiration ───────────────
api.interceptors.request.use(
    async (config) => {
        const token = localStorage.getItem('auth_token')

        // Attach token if available
        if (token) {
            config.headers.Authorization = `Bearer ${token}`

            // Check if token is expired and refresh if needed (for non-refresh endpoints)
            if (isTokenExpired(token) && !config.url?.includes('/auth/refresh')) {
                if (!isRefreshing) {
                    isRefreshing = true

                    try {
                        const refreshToken = localStorage.getItem('refresh_token')
                        if (refreshToken) {
                            const response = await api.post('/api/auth/refresh', { refreshToken })
                            const newToken = response.data.token || response.data.accessToken

                            if (newToken) {
                                localStorage.setItem('auth_token', newToken)
                                config.headers.Authorization = `Bearer ${newToken}`
                                onRefreshed(newToken)
                            } else {
                                throw new Error('No token in refresh response')
                            }
                        } else {
                            throw new Error('No refresh token available')
                        }
                    } catch (error) {
                        console.error('Token refresh failed:', error.message)
                        localStorage.removeItem('auth_token')
                        localStorage.removeItem('refresh_token')
                        window.location.href = '/login'
                        return Promise.reject(error)
                    } finally {
                        isRefreshing = false
                    }
                } else {
                    // Queue the request to retry after refresh
                    return new Promise((resolve) => {
                        addRefreshSubscriber((newToken) => {
                            config.headers.Authorization = `Bearer ${newToken}`
                            resolve(config)
                        })
                    })
                }
            }
        }

        return config
    },
    (error) => Promise.reject(error)
)

// ── Response interceptor: handle 401 + normalize errors ─────────────────────
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('auth_token')
            localStorage.removeItem('refresh_token')

            // Only redirect if not already on /login
            if (!window.location.pathname.startsWith('/login')) {
                window.location.href = '/login'
            }
        }

        return Promise.reject(error)
    }
)

export default api
