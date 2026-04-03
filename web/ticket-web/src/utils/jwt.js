// src/utils/jwt.js — JWT token utilities
import { jwtDecode } from 'jwt-decode'

/**
 * Decode JWT token and return payload
 * @param {string} token - JWT token string
 * @returns {object|null} Decoded payload or null if invalid
 */
export function decodeToken(token) {
    try {
        if (!token) return null
        return jwtDecode(token)
    } catch (error) {
        console.error('Failed to decode token:', error.message)
        return null
    }
}

/**
 * Check if token is expired
 * @param {string} token - JWT token string
 * @returns {boolean} True if expired, false otherwise
 */
export function isTokenExpired(token) {
    const payload = decodeToken(token)
    if (!payload || !payload.exp) return true

    // exp is in seconds, Date.now() is in milliseconds
    const expirationTime = payload.exp * 1000
    const currentTime = Date.now()

    // Consider token expired 1 minute before actual expiration
    const bufferTime = 60 * 1000

    return currentTime > expirationTime - bufferTime
}

/**
 * Get token expiration time (seconds until expiration)
 * @param {string} token - JWT token string
 * @returns {number|null} Seconds until expiration or null if invalid
 */
export function getTokenExpiresIn(token) {
    const payload = decodeToken(token)
    if (!payload || !payload.exp) return null

    const expirationTime = payload.exp * 1000
    const currentTime = Date.now()
    const secondsLeft = (expirationTime - currentTime) / 1000

    return Math.max(0, Math.ceil(secondsLeft))
}

/**
 * Extract user info from JWT token
 * @param {string} token - JWT token string
 * @returns {object|null} User info {id, email, role, name} or null
 */
export function getTokenUser(token) {
    const payload = decodeToken(token)
    if (!payload) return null

    return {
        id: payload.sub || payload.userId || payload.id,
        email: payload.email,
        role: payload.role || payload.roles?.[0] || 'CUSTOMER',
        name: payload.name || payload.fullName || 'Unknown',
        iat: payload.iat, // issued at
        exp: payload.exp, // expiration
    }
}

/**
 * Verify token structure and expiration
 * @param {string} token - JWT token string
 * @returns {{valid: boolean, error: string|null}}
 */
export function verifyToken(token) {
    if (!token || typeof token !== 'string') {
        return { valid: false, error: 'No token provided' }
    }

    const payload = decodeToken(token)
    if (!payload) {
        return { valid: false, error: 'Invalid token format' }
    }

    if (!payload.exp) {
        return { valid: false, error: 'Token has no expiration' }
    }

    if (isTokenExpired(token)) {
        return { valid: false, error: 'Token is expired' }
    }

    return { valid: true, error: null }
}

/**
 * Get tokens from response (supports multiple JWT response formats)
 * @param {object} data - API response data
 * @returns {{accessToken: string|null, refreshToken: string|null}}
 */
export function extractTokens(data) {
    if (!data) return { accessToken: null, refreshToken: null }

    return {
        accessToken: data.token || data.accessToken || data.jwt || null,
        refreshToken: data.refreshToken || data.refresh_token || null,
    }
}
