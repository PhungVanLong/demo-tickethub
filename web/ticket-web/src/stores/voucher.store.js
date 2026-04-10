import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import voucherService from '@/services/voucher.service'
import { extractApiError } from '@/utils/apiError'

function unwrapVoucherPayload(response) {
  return response?.data ?? response
}

function upsertVoucher(list, voucher) {
  if (!voucher || typeof voucher !== 'object') return list
  const voucherId = voucher.id ?? voucher.voucherId
  if (voucherId == null) return [voucher, ...list]

  const next = Array.isArray(list) ? [...list] : []
  const idx = next.findIndex((item) => String(item?.id ?? item?.voucherId) === String(voucherId))
  if (idx === -1) next.unshift(voucher)
  else next[idx] = voucher
  return next
}

export const useVoucherStore = defineStore('voucher', () => {
  // State
  const myVouchers = ref([])
  const organizerVouchers = ref([])
  const eventVouchers = ref({}) // { eventId: [vouchers] }
  const loading = ref(false)
  const error = ref(null)
  const platformSaleVouchers = ref([])
  // Lấy voucher platform sale đang active
  async function fetchPlatformSaleVouchers() {
    try {
      const response = await voucherService.getActivePlatformSaleVouchers()
      platformSaleVouchers.value = Array.isArray(response) ? response : []
      return platformSaleVouchers.value
    } catch (err) {
      console.error('Failed to fetch platform sale vouchers:', err)
      platformSaleVouchers.value = []
      throw err
    }
  }

  // Computed
  const monthlyVouchers = computed(() =>
    myVouchers.value.filter(v => v.voucherType === 'USER_MONTHLY')
  )

  const activeVouchers = computed(() =>
    myVouchers.value.filter(v => v.isActive && new Date(v.validUntil) > new Date())
  )

  const expiringSoon = computed(() =>
    activeVouchers.value.filter(v => {
      const expiryDate = new Date(v.validUntil)
      const threeDaysFromNow = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)
      return expiryDate <= threeDaysFromNow
    })
  )

  // Actions
  async function fetchMyVouchers() {
    loading.value = true
    error.value = null
    try {
      const response = await voucherService.getMyVouchers()
      myVouchers.value = unwrapVoucherPayload(response)
      return myVouchers.value
    } catch (err) {
      error.value = extractApiError(err, 'Failed to fetch my vouchers').message
      console.error('Failed to fetch my vouchers:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchOrganizerVouchers() {
    loading.value = true
    error.value = null
    try {
      const response = await voucherService.getOrganizerVouchers()
      organizerVouchers.value = unwrapVoucherPayload(response)
      return organizerVouchers.value
    } catch (err) {
      error.value = extractApiError(err, 'Failed to fetch organizer vouchers').message
      console.error('Failed to fetch organizer vouchers:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function fetchEventVouchers(eventId) {
    try {
      const response = await voucherService.getEventVouchers(eventId)
      eventVouchers.value[eventId] = unwrapVoucherPayload(response)
      return eventVouchers.value[eventId]
    } catch (err) {
      console.error(`Failed to fetch vouchers for event ${eventId}:`, err)
      throw err
    }
  }

  async function createEventVoucher(eventId, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await voucherService.createEventVoucher(eventId, payload)
      const createdVoucher = unwrapVoucherPayload(response)

      organizerVouchers.value = upsertVoucher(organizerVouchers.value, createdVoucher)

      // Refresh is best-effort only. Creation should be considered successful
      // even when organizer listing endpoint is unstable.
      try {
        await fetchOrganizerVouchers()
      } catch (refreshErr) {
        console.warn('Voucher created but refresh organizer vouchers failed:', refreshErr)
      }

      error.value = null
      return createdVoucher
    } catch (err) {
      error.value = extractApiError(err, 'Failed to create event voucher').message
      console.error('Failed to create event voucher:', err)
      throw new Error(error.value)
    } finally {
      loading.value = false
    }
  }

  async function createPlatformVoucher(payload) {
    loading.value = true
    error.value = null
    try {
      const response = await voucherService.createPlatformVoucher(payload)
      return unwrapVoucherPayload(response)
    } catch (err) {
      error.value = extractApiError(err, 'Failed to create platform voucher').message
      console.error('Failed to create platform voucher:', err)
      throw new Error(error.value)
    } finally {
      loading.value = false
    }
  }

  async function deleteEventVoucher(voucherId) {
    loading.value = true
    error.value = null
    try {
      await voucherService.deleteEventVoucher(voucherId)
      // Refresh organizer vouchers
      await fetchOrganizerVouchers()
    } catch (err) {
      error.value = extractApiError(err, 'Failed to delete voucher').message
      console.error('Failed to delete voucher:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function updateEventVoucher(voucherId, payload) {
    loading.value = true
    error.value = null
    try {
      const response = await voucherService.updateEventVoucher(voucherId, payload)
      // Refresh organizer vouchers
      await fetchOrganizerVouchers()
      return unwrapVoucherPayload(response)
    } catch (err) {
      error.value = extractApiError(err, 'Failed to update voucher').message
      console.error('Failed to update voucher:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  async function validateVoucher(code, context = {}) {
    try {
      const response = await voucherService.validateVoucher(code, context)
      return unwrapVoucherPayload(response)
    } catch (err) {
      console.error('Failed to validate voucher:', err)
      throw err
    }
  }

  return {
    // State
    myVouchers,
    organizerVouchers,
    eventVouchers,
    platformSaleVouchers,
    loading,
    error,

    // Computed
    monthlyVouchers,
    activeVouchers,
    expiringSoon,

    // Actions
    fetchMyVouchers,
    fetchOrganizerVouchers,
    fetchEventVouchers,
    fetchPlatformSaleVouchers,
    createEventVoucher,
    createPlatformVoucher,
    deleteEventVoucher,
    updateEventVoucher,
    validateVoucher
  }
})
