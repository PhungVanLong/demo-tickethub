// src/stores/event.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { eventService } from '@/services/event.service'

// ── Field normalisers (adapter between backend and frontend field names) ───────
export function normalizeTicketTier(t) {
    return {
        id: t.ticketTierId ?? t.id,          // CheckoutTierResponse uses ticketTierId
        name: t.name || t.tierName,
        price: Number(t.price ?? 0),
        available: t.quantityAvailable ?? t.available ?? t.availableCount ?? t.remainingCount ?? 9999,
        maxPerOrder: t.maxPerOrder || 10,
        tierType: t.tierType || 'GENERAL',
        colorCode: t.colorCode || null,
    }
}

export function normalizeEvent(e) {
    return {
        id: e.id,
        title: e.title,
        slug: e.slug,
        category: e.category,
        date: e.date || e.eventDate,
        time: e.time || e.startTime,
        venue: e.venue || e.venueName || e.location,
        city: e.city,
        country: e.country || 'Vietnam',
        image: e.image || e.imageUrl || e.thumbnail || e.coverImage || '',
        banner: e.banner || e.bannerUrl || e.coverImage || e.image || e.imageUrl || '',
        price: e.price ?? e.basePrice ?? e.minPrice ?? 0,
        originalPrice: e.originalPrice ?? null,
        status: (e.status || 'published').toLowerCase(),
        featured: e.featured ?? false,
        tags: e.tags || [],
        rating: e.rating ?? 0,
        reviewCount: e.reviewCount ?? 0,
        sold: e.sold ?? e.soldCount ?? 0,
        capacity: e.capacity ?? e.totalCapacity ?? 0,
        description: e.description || '',
        organizer: e.organizer || { name: e.organizerName || 'Organizer', verified: false },
        ticketTypes: (e.ticketTiers || e.ticketTypes || []).map(normalizeTicketTier),
    }
}

export const useEventStore = defineStore('event', () => {
    const events = ref([])
    const currentEvent = ref(null)
    const ticketTiers = ref([])
    const seatMap = ref(null)
    const loading = ref(false)
    const tiersLoading = ref(false)
    const error = ref(null)
    const pagination = ref({ page: 0, size: 50, totalElements: 0, totalPages: 1 })

    // ── Actions ────────────────────────────────────────────────────────────────
    async function fetchPublished(params = {}) {
        loading.value = true
        error.value = null
        try {
            const data = await eventService.getPublished({ size: 50, ...params })
            // Support both Spring Page response and plain array
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            events.value = Array.isArray(raw) ? raw.map(normalizeEvent) : []
            if (!Array.isArray(data)) {
                pagination.value = {
                    page: data.number ?? 0,
                    size: data.size ?? 50,
                    totalElements: data.totalElements ?? events.value.length,
                    totalPages: data.totalPages ?? 1,
                }
            }
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to load events'
            events.value = []
        } finally {
            loading.value = false
        }
    }

    async function fetchById(id) {
        loading.value = true
        error.value = null
        currentEvent.value = null
        try {
            const data = await eventService.getById(id)
            currentEvent.value = normalizeEvent(data.data ?? data)
        } catch (e) {
            error.value = e.response?.data?.message || 'Event not found'
        } finally {
            loading.value = false
        }
    }

    async function fetchTicketTiers(eventId) {
        tiersLoading.value = true
        try {
            const data = await eventService.getTicketTiers(eventId)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            ticketTiers.value = Array.isArray(raw) ? raw.map(normalizeTicketTier) : []
        } catch {
            ticketTiers.value = []
        } finally {
            tiersLoading.value = false
        }
    }

    async function fetchSeatMap(eventId) {
        try {
            const data = await eventService.getSeatMap(eventId)
            seatMap.value = data.data ?? data
        } catch {
            seatMap.value = null
        }
    }

    function clearCurrent() {
        currentEvent.value = null
        ticketTiers.value = []
        seatMap.value = null
    }

    return {
        events, currentEvent, ticketTiers, seatMap,
        loading, tiersLoading, error, pagination,
        fetchPublished, fetchById, fetchTicketTiers, fetchSeatMap, clearCurrent,
    }
})
