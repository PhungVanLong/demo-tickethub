import api from './api'

function pruneEmptyFields(obj) {
  return Object.fromEntries(
    Object.entries(obj || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}

function normalizeDateTime(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(raw)) return `${raw}:00`
  return raw
}

function withSpaceDateTime(value) {
  const normalized = normalizeDateTime(value)
  return normalized ? normalized.replace('T', ' ') : normalized
}

function normalizeCreatePayload(payload) {
  const code = String(payload?.code || '').trim().toUpperCase()
  const name = String(payload?.name || '').trim()

  return {
    ...payload,
    code,
    name,
    discountType: String(payload?.discountType || '').trim().toUpperCase(),
    discountValue: Number(payload?.discountValue ?? 0),
    usageLimit: Number(payload?.usageLimit ?? 0),
    minOrderValue: Number(payload?.minOrderValue ?? 0),
    validFrom: normalizeDateTime(payload?.validFrom),
    validUntil: normalizeDateTime(payload?.validUntil),
  }
}

function buildCreatePayloadCandidates(payload) {
  const normalized = normalizeCreatePayload(payload)
  const candidates = []

  const type = normalized.discountType
  const typeCandidates = Array.from(new Set([
    type,
    type === 'PERCENTAGE' ? 'PERCENT' : null,
    type === 'PERCENT' ? 'PERCENTAGE' : null,
    type === 'FIXED_AMOUNT' ? 'FIXED' : null,
    type === 'FIXED' ? 'FIXED_AMOUNT' : null,
  ].filter(Boolean)))

  for (const discountType of typeCandidates) {
    candidates.push(pruneEmptyFields({
      ...normalized,
      discountType,
    }))

    candidates.push(pruneEmptyFields({
      ...normalized,
      discountType,
      validFrom: withSpaceDateTime(normalized.validFrom),
      validUntil: withSpaceDateTime(normalized.validUntil),
    }))
  }

  const unique = []
  const seen = new Set()
  for (const candidate of candidates) {
    const key = JSON.stringify(candidate)
    if (!seen.has(key)) {
      seen.add(key)
      unique.push(candidate)
    }
  }

  return unique
}

export const voucherService = {
  // Get user's personal vouchers (monthly + organizer)
  async getMyVouchers() {
    const res = await api.get('/api/vouchers/me')
    return res.data
  },

  // Get organizer's event vouchers
  async getOrganizerVouchers() {
    const res = await api.get('/api/vouchers/organizer')
    return res.data
  },

  // Create event voucher (organizer only)
  async createEventVoucher(eventId, payload) {
    const candidates = buildCreatePayloadCandidates(payload)
    let lastError = null

    for (const candidate of candidates) {
      try {
        const res = await api.post(`/api/vouchers/events/${eventId}`, candidate)
        return res.data
      } catch (err) {
        lastError = err
        const status = err?.response?.status
        if (status !== 400 && status !== 422) {
          throw err
        }
      }
    }

    throw lastError
  },

  // Get vouchers for specific event (public)
  async getEventVouchers(eventId) {
    const res = await api.get(`/api/vouchers/events/${eventId}`)
    return res.data
  },

  // Delete/disable event voucher
  async deleteEventVoucher(voucherId) {
    const res = await api.delete(`/api/vouchers/${voucherId}`)
    return res.data
  },

  // Update event voucher
  async updateEventVoucher(voucherId, payload) {
    const res = await api.put(`/api/vouchers/${voucherId}`, payload)
    return res.data
  },

  // Validate voucher code
  async validateVoucher(code) {
    const res = await api.post('/api/vouchers/validate', { code })
    return res.data
  }
}

export default voucherService
