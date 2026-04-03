// src/stores/booking.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { bookingService } from '@/services/booking.service'
import { ticketService } from '@/services/ticket.service'
import { voucherService } from '@/services/voucher.service'
import { logAction } from '@/services/actionLogger'
import { extractApiError } from '@/utils/apiError'

// ── Order normaliser ──────────────────────────────────────────────────────────
function unwrapListResponse(data) {
    const raw = Array.isArray(data) ? data : (data?.content ?? data?.data ?? [])
    return Array.isArray(raw) ? raw : []
}

function unwrapItemResponse(data) {
    return data?.data ?? data
}

function toKey(value) {
    if (value == null) return ''
    return String(value).trim()
}

export function normalizeTicket(ticket) {
    const tier = ticket?.ticketTier || ticket?.tier || {}
    const event = ticket?.event || {}
    const ticketId = ticket?.ticketId ?? ticket?.id
    const tierName = tier?.name || ticket?.ticketTierName || ticket?.tierName || ticket?.type || 'Ticket'

    return {
        id: ticketId,
        orderId: ticket?.orderId || ticket?.order?.id || ticket?.order?.orderId || null,
        eventId: event?.id || ticket?.eventId || null,
        eventTitle: event?.title || ticket?.eventTitle || '',
        eventDate: event?.date || event?.eventDate || event?.startTime || ticket?.eventDate || ticket?.startTime || null,
        eventTime: event?.time || event?.startTime || ticket?.eventTime || null,
        venue: event?.venue || event?.venueName || ticket?.venue || '',
        ticketCode: ticket?.ticketCode || (ticketId ? `TCK_${ticketId}` : ''),
        qrCodeData: ticket?.qrCodeData || ticket?.qrCode || ticket?.ticketCode || String(ticketId || ''),
        status: String(ticket?.status || ticket?.ticketStatus || 'active').toLowerCase(),
        usedAt: ticket?.usedAt || null,
        holderName: ticket?.holderName || ticket?.purchaserName || ticket?.ownerName || '',
        holderEmail: ticket?.holderEmail || ticket?.purchaserEmail || '',
        seatLabel: ticket?.seatLabel || ticket?.seatNumber || ticket?.seatCode || '',
        sectionName: ticket?.sectionName || tier?.sectionName || '',
        type: tierName,
        tierName,
        price: ticket?.price || ticket?.unitPrice || tier?.price || 0,
    }
}

export function normalizeOrder(o) {
    const ev = o.event || {}
    const primaryPayment = Array.isArray(o.payments) && o.payments.length ? o.payments[0] : null
    const apiOrderId = o.orderId ?? o.id ?? null
    const displayOrderCode = o.orderCode || o.orderNumber || (apiOrderId != null ? String(apiOrderId) : '')
    return {
        orderId: apiOrderId,
        orderCode: displayOrderCode,
        id: apiOrderId,
        displayId: displayOrderCode,
        eventId: ev.id || o.eventId,
        eventTitle: ev.title || o.eventTitle,
        eventDate: ev.date || ev.eventDate || ev.startTime || o.eventDate || o.startTime,
        eventTime: ev.time || ev.startTime || o.eventTime,
        venue: ev.venue || ev.venueName || o.venue,
        image: ev.image || ev.imageUrl || o.image || '',
        tickets: (o.items || o.orderItems || []).map((i) => ({
            type: i.ticketTier?.name || i.tierName || i.type || 'Ticket',
            qty: i.quantity || i.qty,
            price: i.price || i.unitPrice || i.ticketTier?.price || 0,
        })),
        total: o.total || o.finalAmount || o.totalAmount || 0,
        status: String(o.status || o.orderStatus || 'pending').toLowerCase(),
        paymentMethod: o.paymentMethod || o.payment?.method || primaryPayment?.paymentMethod || '-',
        bookedAt: o.createdAt || o.bookedAt,
        qrCodeData: o.qrCodeData || o.qrCode || o.ticketCode || String(o.id || ''),
    }
}

