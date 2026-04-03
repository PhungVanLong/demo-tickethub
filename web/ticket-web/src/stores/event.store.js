// src/stores/event.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { eventService } from '@/services/event.service'
import { events as mockEvents } from '@/data/events'
import { logAction } from '@/services/actionLogger'

function debugLog(step, payload = {}) {
    if (!import.meta.env.DEV) return
    console.info(`[event.store] ${step}`, payload)
    logAction(`EVENT_${step}`, payload)
}

// ── Field normalisers (adapter between backend and frontend field names) ───────
export function normalizeTicketTier(t) {
    return {
        id: t.ticketTierId ?? t.id,          // CheckoutTierResponse uses ticketTierId
        name: t.name || t.tierName,
        price: Number(t.price ?? 0),
        available: t.quantityAvailable ?? t.available ?? t.availableCount ?? t.remainingCount ?? 9999,
        maxPerOrder: t.maxPerOrder || 4,
        tierType: t.tierType || 'GENERAL',
        colorCode: t.colorCode || null,
    }
}

function normalizeTags(tags) {
    if (Array.isArray(tags)) return tags
    if (typeof tags === 'string') {
        return tags
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean)
    }
    return []
}

function normalizeTime(value) {
    if (!value) return ''
    if (typeof value === 'string' && /^\d{2}:\d{2}/.test(value)) return value.slice(0, 5)
    const d = new Date(value)
    if (Number.isNaN(d.getTime())) return ''
    return d.toLocaleTimeString('en-GB', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: 'UTC',
    })
}

export function normalizeEvent(e) {
    const start = e.startTime || e.date || e.eventDate
    return {
        id: e.id,
        organizerId: e.organizerId || e.organizer?.id || null,
        title: e.title,
        slug: e.slug,
        category: e.category,
        date: e.date || e.eventDate || start,
        time: normalizeTime(e.time || start),
        venue: e.venue || e.venueName || e.location,
        city: e.city,
        country: e.country || 'Vietnam',
        image: e.image || e.imageUrl || e.thumbnail || e.coverImage || '',
        banner: e.banner || e.bannerUrl || e.coverImage || e.image || e.imageUrl || '',
        price: e.price ?? e.basePrice ?? e.minPrice ?? 0,
        originalPrice: e.originalPrice ?? null,
        status: (e.status || 'published').toLowerCase(),
        featured: e.featured ?? false,
        tags: normalizeTags(e.tags),
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
    const categories = ref([])
    const currentEvent = ref(null)
    const ticketTiers = ref([])
    const seatMap = ref(null)
    const loading = ref(false)
    const tiersLoading = ref(false)
    const error = ref(null)
    const pagination = ref({ page: 0, size: 50, totalElements: 0, totalPages: 1 })

    // ── Actions ────────────────────────────────────────────────────────────────
    async function fetchPublished(params = {}, options = {}) {
        const { allowMockFallback = true } = options
        loading.value = true
        error.value = null
        debugLog('FETCH_PUBLISHED_START', { params })
        try {
            const data = await eventService.getPublished({ size: 50, ...params })
            // Support both Spring Page response and plain array
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? data)
            events.value = Array.isArray(raw) ? raw.map(normalizeEvent) : []
            debugLog('FETCH_PUBLISHED_SUCCESS', {
                source: 'api',
                count: events.value.length,
                sampleId: events.value[0]?.id,
            })
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
            if (allowMockFallback) {
                // Fallback for local/demo when backend is unavailable or returns 5xx.
                events.value = mockEvents.map(normalizeEvent)
            } else {
                events.value = []
            }
            debugLog('FETCH_PUBLISHED_FALLBACK', {
                source: allowMockFallback ? 'mock' : 'none',
                reason: error.value,
                status: e?.response?.status,
                count: events.value.length,
            })
        } finally {
            loading.value = false
        }
    }

    async function fetchCategories() {
        try {
            const data = await eventService.getCategories()
            const raw = data?.categories ?? data?.data?.categories ?? []
            categories.value = Array.isArray(raw) ? raw : []
            debugLog('FETCH_CATEGORIES_SUCCESS', { count: categories.value.length })
        } catch {
            categories.value = []
            debugLog('FETCH_CATEGORIES_FAILED', {})
        }
    }

    async function fetchById(id, options = {}) {
        const { allowMockFallback = true } = options
        loading.value = true
        error.value = null
        currentEvent.value = null
        debugLog('FETCH_DETAIL_START', { id })
        try {
            const data = await eventService.getById(id)
            currentEvent.value = normalizeEvent(data.data ?? data)
            debugLog('FETCH_DETAIL_SUCCESS', {
                id,
                source: 'api',
                title: currentEvent.value?.title,
                banner: currentEvent.value?.banner,
            })
        } catch (e) {
            const status = e.response?.status

            // Some backends protect /api/events/{id}; fallback to published list for public detail pages.
            if (status === 401 || status === 403) {
                try {
                    const published = await eventService.getPublished({ size: 200 })
                    const raw = Array.isArray(published) ? published : (published.content ?? published.data ?? [])
                    const found = Array.isArray(raw)
                        ? raw.find((ev) => String(ev.id) === String(id))
                        : null
                    if (found) {
                        currentEvent.value = normalizeEvent(found)
                        debugLog('FETCH_DETAIL_FALLBACK', { id, source: 'published-list' })
                        return
                    }
                } catch {
                    // keep default error below
                }
            }

            if (allowMockFallback) {
                const fromMock = mockEvents.find((ev) => String(ev.id) === String(id))
                if (fromMock) {
                    currentEvent.value = normalizeEvent(fromMock)
                    debugLog('FETCH_DETAIL_FALLBACK', { id, source: 'mock' })
                    return
                }
            }

            error.value = e.response?.data?.message || 'Event not found'
            debugLog('FETCH_DETAIL_FAILED', { id, status, reason: error.value })
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
        events, categories, currentEvent, ticketTiers, seatMap,
        loading, tiersLoading, error, pagination,
        fetchPublished, fetchCategories, fetchById, fetchTicketTiers, fetchSeatMap, clearCurrent,
    }
})
