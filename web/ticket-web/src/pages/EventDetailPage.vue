<template>
  <div class="min-h-screen">
    <div v-if="eventStore.loading" class="min-h-screen py-24 flex items-center justify-center">
      <div class="flex flex-col items-center gap-4">
        <div class="w-12 h-12 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
        <p class="text-zinc-500 text-sm">Loading event...</p>
      </div>
    </div>

    <div v-else-if="event" class="animate-fade-in pb-20">
      <section class="relative h-72 sm:h-96 lg:h-[460px] overflow-hidden border-b border-zinc-800">
        <img
          :src="bannerUrl"
          :alt="event.title"
          class="w-full h-full object-cover"
          @error="handleBannerError"
        />
        <div class="absolute inset-0 bg-gradient-to-t from-zinc-950 via-zinc-950/60 to-zinc-950/20" />

        <div class="absolute top-20 left-4 sm:left-8 flex items-center gap-2">
          <button class="btn-secondary py-2 px-4 text-sm" @click="router.back()">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
            Back
          </button>
          <span class="badge-violet">{{ event.category || 'General' }}</span>
          <span v-if="soldPct >= 100" class="badge-red">Sold Out</span>
          <span v-else-if="soldPct >= 80" class="badge-yellow">Almost Full</span>
          <span v-else class="badge-green">On Sale</span>
        </div>

        <div class="absolute bottom-6 left-4 sm:left-8 right-4 sm:right-8">
          <h1 class="text-2xl sm:text-4xl font-black text-white max-w-3xl leading-tight">{{ event.title }}</h1>
          <p class="text-sm text-zinc-300 mt-2">{{ eventDateLabel }}{{ eventTimeLabel ? ` • ${eventTimeLabel}` : '' }}</p>
        </div>
      </section>

      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div class="lg:col-span-2 space-y-6">
          <div class="card p-6">
            <div class="grid sm:grid-cols-2 gap-4">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 rounded-xl bg-violet-500/20 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </div>
                <div>
                  <p class="text-xs text-zinc-500">Date</p>
                  <p class="text-sm font-semibold text-white">{{ eventDateLabel }}</p>
                  <p class="text-xs text-zinc-400">{{ eventTimeLabel || 'Time TBD' }}</p>
                </div>
              </div>

              <div class="flex items-start gap-3">
                <div class="w-10 h-10 rounded-xl bg-violet-500/20 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.657 16.657 13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z"/><path d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/></svg>
                </div>
                <div>
                  <p class="text-xs text-zinc-500">Venue</p>
                  <p class="text-sm font-semibold text-white">{{ event.venue || 'Venue TBD' }}</p>
                  <p class="text-xs text-zinc-400">{{ event.city || 'City TBD' }}, {{ event.country || 'Vietnam' }}</p>
                </div>
              </div>
            </div>

            <div v-if="event.tags?.length" class="flex flex-wrap gap-2 mt-5 pt-5 border-t border-zinc-800">
              <span v-for="tag in event.tags" :key="tag" class="badge-blue text-xs">{{ tag }}</span>
            </div>
          </div>

          <div class="card p-6">
            <h2 class="text-lg font-bold text-white mb-3">About This Event</h2>
            <p class="text-zinc-400 leading-relaxed whitespace-pre-line">{{ eventDescription }}</p>
          </div>

          <div v-if="seatMapImageUrl" class="card p-6">
            <h2 class="text-lg font-bold text-white mb-3">Seat Map Preview</h2>
            <p class="text-zinc-500 text-sm mb-3">Seat layout image submitted for this event.</p>
            <div class="rounded-xl overflow-hidden border border-zinc-700 bg-zinc-900">
              <img
                :src="seatMapImageUrl"
                alt="Seat map preview"
                class="w-full max-h-[460px] object-contain bg-zinc-950"
              />
            </div>
          </div>

          <div class="card p-6">
            <h2 class="text-lg font-bold text-white mb-4">Ticket Options</h2>
            <div v-if="displayTicketTypes.length" class="space-y-3">
              <div
                v-for="tier in displayTicketTypes"
                :key="tier.id"
                class="flex items-center justify-between p-3 rounded-xl bg-zinc-800/50 border border-zinc-700/50"
              >
                <div>
                  <p class="font-semibold text-white text-sm">{{ tier.name }}</p>
                  <p class="text-xs text-zinc-500 mt-0.5">
                    {{ tier.available ?? 0 }} available • max {{ tier.maxPerOrder ?? 4 }} per order
                  </p>
                </div>
                <p class="font-bold text-violet-400">{{ formatPrice(tier.price) }}</p>
              </div>
            </div>
            <p v-else class="text-zinc-500 text-sm">No ticket options published yet.</p>
          </div>
        </div>

        <div class="space-y-4 lg:sticky lg:top-24 self-start">
          <div class="card p-6">
            <div class="text-center mb-6">
              <p class="text-zinc-500 text-sm">Starting from</p>
              <p class="text-4xl font-black text-white mt-1">{{ formatPrice(event.price) }}</p>
              <div v-if="event.originalPrice" class="flex items-center justify-center gap-2 mt-1">
                <span class="text-zinc-600 line-through text-sm">{{ formatPrice(event.originalPrice) }}</span>
                <span class="badge-green text-xs">Save {{ savePct }}%</span>
              </div>
            </div>

            <div class="mb-6">
              <div class="flex justify-between text-xs text-zinc-500 mb-1.5">
                <span>Tickets Sold</span>
                <span>{{ soldPct }}%</span>
              </div>
              <div class="h-2 bg-zinc-800 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500"
                  :class="soldPct >= 80 ? 'bg-amber-500' : 'bg-violet-600'"
                  :style="{ width: `${soldPct}%` }"
                />
              </div>
              <p class="text-xs text-zinc-600 mt-1.5">{{ remainingTickets }} tickets remaining</p>
            </div>

            <RouterLink
              :to="`/booking/${event.id}`"
              class="btn-primary w-full justify-center text-base py-4"
            >
              Book Now
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
            </RouterLink>

            <div class="flex items-center justify-center gap-1.5 mt-3 text-xs text-zinc-500">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              Secure checkout • Instant confirmation
            </div>
          </div>

          <div class="card p-5">
            <p class="text-sm text-zinc-500">Organizer</p>
            <div class="flex items-center gap-3 mt-2">
              <div class="w-10 h-10 rounded-full bg-violet-600/30 text-violet-300 font-bold flex items-center justify-center">
                {{ organizerInitial }}
              </div>
              <div>
                <p class="text-white font-semibold text-sm">{{ organizerName }}</p>
                <p class="text-xs" :class="event.organizer?.verified ? 'text-emerald-400' : 'text-zinc-500'">
                  {{ event.organizer?.verified ? 'Verified organizer' : 'Organizer' }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div v-else class="min-h-screen py-24 flex items-center justify-center">
      <div class="text-center">
        <p class="text-zinc-300 text-xl font-semibold mb-2">Event not found</p>
        <p v-if="eventStore.error" class="text-red-400 text-sm mb-4">{{ eventStore.error }}</p>
        <RouterLink to="/" class="btn-primary">Back to Home</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useEventStore } from '@/stores/event.store'
