// src/stores/auth.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/auth.service'

// ── Field normaliser ──────────────────────────────────────────────────────────
function normalizeUser(u) {
    if (!u) return null
    return {
        id: u.id,
        name: u.name || u.fullName || u.username || u.email,
        email: u.email,
        avatar: u.avatar || u.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(u.email)}`,
        role: (u.role || 'user').toUpperCase(),
        joinedAt: u.joinedAt || u.createdAt,
    }
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('auth_token') || null)
    const loading = ref(false)
    const error = ref(null)

    const isLoggedIn = computed(() => !!token.value && !!user.value)
    const isAdmin = computed(() => ['ADMIN', 'admin'].includes(user.value?.role))
    const isOrganizer = computed(() => ['ORGANIZER', 'organizer', 'ADMIN', 'admin'].includes(user.value?.role))

    // ── Actions ────────────────────────────────────────────────────────────────
    async function login(email, password) {
        loading.value = true
        error.value = null
        try {
            const data = await authService.login(email, password)
            const rawToken = data.token || data.accessToken || data.jwt
            const rawUser = data.user || data

            if (!rawToken) throw new Error('No token received from server')

            token.value = rawToken
            localStorage.setItem('auth_token', rawToken)
            user.value = normalizeUser(rawUser)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || e.message || 'Login failed'
            return false
        } finally {
            loading.value = false
        }
    }

    async function register(formData) {
        loading.value = true
        error.value = null
        try {
            const data = await authService.register(formData)
            const rawToken = data.token || data.accessToken || null
            const rawUser = data.user || data

            if (rawToken) {
                token.value = rawToken
                localStorage.setItem('auth_token', rawToken)
            }
            user.value = normalizeUser(rawUser)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || e.message || 'Registration failed'
            return false
        } finally {
            loading.value = false
        }
    }

    async function fetchMe() {
        if (!token.value) return
        try {
            const data = await authService.getMe()
            user.value = normalizeUser(data.user || data)
        } catch {
            logout()
        }
    }

    async function updateProfile(formData) {
        loading.value = true
        error.value = null
        try {
            const updated = await authService.updateProfile(formData)
            user.value = normalizeUser(updated.user || updated)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Profile update failed'
            return false
        } finally {
            loading.value = false
        }
    }

    function logout() {
        user.value = null
        token.value = null
        localStorage.removeItem('auth_token')
    }

    /** DEV ONLY — toggle admin role without re-login */
    function switchRole() {
        if (user.value) {
            user.value = { ...user.value, role: user.value.role === 'ADMIN' ? 'USER' : 'ADMIN' }
        }
    }

    return {
        user, token, loading, error,
        isLoggedIn, isAdmin, isOrganizer,
        login, register, fetchMe, updateProfile, logout, switchRole,
    }
})