export const useBookingStore = defineStore('booking', () => {
    const event = ref(null)   // normalized event object
    const selections = ref([])     // [{ ticketType: NormalizedTier, qty: number }]
    const quote = ref(null)   // API quote response
    const order = ref(null)   // created order
    const paymentIntent = ref(null)
    const myOrders = ref([])
    const ticketsByOrder = ref({})
    const ticketsById = ref({})
    const purchaserInfo = ref(null)  // { firstName, lastName, email, phone }
    const appliedVoucher = ref(null)  // { code, discount, discountType }
    const loading = ref(false)
    const error = ref(null)

    // ── Computed totals (use API quote when available, fallback to local) ────────
    const totalTickets = computed(() =>
        selections.value.reduce((s, r) => s + r.qty, 0)
    )
    const subtotal = computed(() =>
        quote.value?.subtotal ?? selections.value.reduce((s, r) => s + r.ticketType.price * r.qty, 0)
    )
    const serviceFee = computed(() =>
        quote.value?.serviceFee ?? Math.round(subtotal.value * 0.05)
    )
    const discountAmount = computed(() => {
        const quoteDiscount = Number(
            quote.value?.discountAmount
            ?? quote.value?.discount
            ?? quote.value?.voucherDiscount
            ?? 0
        )
        if (quoteDiscount > 0) {
            return Math.max(0, Math.min(quoteDiscount, subtotal.value))
        }

        const applied = appliedVoucher.value
        if (!applied) return 0

        const type = String(applied.discountType || '').toUpperCase()
        const rawDiscount = Number(applied.discount || 0)
        if (!Number.isFinite(rawDiscount) || rawDiscount <= 0) return 0

        if (type === 'PERCENTAGE' || type === 'PERCENT') {
            const percentValue = Math.max(0, Math.min(rawDiscount, 100))
            return Math.round((subtotal.value * percentValue) / 100)
        }

        return Math.max(0, Math.min(rawDiscount, subtotal.value))
    })
    const subtotalAfterDiscount = computed(() =>
        Math.max(0, subtotal.value - discountAmount.value)
    )
    const grandTotal = computed(() =>
        (() => {
            const quoteTotal = Number(quote.value?.total)
            const quoteDiscount = Number(
                quote.value?.discountAmount
                ?? quote.value?.discount
                ?? quote.value?.voucherDiscount
                ?? 0
            )
            const hasAppliedVoucher = Boolean(appliedVoucher.value?.code)

            // Avoid showing stale total from a pre-voucher quote.
            if (Number.isFinite(quoteTotal) && (!hasAppliedVoucher || quoteDiscount > 0)) {
                return quoteTotal
            }

            return subtotalAfterDiscount.value + serviceFee.value
        })()
    )

    // ── Actions ────────────────────────────────────────────────────────────────
    function setBooking(eventObj, selected) {
        event.value = eventObj
        selections.value = selected.filter((s) => s.qty > 0)
        quote.value = null
        logAction('BOOKING_SET', {
            eventId: eventObj?.id,
            ticketCount: selections.value.reduce((sum, s) => sum + s.qty, 0),
        })
    }

    function setPurchaserInfo(info) {
        purchaserInfo.value = {
            firstName: String(info?.firstName ?? '').trim(),
            lastName: String(info?.lastName ?? '').trim(),
            email: String(info?.email ?? '').trim(),
            phone: String(info?.phone ?? '').trim(),
        }
    }

    function cacheTicket(ticket) {
        if (!ticket?.id) return

        ticketsById.value = {
            ...ticketsById.value,
            [ticket.id]: ticket,
        }

        if (ticket.orderId) {
            const currentOrderTickets = ticketsByOrder.value[ticket.orderId] || []
            const nextOrderTickets = currentOrderTickets.filter((item) => String(item.id) !== String(ticket.id))
            nextOrderTickets.push(ticket)
            ticketsByOrder.value = {
                ...ticketsByOrder.value,
                [ticket.orderId]: nextOrderTickets,
            }
        }
    }

    async function fetchQuote() {
        if (!event.value || totalTickets.value === 0) return
        loading.value = true
        error.value = null
        logAction('BOOKING_QUOTE_START', { eventId: event.value?.id, totalTickets: totalTickets.value })
        try {
            const { useAuthStore } = await import('@/stores/auth.store')
            const auth = useAuthStore()
            if (!auth.isLoggedIn) {
                error.value = 'Please login to continue checkout'
                logAction('BOOKING_QUOTE_FAILED', { eventId: event.value?.id, message: error.value })
                return
            }
            const payload = {
                userId: auth.user?.id,
                eventId: event.value.id,
                items: selections.value.map((s) => ({
                    ticketTierId: s.ticketType.id,
                    quantity: s.qty,
                })),
            }
            if (appliedVoucher.value?.code) {
                payload.voucherCode = appliedVoucher.value.code
                payload.code = appliedVoucher.value.code
            }
            quote.value = await bookingService.quote(payload)
            logAction('BOOKING_QUOTE_SUCCESS', { eventId: event.value?.id, total: quote.value?.total })
        } catch (e) {
            // Quote failure is non-blocking — fall back to local calculation
            error.value = extractApiError(e, 'Failed to get checkout quote').message
            logAction('BOOKING_QUOTE_FAILED', { eventId: event.value?.id, message: error.value })
        } finally {
            loading.value = false
        }
    }

    async function createOrder() {
        loading.value = true
        error.value = null
        logAction('BOOKING_ORDER_START', { eventId: event.value?.id, totalTickets: totalTickets.value })
        try {
            const { useAuthStore } = await import('@/stores/auth.store')
            const auth = useAuthStore()
            if (!auth.isLoggedIn) {
                error.value = 'Please login to continue checkout'
                logAction('BOOKING_ORDER_FAILED', { message: error.value })
                return null
            }
            const payload = {
                eventId: event.value.id,
                items: selections.value.map((s) => ({
                    ticketTierId: s.ticketType.id,
                    quantity: s.qty,
                })),
            }

            if (appliedVoucher.value?.code) {
                payload.voucherCode = appliedVoucher.value.code
                payload.code = appliedVoucher.value.code
            }
            if (appliedVoucher.value?.voucherId) {
                payload.voucherId = appliedVoucher.value.voucherId
            }

            // Include purchaser info if available (for email delivery, invoices, etc.)
            if (purchaserInfo.value) {
                payload.purchaserName = `${purchaserInfo.value.firstName} ${purchaserInfo.value.lastName}`.trim()
                payload.purchaserEmail = purchaserInfo.value.email
                payload.purchaserPhone = purchaserInfo.value.phone
            }

            order.value = await bookingService.createOrder(payload)
            logAction('BOOKING_ORDER_SUCCESS', { orderId: order.value?.orderId || order.value?.id })
            return order.value
        } catch (e) {
            error.value = extractApiError(e, 'Failed to create order').message
            logAction('BOOKING_ORDER_FAILED', { message: error.value })
            return null
        } finally {
            loading.value = false
        }
    }

    async function createPaymentIntent(orderId, method) {
        loading.value = true
        error.value = null
        logAction('BOOKING_PAYMENT_INTENT_START', { orderId, method: String(method || '').toUpperCase() })
        try {
            paymentIntent.value = await bookingService.createPaymentIntent(orderId, {
                method: String(method || '').toUpperCase(),
                returnUrl: `${window.location.origin}/payment-result`,
                cancelUrl: `${window.location.origin}/checkout`,
            })
            logAction('BOOKING_PAYMENT_INTENT_SUCCESS', {
                orderId,
                paymentCode: paymentIntent.value?.paymentCode || paymentIntent.value?.paymentId,
            })
            return paymentIntent.value
        } catch (e) {
            error.value = extractApiError(e, 'Payment failed. Please try again.').message
            logAction('BOOKING_PAYMENT_INTENT_FAILED', { orderId, message: error.value })
            return null
        } finally {
            loading.value = false
        }
    }

    async function fakeWebhook(paymentCode, succeed = true) {
        loading.value = true
        error.value = null
        try {
            const result = await bookingService.fakeWebhook({
                paymentCode,
                success: succeed,
                transactionId: `sim-${Date.now()}`,
                message: succeed ? 'Payment simulated successfully' : 'Payment declined by simulation',
            })
            return result
        } catch (e) {
            error.value = extractApiError(e, 'Webhook failed').message
            return null
        } finally {
            loading.value = false
        }
    }

    async function fetchMyOrders() {
        loading.value = true
        try {
            const data = await bookingService.getMyOrders()
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            myOrders.value = Array.isArray(raw) ? raw.map(normalizeOrder) : []
        } catch {
            myOrders.value = []
        } finally {
            loading.value = false
        }
    }

    async function ensureOrdersCacheLoaded() {
        if (myOrders.value.length) return
        try {
            const data = await bookingService.getMyOrders()
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            myOrders.value = Array.isArray(raw) ? raw.map(normalizeOrder) : []
        } catch {
            myOrders.value = []
        }
    }

    function findCachedOrder(orderIdentifier) {
        const key = toKey(orderIdentifier)
        if (!key) return null
        return myOrders.value.find((item) => {
            const candidates = [item.orderId, item.id, item.orderCode, item.displayId].map(toKey)
            return candidates.includes(key)
        }) || null
    }

    async function fetchOrderById(id) {
        const tried = new Set()

        const tryFetch = async (candidateId) => {
            const key = toKey(candidateId)
            if (!key || tried.has(key)) return null
            tried.add(key)

            const data = await bookingService.getOrderById(candidateId)
            return normalizeOrder(data)
        }

        try {
            const result = await tryFetch(id)
            error.value = null
            return result
        } catch (e) {
            await ensureOrdersCacheLoaded()
            const fallbackOrder = findCachedOrder(id)

            if (fallbackOrder) {
                try {
                    const result = await tryFetch(fallbackOrder.orderId)
                    error.value = null
                    if (result) return result
                } catch {
                    // Fall through and return cached order below.
                }

                error.value = null
                return fallbackOrder
            }

            error.value = extractApiError(e, 'Failed to load order detail').message
            return null
        }
    }

    async function fetchOrderTickets(orderId, force = false) {
        if (!orderId) return []
        if (!force && Array.isArray(ticketsByOrder.value[orderId])) {
            return ticketsByOrder.value[orderId]
        }

        await ensureOrdersCacheLoaded()
        const linkedOrder = findCachedOrder(orderId)

        const candidates = Array.from(new Set([
            toKey(orderId),
            toKey(linkedOrder?.orderId),
            toKey(linkedOrder?.id),
            toKey(linkedOrder?.orderCode),
            toKey(linkedOrder?.displayId),
        ].filter(Boolean)))

        loading.value = true
        error.value = null
        let lastError = null
        try {
            for (const candidate of candidates) {
                try {
                    const data = await ticketService.getTicketsByOrder(candidate)
                    const tickets = unwrapListResponse(data).map(normalizeTicket)

                    if (tickets.length) {
                        tickets.forEach(cacheTicket)
                        const nextMap = { ...ticketsByOrder.value }
                        candidates.forEach((alias) => {
                            nextMap[alias] = tickets
                        })
                        ticketsByOrder.value = nextMap
                        return tickets
                    }
                } catch (e) {
                    lastError = e
                }
            }

            if (lastError) {
                error.value = extractApiError(lastError, 'Failed to load tickets').message
            }

            return []
        } finally {
            loading.value = false
        }
    }

    async function fetchTicketById(ticketId, force = false) {
        if (!ticketId) return null
        if (!force && ticketsById.value[ticketId]) {
            return ticketsById.value[ticketId]
        }

        loading.value = true
        error.value = null
        try {
            const data = await ticketService.getTicketById(ticketId)
            const ticket = normalizeTicket(unwrapItemResponse(data))
            cacheTicket(ticket)
            return ticket
        } catch (e) {
            error.value = extractApiError(e, 'Failed to load ticket').message
            return null
        } finally {
            loading.value = false
        }
    }

    async function useTicket(ticketId) {
        loading.value = true
        error.value = null
        try {
            const data = await ticketService.useTicket(ticketId)
            const ticket = normalizeTicket(unwrapItemResponse(data))
            cacheTicket(ticket)
            return ticket
        } catch (e) {
            error.value = extractApiError(e, 'Failed to use ticket').message
            return null
        } finally {
            loading.value = false
        }
    }

    async function downloadTicket(ticketId) {
        loading.value = true
        error.value = null
        try {
            const data = await ticketService.downloadTicket(ticketId)
            const ticket = normalizeTicket(unwrapItemResponse(data))
            cacheTicket(ticket)
            return ticket
        } catch (e) {
            error.value = extractApiError(e, 'Failed to download ticket').message
            return null
        } finally {
            loading.value = false
        }
    }

    async function cancelOrder(id) {
        loading.value = true
        error.value = null
        logAction('BOOKING_CANCEL_START', { orderId: id })
        try {
            await bookingService.cancelOrder(id)
            const idx = myOrders.value.findIndex((o) => String(o.orderId ?? o.id) === String(id) || String(o.id) === String(id))
            if (idx !== -1) myOrders.value[idx] = { ...myOrders.value[idx], status: 'cancelled' }
            logAction('BOOKING_CANCEL_SUCCESS', { orderId: id })
            return true
        } catch (e) {
            error.value = extractApiError(e, 'Failed to cancel order').message
            logAction('BOOKING_CANCEL_FAILED', { orderId: id, message: error.value })
            return false
        } finally {
            loading.value = false
        }
    }

    async function applyVoucher(code) {
        loading.value = true
        error.value = null
        try {
            const data = await voucherService.validateVoucher(code)
            appliedVoucher.value = {
                code: code.trim(),
                discount: data?.discount || data?.discountValue || 0,
                discountType: data?.discountType || 'PERCENTAGE',
                voucherId: data?.id || data?.voucherId,
            }

            // Drop stale quote first so UI updates immediately when Apply is clicked.
            quote.value = null

            // Recalculate quote with voucher for authoritative totals when backend supports it.
            await fetchQuote()

            logAction('VOUCHER_APPLIED', { code, discount: appliedVoucher.value.discount })
            return appliedVoucher.value
        } catch (e) {
            appliedVoucher.value = null
            quote.value = null
            error.value = extractApiError(e, 'Invalid promo code').message
            logAction('VOUCHER_APPLY_FAILED', { code, message: error.value })
            return null
        } finally {
            loading.value = false
        }
    }

    function clear() {
        event.value = null
        selections.value = []
        quote.value = null
        order.value = null
        paymentIntent.value = null
        purchaserInfo.value = null
        appliedVoucher.value = null
    }

    return {
        event, selections, quote, order, paymentIntent, myOrders, ticketsByOrder, ticketsById, purchaserInfo,
        appliedVoucher,
        loading, error,
        totalTickets, subtotal, discountAmount, subtotalAfterDiscount, serviceFee, grandTotal,
        setBooking, setPurchaserInfo, fetchQuote, createOrder, createPaymentIntent,
        fakeWebhook, fetchMyOrders, fetchOrderById, fetchOrderTickets, fetchTicketById,
        useTicket, downloadTicket, cancelOrder, applyVoucher, clear,
    }
})