import { extractSeatMapImageFromDescription, stripSeatMapImageTag } from '@/utils/seatMapImage'
import { resolveMediaUrl } from '@/utils/mediaUrl'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const eventStore = useEventStore()

onMounted(async () => {
  await eventStore.fetchById(props.id, { allowMockFallback: false })
  if (eventStore.currentEvent) {
    await eventStore.fetchTicketTiers(props.id)
    await eventStore.fetchSeatMap(props.id)
  }
})

const event = computed(() => eventStore.currentEvent)
const displayTicketTypes = computed(() => eventStore.ticketTiers.length ? eventStore.ticketTiers : (event.value?.ticketTypes ?? []))

const soldPct = computed(() => {
  if (!event.value?.capacity) return 0
  const value = Math.round(((event.value.sold ?? 0) / event.value.capacity) * 100)
  return Math.min(100, Math.max(0, value))
})

const remainingTickets = computed(() => {
  const cap = event.value?.capacity ?? 0
  const sold = event.value?.sold ?? 0
  return Math.max(0, cap - sold)
})

const savePct = computed(() => {
  const price = Number(event.value?.price ?? 0)
  const origin = Number(event.value?.originalPrice ?? 0)
  if (!origin || origin <= 0 || price <= 0 || price >= origin) return 0
  return Math.round((1 - price / origin) * 100)
})

const eventDateLabel = computed(() => {
  const date = event.value?.date
  if (!date) return 'Date TBD'
  const d = new Date(date)
  if (Number.isNaN(d.getTime())) return 'Date TBD'
  return d.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC',
  })
})

const eventTimeLabel = computed(() => event.value?.time || '')

const organizerName = computed(() => event.value?.organizer?.name || 'Organizer')
const organizerInitial = computed(() => organizerName.value?.charAt(0)?.toUpperCase() || 'O')

const bannerUrl = computed(() => {
  const source = event.value?.banner || event.value?.image
  return resolveMediaUrl(source) || 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=1600&q=80'
})
const seatMapImageUrl = computed(() => {
  const fromApi = eventStore.seatMap?.imageUrl || ''
  if (fromApi) return resolveMediaUrl(fromApi)
  return resolveMediaUrl(extractSeatMapImageFromDescription(event.value?.description || ''))
})
const eventDescription = computed(() => stripSeatMapImageTag(event.value?.description || '') || 'No description available.')

function handleBannerError(e) {
  e.target.onerror = null
  e.target.src = 'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=1600&q=80'
}

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(val ?? 0))
}
</script>
