import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { dashboardService } from '@/services/dashboard.service'
import { eventService } from '@/services/event.service'
import { normalizeEvent } from '@/stores/event.store'
import { bookingService } from '@/services/booking.service'

function normalizeOrderStatus(value) {
    return String(value || '').toLowerCase()
}

function normalizeDashboardOrder(order) {
    const event = order?.event || {}
    return {
        id: order?.id,
        orderCode: order?.orderCode || order?.orderNumber || String(order?.id || ''),
        status: normalizeOrderStatus(order?.status || order?.orderStatus),
        total: order?.total || order?.finalAmount || order?.totalAmount || 0,
        createdAt: order?.createdAt || order?.bookedAt || null,
        eventTitle: event?.title || order?.eventTitle || 'Event',
    }
}

export const useDashboardStore = defineStore('dashboard', () => {
    const profile = ref(null)
    const stats = ref({})
    const canCreateEvent = ref(true)
    const requestSheets = ref([])
    const sheetsLoading = ref(false)
    const myOrders = ref([])
    const ordersLoading = ref(false)
    const loading = ref(false)
    const error = ref(null)

    const role = computed(() => (profile.value?.role || '').toUpperCase())
    const pendingSheets = computed(() => requestSheets.value.filter((e) => e.status === 'pending'))
    const pendingOrders = computed(() => myOrders.value.filter((o) => o.status === 'pending'))
    const confirmedOrders = computed(() => myOrders.value.filter((o) => o.status === 'confirmed'))

    async function fetchMyOrders() {
        ordersLoading.value = true
        try {
            const data = await bookingService.getMyOrders()
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? [])
            myOrders.value = Array.isArray(raw) ? raw.map(normalizeDashboardOrder) : []
        } catch {
            myOrders.value = []
        } finally {
            ordersLoading.value = false
        }
    }

    async function fetchPendingSheets(userId) {
        if (!userId) {
            requestSheets.value = []
            return
        }

        sheetsLoading.value = true
        try {
            const data = await eventService.getByOrganizer(userId)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? [])
            requestSheets.value = Array.isArray(raw)
                ? raw
                    .map((event) => ({
                        ...normalizeEvent(event),
                        createdAt: event?.createdAt || null,
                        updatedAt: event?.updatedAt || null,
                    }))
                    .sort((a, b) => {
                        const timeA = new Date(a.updatedAt || a.createdAt || 0).getTime()
                        const timeB = new Date(b.updatedAt || b.createdAt || 0).getTime()
                        return timeB - timeA
                    })
                : []
        } catch {
            requestSheets.value = []
        } finally {
            sheetsLoading.value = false
        }
    }

    async function fetchDashboard() {
        loading.value = true
        error.value = null
        try {
            const data = await dashboardService.getDashboard()
            const raw = data?.data ?? data ?? {}
            profile.value = {
                role: raw.role,
                userId: raw.userId,
                email: raw.email,
                fullName: raw.fullName,
            }
            canCreateEvent.value = raw.canCreateEvent !== false
            stats.value = raw.stats && typeof raw.stats === 'object' ? raw.stats : {}
            await Promise.all([
                fetchPendingSheets(profile.value?.userId),
                fetchMyOrders(),
            ])
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to load dashboard'
            profile.value = null
            stats.value = {}
            canCreateEvent.value = true
            requestSheets.value = []
            myOrders.value = []
        } finally {
            loading.value = false
        }
    }

    return {
        profile,
        stats,
        canCreateEvent,
        requestSheets,
        pendingSheets,
        sheetsLoading,
        myOrders,
        ordersLoading,
        pendingOrders,
        confirmedOrders,
        loading,
        error,
        role,
        fetchPendingSheets,
        fetchMyOrders,
        fetchDashboard,
    }
})
