// src/stores/organizer.store.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { eventService } from '@/services/event.service'
import { normalizeEvent } from '@/stores/event.store'
import api from '@/services/api'

export const useOrganizerStore = defineStore('organizer', () => {
    const myEvents = ref([])
    const stats = ref(null)
    const loading = ref(false)
    const formLoading = ref(false)
    const error = ref(null)
    const formError = ref(null)
    const formWarning = ref(null)

    const pendingCount = computed(() =>
        myEvents.value.filter((e) => ['pending', 'draft'].includes(e.status)).length
    )
    const publishedCount = computed(() =>
        myEvents.value.filter((e) => ['published', 'approved'].includes(e.status)).length
    )

    // ── Actions ────────────────────────────────────────────────────────────────
    async function fetchMyEvents(organizerId) {
        loading.value = true
        error.value = null
        try {
            const data = await eventService.getByOrganizer(organizerId)
            const raw = Array.isArray(data) ? data : (data.content ?? data.data ?? [])
            myEvents.value = raw.map(normalizeEvent)
        } catch (e) {
            error.value = e.response?.data?.message || 'Failed to load your events'
            myEvents.value = []
        } finally {
            loading.value = false
        }
    }

    async function fetchStats(organizerId) {
        try {
            const res = await api.get(`/api/stats/organizer/${organizerId}`)
            stats.value = res.data
        } catch {
            stats.value = null
        }
    }

    /**
     * Multi-step event creation:
     * 1. POST /api/events  (event details)
     * 2. POST /api/events/{id}/seat-maps  (layout)
     * 3. POST /api/events/{id}/seat-maps/{seatMapId}/tiers  (ticket tiers, one per tier)
     */
    async function createFullEvent({ eventData, seatMapData, tiers = [] }) {
        formLoading.value = true
        formError.value = null
        formWarning.value = null
        try {
            // 1. Create event
            const event = await eventService.create(eventData)
            const eventId = event.id

            // 2. Create seat map (if provided)
            let seatMapId = null
            if (seatMapData) {
                try {
                    const seatMap = await eventService.createSeatMap(eventId, seatMapData)
                    seatMapId = seatMap?.id ?? seatMap?.seatMapId ?? null
                } catch (e) {
                    if (e?.response?.status === 403) {
                        formWarning.value = 'Event created, but your role is not allowed to create seat map/ticket tiers.'
                    } else {
                        throw e
                    }
                }
            }

            // 3. Create ticket tiers
            if (seatMapId && tiers.length) {
                try {
                    await Promise.all(tiers.map((tier) =>
                        eventService.createTier(eventId, seatMapId, tier)
                    ))
                } catch (e) {
                    if (e?.response?.status === 403) {
                        formWarning.value = 'Event created, but your role is not allowed to create ticket tiers.'
                    } else {
                        throw e
                    }
                }
            }

            const norm = normalizeEvent(event)
            myEvents.value.unshift(norm)
            return norm
        } catch (e) {
            formError.value = e.response?.data?.message || 'Failed to create event'
            return null
        } finally {
            formLoading.value = false
        }
    }

    async function createOrganizerRequest(eventData) {
        formLoading.value = true
        formError.value = null
        formWarning.value = null
        try {
            const event = await eventService.create(eventData)
            const norm = normalizeEvent(event)
            myEvents.value.unshift(norm)
            formWarning.value = 'Request submitted. Admin approval will promote your account to Organizer.'
            return norm
        } catch (e) {
            formError.value = e.response?.data?.message || 'Failed to submit organizer request'
            return null
        } finally {
            formLoading.value = false
        }
    }

    async function updateMyEvent(id, data) {
        formLoading.value = true
        formError.value = null
        try {
            const updated = await eventService.update(id, data)
            const norm = normalizeEvent(updated)
            const idx = myEvents.value.findIndex((e) => e.id === id)
            if (idx !== -1) myEvents.value[idx] = norm
            return norm
        } catch (e) {
            formError.value = e.response?.data?.message || 'Failed to update event'
            return null
        } finally {
            formLoading.value = false
        }
    }

    async function deleteMyEvent(id) {
        try {
            await eventService.deleteEvent(id)
            myEvents.value = myEvents.value.filter((e) => e.id !== id)
            return true
        } catch (e) {
            formError.value = e.response?.data?.message || 'Failed to delete event'
            return false
        }
    }

    function clear() {
        myEvents.value = []
        stats.value = null
        error.value = null
        formError.value = null
        formWarning.value = null
    }

    return {
        myEvents, stats, loading, formLoading, error, formError, formWarning,
        pendingCount, publishedCount,
        fetchMyEvents, fetchStats, createFullEvent, createOrganizerRequest, updateMyEvent, deleteMyEvent, clear,
    }
})
