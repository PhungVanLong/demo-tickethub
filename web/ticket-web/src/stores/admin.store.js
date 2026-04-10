// src/stores/admin.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { adminService } from '@/services/admin.service'
import { eventService } from '@/services/event.service'
import { normalizeEvent } from '@/stores/event.store'

const PLATFORM_FEE_RATE = 0.1

function toMoneyNumber(value) {
    const num = Number(value)
    return Number.isFinite(num) ? num : 0
}

function normalizePayment(payment) {
    const gross = toMoneyNumber(payment.amount ?? payment.totalAmount ?? payment.grossAmount)
    const gatewayFee = toMoneyNumber(payment.gatewayFee)

    const feeFromApi = toMoneyNumber(payment.platformFee)
    const platformFee = feeFromApi > 0 ? feeFromApi : gross * PLATFORM_FEE_RATE

    const netFromApi = toMoneyNumber(payment.organizerNet)
    const organizerNet = netFromApi > 0
        ? netFromApi
        : Math.max(gross - platformFee - gatewayFee, 0)

    return {
        ...payment,
        amount: gross,
        platformFee,
        gatewayFee,
        organizerNet,
    }
}

function normalizeRevenueSummary(summary) {
    const base = summary || {}
    const gross = toMoneyNumber(base.totalGrossRevenue ?? base.gmv)
    const gatewayFee = toMoneyNumber(base.totalGatewayFee ?? base.gatewayFeeTotal)

    const feeFromApi = toMoneyNumber(base.totalPlatformFee ?? base.platformFeeTotal)
    const platformFee = feeFromApi > 0 ? feeFromApi : gross * PLATFORM_FEE_RATE

    const netFromApi = toMoneyNumber(base.totalOrganizerNet ?? base.organizerNetTotal)
    const organizerNet = netFromApi > 0
        ? netFromApi
        : Math.max(gross - platformFee - gatewayFee, 0)

    return {
        ...base,
        totalGrossRevenue: gross,
        gmv: gross,
        totalPlatformFee: platformFee,
        platformFeeTotal: platformFee,
        totalGatewayFee: gatewayFee,
        gatewayFeeTotal: gatewayFee,
        totalOrganizerNet: organizerNet,
        organizerNetTotal: organizerNet,
    }
}

