import api from './api'

export const platformSalesService = {
  // Get active platform sales (public, no auth)
  async getActiveSales() {
    // Gọi đúng URL backend expose
    const res = await api.get('/api/admin/platform-sales/active')
    return res.data
  },

  // Get active platform sales (admin)
  async getActiveSalesAdmin() {
    const res = await api.get('/api/admin/platform-sales/active')
    return res.data
  },

  // Get all sales (admin)
  async getAllSales(params = {}) {
    const res = await api.get('/api/admin/platform-sales', { params })
    return res.data
  },

  // Create platform sale (admin)
  async createSale(payload) {
    const res = await api.post('/api/admin/platform-sales', payload)
    return res.data
  },

  // Update platform sale (admin)
  async updateSale(saleId, payload) {
    const res = await api.put(`/api/admin/platform-sales/${saleId}`, payload)
    return res.data
  },

  // Deactivate platform sale (admin)
  async deactivateSale(saleId) {
    const res = await api.delete(`/api/admin/platform-sales/${saleId}`)
    return res.data
  }
}

export default platformSalesService
