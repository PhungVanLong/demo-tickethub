import api from './api'

export const ticketService = {
    async getMyTickets() {
        const res = await api.get('/api/tickets/me')
        return res.data
    },

    async getTicketsByOrder(orderId) {
        const res = await api.get(`/api/tickets/order/${orderId}`)
        return res.data
    },

    async getTicketById(ticketId) {
        const res = await api.get(`/api/tickets/${ticketId}`)
        return res.data
    },

    async useTicket(ticketId) {
        const res = await api.post(`/api/tickets/${ticketId}/use`)
        return res.data
    },

    async downloadTicket(ticketId) {
        const res = await api.get(`/api/tickets/${ticketId}/download`)
        return res.data
    },
}

export default ticketService