function normalizeUser(u) {
    return {
        id: u.id,
        name: u.fullName || u.name || u.username || u.email,
        email: u.email,
        role: (u.role || 'CUSTOMER').toUpperCase(),
        active: u.active ?? u.enabled ?? true,
        avatar: u.avatarUrl || u.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(u.email || u.id)}`,
        orders: u.ordersCount ?? 0,
        spent: u.totalSpent ?? 0,
        createdAt: u.createdAt,
    }
}

function toEventKey(eventItem) {
    const id = eventItem?.id
    if (id != null && String(id).trim()) return String(id)

    const slug = String(eventItem?.slug || '').trim()
    if (slug) return `slug:${slug}`

    const title = String(eventItem?.title || '').trim().toLowerCase()
    const date = String(eventItem?.date || '').trim()
    return `fallback:${title}:${date}`
}

function adminEventCompletenessScore(eventItem) {
    return [
        eventItem?.title,
        eventItem?.category,
        eventItem?.date,
        eventItem?.venue,
        eventItem?.city,
        eventItem?.image,
        Number(eventItem?.price || 0) > 0,
        Number(eventItem?.capacity || 0) > 0,
        Number(eventItem?.sold || 0) > 0,
    ].filter(Boolean).length
}

function normalizeAdminEvent(raw) {
    const normalized = normalizeEvent(raw)
    return {
        ...normalized,
        title: normalized.title || raw?.name || 'Untitled Event',
        category: normalized.category || raw?.type || 'General',
        price: toMoneyNumber(normalized.price),
        sold: toMoneyNumber(normalized.sold),
        capacity: toMoneyNumber(normalized.capacity),
        image: normalized.image || normalized.banner || '',
        status: String(normalized.status || raw?.status || 'pending').toLowerCase(),
    }
}

function dedupeEvents(list) {
    const byKey = new Map()

    list.forEach((item) => {
        const normalized = normalizeAdminEvent(item)
        const key = toEventKey(normalized)
        const existing = byKey.get(key)

        if (!existing) {
            byKey.set(key, normalized)
            return
        }

        const keepCurrent = adminEventCompletenessScore(normalized) >= adminEventCompletenessScore(existing)
        if (keepCurrent) {
            byKey.set(key, {
                ...existing,
                ...normalized,
                // Keep non-empty preferred fields when one source is partial.
                image: normalized.image || existing.image,
                banner: normalized.banner || existing.banner,
                title: normalized.title || existing.title,
                category: normalized.category || existing.category,
                venue: normalized.venue || existing.venue,
                city: normalized.city || existing.city,
            })
        }
    })

    return Array.from(byKey.values())
}

export const useAdminStore = defineStore('admin', () => {
    const pendingEvents = ref([])
    const allEvents = ref([])
    const users = ref([])
    const allOrders = ref([])
    const revenueSummary = ref(null)
    const payments = ref([])
    const payoutPreviews = ref({})
    const payoutResults = ref({})
    const platformStats = ref(null)
    const loading = ref(false)
    const usersLoading = ref(false)
    const ordersLoading = ref(false)
    const revenueLoading = ref(false)
    const payoutLoading = ref(false)
    const payoutError = ref(null)
    const error = ref(null)

    // ── KPIs: first from platformStats, fallback to revenueSummary ─────────────
    const kpiRevenue = computed(() =>
        platformStats.value?.monthlyGMV ?? revenueSummary.value?.gmv ?? null
    )
    const kpiOrders = computed(() =>
        platformStats.value?.confirmedOrdersToday ?? revenueSummary.value?.successfulPayments ?? null
    )
    const kpiUsers = computed(() =>
        platformStats.value?.totalUsers ?? null
    )
    const kpiTickets = computed(() =>
        platformStats.value?.publishedEvents ?? allEvents.value.length ?? null
    )

    // ── Actions ────────────────────────────────────────────────────────────────
    async function fetchPendingEvents() {
        loading.value = true
        error.value = null
        try {
            const data = await adminService.getPendingEvents()
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            pendingEvents.value = Array.isArray(raw) ? dedupeEvents(raw) : []
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to load pending events'
            pendingEvents.value = []
        } finally {
            loading.value = false
        }
    }

    async function fetchAllEvents() {
        loading.value = true
        try {
            const data = await adminService.getAllEvents({ size: 200 })
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            const adminEvents = Array.isArray(raw) ? raw : []
            allEvents.value = dedupeEvents([...adminEvents, ...pendingEvents.value])
        } catch {
            // Backward-compatible fallback in case admin endpoint is unavailable.
            try {
                const publishedData = await eventService.getPublished({ size: 200 })
                const publishedRaw = Array.isArray(publishedData)
                    ? publishedData
                    : (publishedData.content ?? publishedData.data ?? publishedData)
                const published = Array.isArray(publishedRaw) ? publishedRaw : []
                allEvents.value = dedupeEvents([...published, ...pendingEvents.value])
            } catch {
                allEvents.value = []
            }
        } finally {
            loading.value = false
        }
    }

    async function fetchPlatformStats() {
        try {
            const data = await adminService.getPlatformStats()
            platformStats.value = data.data ?? data
        } catch {
            platformStats.value = null
        }
    }

    async function fetchRevenueSummary(params = {}) {
        revenueLoading.value = true
        try {
            const data = await adminService.getRevenueSummary(params)
            revenueSummary.value = normalizeRevenueSummary(data.data ?? data)
        } catch {
            revenueSummary.value = null
        } finally {
            revenueLoading.value = false
        }
    }

    async function fetchPayments(params = {}) {
        revenueLoading.value = true
        try {
            const data = await adminService.getPayments(params)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? [])
            payments.value = Array.isArray(raw) ? raw.map(normalizePayment) : []
        } catch {
            payments.value = []
        } finally {
            revenueLoading.value = false
        }
    }

    async function previewOrganizerPayout(organizerId, params = {}) {
        payoutLoading.value = true
        payoutError.value = null
        try {
            const data = await adminService.getPayoutPreview(organizerId, params)
            payoutPreviews.value = {
                ...payoutPreviews.value,
                [organizerId]: data.data ?? data,
            }
            return payoutPreviews.value[organizerId]
        } catch (e) {
            payoutError.value = e.response?.data?.message || 'Failed to preview organizer payout'
            return null
        } finally {
            payoutLoading.value = false
        }
    }

    async function executeOrganizerPayout(organizerId, payload) {
        payoutLoading.value = true
        payoutError.value = null
        try {
            const data = await adminService.executePayout(organizerId, payload)
            payoutResults.value = {
                ...payoutResults.value,
                [organizerId]: data.data ?? data,
            }
            return payoutResults.value[organizerId]
        } catch (e) {
            payoutError.value = e.response?.data?.message || 'Failed to execute organizer payout'
            return null
        } finally {
            payoutLoading.value = false
        }
    }

    async function fetchUsers(search = '') {
        usersLoading.value = true
        error.value = null
        try {
            const data = search
                ? await adminService.searchUsers(search)
                : await adminService.getUsers()
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            users.value = Array.isArray(raw) ? raw.map(normalizeUser) : []
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to load users'
            users.value = []
        } finally {
            usersLoading.value = false
        }
    }

    async function activateUser(userId) {
        try {
            const updated = await adminService.activateUser(userId)
            const idx = users.value.findIndex((u) => u.id === userId)
            if (idx !== -1) users.value[idx] = normalizeUser(updated.data ?? updated)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to activate user'
            return false
        }
    }

    async function deactivateUser(userId) {
        try {
            const updated = await adminService.deactivateUser(userId)
            const idx = users.value.findIndex((u) => u.id === userId)
            if (idx !== -1) users.value[idx] = normalizeUser(updated.data ?? updated)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to deactivate user'
            return false
        }
    }

    async function promoteOrganizer(userId) {
        try {
            const updated = await adminService.promoteOrganizer(userId)
            const idx = users.value.findIndex((u) => u.id === userId)
            if (idx !== -1) users.value[idx] = normalizeUser(updated.data ?? updated)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to promote user'
            return false
        }
    }

    async function demoteCustomer(userId) {
        try {
            const updated = await adminService.demoteCustomer(userId)
            const idx = users.value.findIndex((u) => u.id === userId)
            if (idx !== -1) users.value[idx] = normalizeUser(updated.data ?? updated)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to demote user'
            return false
        }
    }

    async function deleteUser(userId) {
        try {
            await adminService.deleteUser(userId)
            users.value = users.value.filter((u) => u.id !== userId)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to delete user'
            return false
        }
    }

    async function fetchAllOrders(params = {}) {
        ordersLoading.value = true
        try {
            const data = await adminService.getAllOrders(params)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? [])
            allOrders.value = Array.isArray(raw) ? raw : []
        } catch {
            allOrders.value = []
        } finally {
            ordersLoading.value = false
        }
    }

    async function approveEvent(id, reason = '') {
        try {
            await adminService.approveEvent(id, reason)
            pendingEvents.value = pendingEvents.value.filter((e) => e.id !== id)
            const ev = allEvents.value.find((e) => e.id === id)
            if (ev) ev.status = 'published'
            await fetchUsers()
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to approve event'
            return false
        }
    }

    async function rejectEvent(id, reason = '') {
        try {
            await adminService.rejectEvent(id, reason)
            pendingEvents.value = pendingEvents.value.filter((e) => e.id !== id)
            const ev = allEvents.value.find((e) => e.id === id)
            if (ev) ev.status = 'rejected'
            await fetchUsers()
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to reject event'
            return false
        }
    }

    async function createEvent(data) {
        loading.value = true
        error.value = null
        try {
            const created = await eventService.create(data)
            const norm = normalizeAdminEvent(created.data ?? created)
            allEvents.value = dedupeEvents([norm, ...allEvents.value])
            return norm
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to create event'
            return null
        } finally {
            loading.value = false
        }
    }

    async function updateEvent(id, data) {
        loading.value = true
        error.value = null
        try {
            const updated = await eventService.update(id, data)
            const norm = normalizeAdminEvent(updated.data ?? updated)
            const idx = allEvents.value.findIndex((e) => e.id === id)
            if (idx !== -1) {
                allEvents.value[idx] = norm
                allEvents.value = dedupeEvents(allEvents.value)
            }
            return norm
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to update event'
            return null
        } finally {
            loading.value = false
        }
    }

    async function deleteEvent(id) {
        try {
            await eventService.deleteEvent(id)
            allEvents.value = allEvents.value.filter((e) => e.id !== id)
            return true
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to delete event'
            return false
        }
    }

    function deleteLocalEvent(id) {
        allEvents.value = allEvents.value.filter((e) => e.id !== id)
    }

    return {
        pendingEvents, allEvents, users, allOrders, revenueSummary, payments, payoutPreviews, payoutResults, platformStats,
        loading, usersLoading, ordersLoading, revenueLoading, payoutLoading, payoutError, error,
        kpiRevenue, kpiOrders, kpiUsers, kpiTickets,
        fetchPendingEvents, fetchAllEvents, fetchPlatformStats,
        fetchRevenueSummary, fetchPayments,
        previewOrganizerPayout, executeOrganizerPayout,
        fetchUsers, activateUser, deactivateUser, promoteOrganizer, demoteCustomer, deleteUser,
        fetchAllOrders,
        approveEvent, rejectEvent, createEvent, updateEvent, deleteEvent, deleteLocalEvent,
    }
})
