// src/services/booking.service.js
import api from './api'

export const bookingService = {
    /** GET /api/checkout/events/{eventId}/tiers — public, includes availability */
    async getCheckoutTiers(eventId) {
        const res = await api.get(`/api/checkout/events/${eventId}/tiers`)
        return res.data
    },

    /** POST /api/checkout/quote — preview price with optional voucher */
    async quote(data) {
        const res = await api.post('/api/checkout/quote', data)
        return res.data
    },

    /** POST /api/checkout/orders — create PENDING order + seat hold */
    async createOrder(data) {
        const res = await api.post('/api/checkout/orders', data)
        return res.data
    },

    /** POST /api/checkout/orders/{orderId}/payments — create payment intent */
    async createPaymentIntent(orderId, data) {
        const res = await api.post(`/api/checkout/orders/${orderId}/payments`, data)
        return res.data
    },

    /** POST /api/checkout/payments/webhook/fake — simulate payment result */
    async fakeWebhook(data) {
        const res = await api.post('/api/checkout/payments/webhook/fake', data)
        return res.data
    },

    /** GET /api/orders/me */
    async getMyOrders() {
        const res = await api.get('/api/orders/me')
        return res.data
    },

    /** GET /api/orders/{id} */
    async getOrderById(id) {
        const res = await api.get(`/api/orders/${id}`)
        return res.data
    },

    /** POST /api/orders/{id}/cancel */
    async cancelOrder(id) {
        const res = await api.post(`/api/orders/${id}/cancel`)
        return res.data
    },

    /** POST /api/orders/{id}/refund */
    async refundOrder(id) {
        const res = await api.post(`/api/orders/${id}/refund`)
        return res.data
    },
}
