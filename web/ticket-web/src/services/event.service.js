// src/services/event.service.js
import api from './api'

export const eventService = {
    // ── Public event queries ─────────────────────────────────────────────────
    /** GET /api/events/published */
    async getPublished(params = {}) {
        const res = await api.get('/api/events/published', { params })
        return res.data
    },

    /** GET /api/events/search?term= */
    async search(term) {
        const res = await api.get('/api/events/search', { params: { term } })
        return res.data
    },

    /** GET /api/events/{id} */
    async getById(id) {
        const res = await api.get(`/api/events/${id}`)
        return res.data
    },

    /** GET /api/events/pending (admin) */
    async getPending() {
        const res = await api.get('/api/events/pending')
        return res.data
    },

    /** GET /api/events/organizer/{organizerId} */
    async getByOrganizer(organizerId) {
        const res = await api.get(`/api/events/organizer/${organizerId}`)
        return res.data
    },

    /** GET /api/events/city/{city} */
    async getByCity(city) {
        const res = await api.get(`/api/events/city/${city}`)
        return res.data
    },

    // ── Event CRUD ───────────────────────────────────────────────────────────
    /** POST /api/events — body: CreateEventRequest */
    async create(data) {
        const res = await api.post('/api/events', data)
        return res.data
    },

    /** PUT /api/events/{id} */
    async update(id, data) {
        const res = await api.put(`/api/events/${id}`, data)
        return res.data
    },

    /** DELETE /api/events/{id} */
    async deleteEvent(id) {
        await api.delete(`/api/events/${id}`)
    },

    // ── Approval ────────────────────────────────────────────────────────────
    /** POST /api/events/{id}/approve — body: { reason } optional */
    async approve(id, reason = '') {
        const res = await api.post(`/api/events/${id}/approve`, { reason })
        return res.data
    },

    /** POST /api/events/{id}/reject — body: { reason } */
    async reject(id, reason = '') {
        const res = await api.post(`/api/events/${id}/reject`, { reason })
        return res.data
    },

    // ── Ticket Tiers (public checkout tiers, includes availability) ───────────
    /** GET /api/checkout/events/{eventId}/tiers */
    async getTicketTiers(eventId) {
        const res = await api.get(`/api/checkout/events/${eventId}/tiers`)
        return res.data
    },

    // ── Seat Maps (organizer/admin management) ────────────────────────────────
    /** GET /api/events/{eventId}/seat-maps */
    async getSeatMaps(eventId) {
        const res = await api.get(`/api/events/${eventId}/seat-maps`)
        return res.data
    },

    /** POST /api/events/{eventId}/seat-maps */
    async createSeatMap(eventId, data) {
        const res = await api.post(`/api/events/${eventId}/seat-maps`, data)
        return res.data
    },

    /** DELETE /api/events/{eventId}/seat-maps/{seatMapId} */
    async deleteSeatMap(eventId, seatMapId) {
        await api.delete(`/api/events/${eventId}/seat-maps/${seatMapId}`)
    },

    // ── Tier Management (organizer/admin, scoped to seat map) ─────────────────
    /** GET /api/events/{eventId}/seat-maps/{seatMapId}/tiers */
    async getTiersBySeatMap(eventId, seatMapId) {
        const res = await api.get(`/api/events/${eventId}/seat-maps/${seatMapId}/tiers`)
        return res.data
    },

    /** POST /api/events/{eventId}/seat-maps/{seatMapId}/tiers */
    async createTier(eventId, seatMapId, data) {
        const res = await api.post(`/api/events/${eventId}/seat-maps/${seatMapId}/tiers`, data)
        return res.data
    },

    /** DELETE /api/events/{eventId}/seat-maps/{seatMapId}/tiers/{tierId} */
    async deleteTier(eventId, seatMapId, tierId) {
        await api.delete(`/api/events/${eventId}/seat-maps/${seatMapId}/tiers/${tierId}`)
    },
}
