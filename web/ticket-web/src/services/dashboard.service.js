import api from './api'

export const dashboardService = {
    async getDashboard() {
        const res = await api.get('/api/dashboard')
        return res.data
    },
}
