// src/stores/booking.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { bookingService } from '@/services/booking.service'

// ── Order normaliser ──────────────────────────────────────────────────────────
export function normalizeOrder(o) {
    const ev = o.event || {}
    return {
        id: o.orderCode || o.orderNumber || String(o.id),
        eventId: ev.id || o.eventId,
        eventTitle: ev.title || o.eventTitle,
        eventDate: ev.date || ev.eventDate || o.eventDate,
        eventTime: ev.time || ev.startTime || o.eventTime,
        venue: ev.venue || ev.venueName || o.venue,
        image: ev.image || ev.imageUrl || o.image || '',
        tickets: (o.items || o.orderItems || []).map((i) => ({
            type: i.ticketTier?.name || i.tierName || i.type || 'Ticket',
            qty: i.quantity || i.qty,
            price: i.price || i.ticketTier?.price || 0,
        })),
        total: o.total || o.totalAmount || 0,
        status: (o.status || 'pending').toLowerCase(),
        paymentMethod: o.paymentMethod || o.payment?.method || '-',
        bookedAt: o.createdAt || o.bookedAt,
        qrCode: o.qrCode || o.ticketCode || o.id,
    }
}

export const useBookingStore = defineStore('booking', () => {
    const event = ref(null)   // normalized event object
    const selections = ref([])     // [{ ticketType: NormalizedTier, qty: number }]
    const quote = ref(null)   // API quote response
    const order = ref(null)   // created order
    const paymentIntent = ref(null)
    const myOrders = ref([])
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
    const grandTotal = computed(() =>
        quote.value?.total ?? (subtotal.value + serviceFee.value)
    )

    // ── Actions ────────────────────────────────────────────────────────────────
    function setBooking(eventObj, selected) {
        event.value = eventObj
        selections.value = selected.filter((s) => s.qty > 0)
        quote.value = null
    }

    async function fetchQuote() {
        if (!event.value || totalTickets.value === 0) return
        loading.value = true
        error.value = null
        try {
            const payload = {
                eventId: event.value.id,
                items: selections.value.map((s) => ({
                    ticketTierId: s.ticketType.id,
                    quantity: s.qty,
                })),
            }
            quote.value = await bookingService.quote(payload)
        } catch (e) {
            // Quote failure is non-blocking — fall back to local calculation
            error.value = e.response?.data?.message || null
        } finally {
            loading.value = false
        }
    }

    async function createOrder(contactInfo) {
        loading.value = true
        error.value = null
        try {
            const payload = {
                eventId: event.value.id,
                items: selections.value.map((s) => ({
                    ticketTierId: s.ticketType.id,
                    quantity: s.qty,
                })),
                contact: contactInfo,
            }
            order.value = await bookingService.createOrder(payload)
            return order.value
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to create order'
            return null
        } finally {
            loading.value = false
        }
    }

    async function createPaymentIntent(orderId, method) {
        loading.value = true
        error.value = null
        try {
            paymentIntent.value = await bookingService.createPaymentIntent(orderId, {
                method,
                returnUrl: `${window.location.origin}/payment-result`,
                cancelUrl: `${window.location.origin}/checkout`,
            })
            return paymentIntent.value
        } catch (e) {
            error.value = e.response?.data?.message || 'Payment failed. Please try again.'
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
            error.value = e.response?.data?.message || 'Webhook failed'
            return null
        } finally {
            loading.value = false
        }
    }

    async function fetchMyOrders() {
        loading.value = true
        try {
            const { useAuthStore } = await import('@/stores/auth.store')
            const auth = useAuthStore()
            const data = await bookingService.getMyOrders(auth.user?.id)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            myOrders.value = Array.isArray(raw) ? raw.map(normalizeOrder) : []
        } catch {
            myOrders.value = []
        } finally {
            loading.value = false
        }
    }

    async function fetchOrderById(id) {
        try {
            const data = await bookingService.getOrderById(id)
            return normalizeOrder(data)
        } catch {
            return null
        }
    }

    async function cancelOrder(id) {
        loading.value = true
        error.value = null
        try {
            await bookingService.cancelOrder(id)
            const idx = myOrders.value.findIndex((o) => o.id === id)
            if (idx !== -1) myOrders.value[idx] = { ...myOrders.value[idx], status: 'cancelled' }
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to cancel order'
            return false
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
    }

    return {
        event, selections, quote, order, paymentIntent, myOrders,
        loading, error,
        totalTickets, subtotal, serviceFee, grandTotal,
        setBooking, fetchQuote, createOrder, createPaymentIntent,
        fakeWebhook, fetchMyOrders, fetchOrderById, cancelOrder, clear,
    }
})
