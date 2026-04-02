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

    /** GET /api/events/categories */
    async getCategories() {
        const res = await api.get('/api/events/categories')
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
        return res.data?.data ?? res.data
    },

    /** Backward-compatible helper: returns first seat map when only one is expected */
    async getSeatMap(eventId) {
        const data = await this.getSeatMaps(eventId)
        const raw = Array.isArray(data) ? data : (data?.content ?? data?.data ?? data)
        return Array.isArray(raw) ? (raw[0] ?? null) : raw
    },

    /** POST /api/events/{eventId}/seat-maps (multipart/form-data) */
    async createSeatMap(eventId, data) {
        const form = new FormData()
        form.append('name', (data?.name || 'Main Hall').trim())

        if (data?.file) {
            form.append('file', data.file)
        } else if (data?.imageUrl) {
            form.append('imageUrl', data.imageUrl)
        }

        const res = await api.post(`/api/events/${eventId}/seat-maps`, form, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
        return res.data
    },

    /** DELETE /api/events/{eventId}/seat-maps/{seatMapId} */
    async deleteSeatMap(eventId, seatMapId) {
        await api.delete(`/api/events/${eventId}/seat-maps/${seatMapId}`)
    },

    /** PUT /api/events/{eventId}/seat-maps/{seatMapId}/image (multipart/form-data) */
    async uploadSeatMapImage(eventId, seatMapId, file) {
        const form = new FormData()
        form.append('file', file)
        const res = await api.put(`/api/events/${eventId}/seat-maps/${seatMapId}/image`, form, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
        return res.data
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

    /** GET /api/events/{eventId}/stats/sold-tickets */
    async getSoldTicketsStats(eventId) {
        const res = await api.get(`/api/events/${eventId}/stats/sold-tickets`)
        return res.data
    },

    /** GET /api/events/{eventId}/stats/total-tickets */
    async getTotalTicketsStats(eventId) {
        const res = await api.get(`/api/events/${eventId}/stats/total-tickets`)
        return res.data
    },
}
