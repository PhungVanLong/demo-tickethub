import api from './api'

function pruneEmptyFields(obj) {
  return Object.fromEntries(
    Object.entries(obj || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}

function toOptionalNumber(value) {
  if (value === '' || value === null || value === undefined) return undefined
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : undefined
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
    discountValue: toOptionalNumber(payload?.discountValue),
    usageLimit: toOptionalNumber(payload?.usageLimit),
    minOrderValue: toOptionalNumber(payload?.minOrderValue),
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
    const withEventId = {
      ...normalized,
      eventId: normalized?.eventId,
    }

    candidates.push(pruneEmptyFields({
      ...withEventId,
      discountType,
    }))

    candidates.push(pruneEmptyFields({
      ...withEventId,
      discountType,
      validFrom: withSpaceDateTime(normalized.validFrom),
      validUntil: withSpaceDateTime(normalized.validUntil),
    }))

    // Compatibility for backends using validTo + minOrderAmount naming.
    candidates.push(pruneEmptyFields({
      ...withEventId,
      discountType,
      validTo: normalized.validUntil,
      validUntil: undefined,
      minOrderAmount: normalized.minOrderValue,
      minOrderValue: undefined,
    }))

    candidates.push(pruneEmptyFields({
      ...withEventId,
      discountType,
      validTo: withSpaceDateTime(normalized.validUntil),
      validUntil: undefined,
      validFrom: withSpaceDateTime(normalized.validFrom),
      minOrderAmount: normalized.minOrderValue,
      minOrderValue: undefined,
    }))

    // Compatibility for backends using maxUsage naming.
    candidates.push(pruneEmptyFields({
      ...withEventId,
      discountType,
      maxUsage: normalized.usageLimit,
      usageLimit: undefined,
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

  // Lấy danh sách voucher platform sale đang active (public)
  async getActivePlatformSaleVouchers() {
    const res = await api.get('/api/platform-sales/active-vouchers')
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

  // Create platform-wide voucher (admin only)
  async createPlatformVoucher(payload) {
    const candidates = buildCreatePayloadCandidates(payload)
    let lastError = null

    for (const candidate of candidates) {
      try {
        const res = await api.post('/api/admin/vouchers/platform', candidate)
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
  async validateVoucher(code, context = {}) {
    const payload = pruneEmptyFields({
      code,
      eventId: context?.eventId,
      orderAmount: toOptionalNumber(context?.orderAmount),
    })
    const res = await api.post('/api/vouchers/validate', payload)
    return res.data
  }
}

export default voucherService
