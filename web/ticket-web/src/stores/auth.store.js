// src/stores/auth.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services/auth.service'
import { logAction } from '@/services/actionLogger'
import { extractApiError } from '@/utils/apiError'
import {
    decodeToken,
    isTokenExpired,
    verifyToken,
    getTokenUser,
    extractTokens
} from '@/utils/jwt'

// ── Field normaliser ──────────────────────────────────────────────────────────
function normalizeUser(u) {
    if (!u) return null
    return {
        id: u.id || u.userId,
        name: u.name || u.fullName || u.username || u.email,
        email: u.email,
        avatar: u.avatar || u.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(u.email)}`,
        role: (u.role || 'CUSTOMER').toUpperCase(),
        joinedAt: u.joinedAt || u.createdAt,
    }
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('auth_token') || null)
    const refreshToken = ref(localStorage.getItem('refresh_token') || null)
    const loading = ref(false)
    const error = ref(null)
    const tokenValidated = ref(false)

    const isLoggedIn = computed(() => !!token.value && !!user.value)
    const isAdmin = computed(() => {
        // Check from user object AND verify from JWT token
        const userRoleValid = ['ADMIN', 'admin'].includes(user.value?.role)
        const tokenUser = decodeToken(token.value)
        const tokenRoleValid = ['ADMIN', 'admin'].includes(tokenUser?.role)

        return userRoleValid && tokenRoleValid
    })
    const isOrganizer = computed(() => {
        const userRoleValid = ['ORGANIZER', 'organizer', 'ADMIN', 'admin'].includes(user.value?.role)
        const tokenUser = decodeToken(token.value)
        const tokenRoleValid = ['ORGANIZER', 'organizer', 'ADMIN', 'admin'].includes(tokenUser?.role)

        return userRoleValid && tokenRoleValid
    })

    // ── Actions ────────────────────────────────────────────────────────────────
    async function login(email, password) {
        loading.value = true
        error.value = null
        logAction('AUTH_LOGIN_START', { email })
        try {
            const data = await authService.login(email, password)
            const { accessToken, refreshToken: newRefreshToken } = extractTokens(data)
            const rawUser = data.user ? { ...data, ...data.user } : data

            if (!accessToken) throw new Error('No token received from server')

            // Verify JWT token
            const verification = verifyToken(accessToken)
            if (!verification.valid) {
                throw new Error(`Invalid JWT token: ${verification.error}`)
            }

            // Store tokens
            token.value = accessToken
            localStorage.setItem('auth_token', accessToken)

            if (newRefreshToken) {
                refreshToken.value = newRefreshToken
                localStorage.setItem('refresh_token', newRefreshToken)
            }

            // Extract user info from JWT payload (most reliable source)
            const tokenUser = getTokenUser(accessToken)
            user.value = normalizeUser({ ...rawUser, role: tokenUser?.role })
            tokenValidated.value = true

            logAction('AUTH_LOGIN_SUCCESS', { userId: user.value?.id, role: user.value?.role })
            return true
        } catch (e) {
            const parsed = extractApiError(e, 'Login failed')
            error.value = parsed.message
            logAction('AUTH_LOGIN_FAILED', { message: error.value })
            return false
        } finally {
            loading.value = false
        }
    }

    async function register(formData) {
        loading.value = true
        error.value = null
        logAction('AUTH_REGISTER_START', { email: formData?.email })
        try {
            const data = await authService.register(formData)
            const { accessToken, refreshToken: newRefreshToken } = extractTokens(data)
            const rawUser = data.user ? { ...data, ...data.user } : data

            if (accessToken) {
                // Verify JWT token
                const verification = verifyToken(accessToken)
                if (!verification.valid) {
                    throw new Error(`Invalid JWT token: ${verification.error}`)
                }

                token.value = accessToken
                localStorage.setItem('auth_token', accessToken)

                if (newRefreshToken) {
                    refreshToken.value = newRefreshToken
                    localStorage.setItem('refresh_token', newRefreshToken)
                }

                const tokenUser = getTokenUser(accessToken)
                user.value = normalizeUser({ ...rawUser, role: tokenUser?.role })
                tokenValidated.value = true
            } else {
                user.value = normalizeUser(rawUser)
            }

            logAction('AUTH_REGISTER_SUCCESS', { userId: user.value?.id, role: user.value?.role })
            return true
        } catch (e) {
            const parsed = extractApiError(e, 'Registration failed')
            error.value = parsed.message
            logAction('AUTH_REGISTER_FAILED', { message: error.value })
            return false
        } finally {
            loading.value = false
        }
    }

    async function validateToken() {
        if (!token.value) {
            logout()
            return false
        }

        // Check JWT expiration
        if (isTokenExpired(token.value)) {
            // Try to refresh if refresh token available
            if (refreshToken.value) {
                return await refreshAccessToken()
            } else {
                logout()
                return false
            }
        }

        // Verify token structure
        const verification = verifyToken(token.value)
        if (!verification.valid) {
            logAction('AUTH_TOKEN_INVALID', { error: verification.error })
            logout()
            return false
        }

        tokenValidated.value = true
        return true
    }

    async function refreshAccessToken() {
        if (!refreshToken.value) return false

        try {
            logAction('AUTH_REFRESH_TOKEN_START', {})
            const data = await authService.refreshToken(refreshToken.value)
            const { accessToken, refreshToken: newRefreshToken } = extractTokens(data)

            if (!accessToken) throw new Error('No token in refresh response')

            token.value = accessToken
            localStorage.setItem('auth_token', accessToken)

            if (newRefreshToken) {
                refreshToken.value = newRefreshToken
                localStorage.setItem('refresh_token', newRefreshToken)
            }

            tokenValidated.value = true
            logAction('AUTH_REFRESH_TOKEN_SUCCESS', {})
            return true
        } catch (e) {
            const parsed = extractApiError(e, 'Token refresh failed')
            logAction('AUTH_REFRESH_TOKEN_FAILED', { error: parsed.message })
            logout()
            return false
        }
    }

    async function fetchMe() {
        if (!token.value) return

        // Validate token first
        if (!(await validateToken())) return

        try {
            const data = await authService.getMe()
            const rawUser = data?.user || data?.data?.user || data?.data || data
            const tokenUser = getTokenUser(token.value)

            // Merge API user info with JWT role for consistency
            user.value = normalizeUser({ ...rawUser, role: tokenUser?.role || rawUser.role })
            tokenValidated.value = true
            logAction('AUTH_FETCH_ME_SUCCESS', { userId: user.value?.id, role: user.value?.role })
        } catch {
            logAction('AUTH_FETCH_ME_FAILED', {})
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
            error.value = extractApiError(e, 'Profile update failed').message
            return false
        } finally {
            loading.value = false
        }
    }

    async function logout() {
        logAction('AUTH_LOGOUT', { userId: user.value?.id })

        // Notify backend about logout
        if (token.value) {
            try {
                await authService.logout()
            } catch {
                // Ignore backend logout errors
            }
        }

        user.value = null
        token.value = null
        refreshToken.value = null
        tokenValidated.value = false
        localStorage.removeItem('auth_token')
        localStorage.removeItem('refresh_token')
    }

    /** DEV ONLY — toggle admin role without re-login */
    function switchRole() {

        if (user.value) {
            user.value = { ...user.value, role: user.value.role === 'ADMIN' ? 'USER' : 'ADMIN' }
        }
    }

    return {
        user, token, refreshToken, loading, error, tokenValidated,
        isLoggedIn, isAdmin, isOrganizer,
        login, register, fetchMe, updateProfile, logout, switchRole,
        validateToken, refreshAccessToken,
    }
})
