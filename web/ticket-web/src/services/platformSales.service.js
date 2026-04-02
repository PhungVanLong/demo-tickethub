import api from './api'

export const platformSalesService = {
  // Get active platform sales (public, no auth)
  async getActiveSales() {
    const candidates = [
      '/api/platform-sales/active',
      '/api/admin/platform-sales/active',
    ]

    let lastError = null
    for (const url of candidates) {
      try {
        const res = await api.get(url)
        return res.data
      } catch (err) {
        lastError = err
      }
    }

    const status = lastError?.response?.status
    if (status === 401 || status === 403 || status === 404) {
      return []
    }

    throw lastError
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
