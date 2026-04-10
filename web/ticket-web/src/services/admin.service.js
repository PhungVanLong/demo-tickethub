// src/services/admin.service.js
import api from './api'

export const adminService = {
    // ── Events ──────────────────────────────────────────────────────────────
    async getAllEvents(params = {}) {
        const res = await api.get('/api/events', { params })
        return res.data
    },

    async getPendingEvents() {
        const res = await api.get('/api/events/pending')
        return res.data
    },

    async approveEvent(id, reason = '') {
        const res = await api.post(`/api/events/${id}/approve`, { reason })
        return res.data
    },

    async rejectEvent(id, reason = '') {
        const res = await api.post(`/api/events/${id}/reject`, { reason })
        return res.data
    },

    // ── Revenue & Payments ───────────────────────────────────────────────────
    /** GET /api/admin/revenue/summary?from=&to= */
    async getRevenueSummary(params = {}) {
        const res = await api.get('/api/admin/revenue/summary', { params })
        return res.data
    },

    /** GET /api/admin/payments?from=&to= */
    async getPayments(params = {}) {
        const res = await api.get('/api/admin/payments', { params })
        return res.data
    },

    // ── Payouts ──────────────────────────────────────────────────────────────
    /** GET /api/admin/payouts/organizers/{organizerId}/preview?from=&to= */
    async getPayoutPreview(organizerId, params = {}) {
        const res = await api.get(`/api/admin/payouts/organizers/${organizerId}/preview`, { params })
        return res.data
    },

    /** POST /api/admin/payouts/organizers/{organizerId} */
    async executePayout(organizerId, data) {
        const res = await api.post(`/api/admin/payouts/organizers/${organizerId}`, data)
        return res.data
    },

    // ── Users ────────────────────────────────────────────────────────────────
    async getUsers() {
        const res = await api.get('/api/users')
        return res.data
    },

    async getUsersByRole(role) {
        const res = await api.get(`/api/users/role/${role}`)
        return res.data
    },

    async searchUsers(term) {
        const res = await api.get('/api/users/search', { params: { term } })
        return res.data
    },

    async activateUser(userId) {
        const res = await api.post(`/api/users/${userId}/activate`)
        return res.data
    },

    async deactivateUser(userId) {
        const res = await api.post(`/api/users/${userId}/deactivate`)
        return res.data
    },

    async promoteOrganizer(userId) {
        const res = await api.post(`/api/users/${userId}/promote-organizer`)
        return res.data
    },

    async demoteCustomer(userId) {
        const res = await api.post(`/api/users/${userId}/demote-customer`)
        return res.data
    },

    async deleteUser(userId) {
        await api.delete(`/api/users/${userId}`)
    },

    // ── Platform Stats ───────────────────────────────────────────────────────
    /** GET /api/stats/platform */
    async getPlatformStats() {
        const res = await api.get('/api/stats/platform')
        return res.data
    },

    // ── All Orders (admin) ───────────────────────────────────────────────────
    async getAllOrders(params = {}) {
        const res = await api.get('/api/orders', { params })
        return res.data
    },

    async getOrdersByStatus(status) {
        const res = await api.get(`/api/orders/status/${status}`)
        return res.data
    },
}
