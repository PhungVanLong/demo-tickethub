import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import platformSalesService from '@/services/platformSales.service'

export const usePlatformSalesStore = defineStore('platformSales', () => {
  // State
  const activeSales = ref([])
  const allSales = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Computed
  const bestDiscount = computed(() => {
    if (activeSales.value.length === 0) return null
    return activeSales.value.reduce((best, sale) =>
      sale.discountPercentage > best.discountPercentage ? sale : best
    )
  })

  const hasActiveSales = computed(() => activeSales.value.length > 0)

  const inactiveSales = computed(() =>
    allSales.value.filter(sale => !sale.isActive)
  )

  // Actions
  async function fetchActiveSales() {
    loading.value = true
    error.value = null
    try {
      const response = await platformSalesService.getActiveSales()
      activeSales.value = response.data || response
      return activeSales.value
    } catch (err) {
      // Don't throw - graceful fallback for public endpoint
      error.value = null
      activeSales.value = []
    } finally {
      loading.value = false
    }
  }

  async function fetchAllSales(params = {}) {
    loading.value = true
    error.value = null
    try {
      const response = await platformSalesService.getAllSales(params)
      allSales.value = response.data || response
      return allSales.value
    } catch (err) {
      error.value = err.message
      console.error('Failed to fetch all sales:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function createSale(payload) {
    loading.value = true
    error.value = null
    try {
      const response = await platformSalesService.createSale(payload)
      await fetchAllSales()
      return response.data || response
    } catch (err) {
      error.value = err.message
      console.error('Failed to create sale:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateSale(saleId, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await platformSalesService.updateSale(saleId, payload)
      await fetchAllSales()
      return response.data || response
    } catch (err) {
      error.value = err.message
      console.error('Failed to update sale:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function deactivateSale(saleId) {
    loading.value = true
    error.value = null
    try {
      await platformSalesService.deactivateSale(saleId)
      await fetchAllSales()
    } catch (err) {
      error.value = err.message
      console.error('Failed to deactivate sale:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    activeSales,
    allSales,
    loading,
    error,

    // Computed
    bestDiscount,
    hasActiveSales,
    inactiveSales,

    // Actions
    fetchActiveSales,
    fetchAllSales,
    createSale,
    updateSale,
    deactivateSale
  }
})
