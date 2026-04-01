<template>
  <div v-if="event" class="min-h-screen animate-fade-in">

    <!-- ===== BANNER ===== -->
    <div class="relative h-72 sm:h-96 lg:h-[480px] overflow-hidden">
      <img :src="event.banner" :alt="event.title" class="w-full h-full object-cover" />
      <div class="absolute inset-0 bg-gradient-to-t from-zinc-950 via-zinc-950/60 to-transparent" />

      <!-- Back button -->
      <button
        class="absolute top-20 left-4 sm:left-8 btn-secondary py-2 px-4 text-sm"
        @click="router.back()"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
        Back
      </button>

      <!-- Category + status -->
      <div class="absolute bottom-6 left-4 sm:left-8 flex items-center gap-2">
        <span class="badge-violet">{{ event.category }}</span>
        <span v-if="soldPct >= 80" class="badge-yellow">{{ soldPct >= 100 ? 'Sold Out' : 'Almost Full' }}</span>
        <span v-else class="badge-green">On Sale</span>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-12 relative z-10 pb-20">
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">

        <!-- ===== LEFT / MAIN ===== -->
        <div class="lg:col-span-2 space-y-8">

          <!-- Title block -->
          <div class="card p-6">
            <h1 class="text-3xl sm:text-4xl font-black text-white leading-tight mb-4">
              {{ event.title }}
            </h1>

            <div class="grid sm:grid-cols-2 gap-4 mb-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-violet-500/20 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
                  </svg>
                </div>
                <div>
                  <p class="text-xs text-zinc-500">Date & Time</p>
                  <p class="text-sm font-semibold text-white">{{ formattedDate }}</p>
                  <p class="text-xs text-zinc-400">{{ event.time }} local time</p>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-violet-500/20 flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path d="M17.657 16.657 13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z"/><path d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                  </svg>
                </div>
                <div>
                  <p class="text-xs text-zinc-500">Venue</p>
                  <p class="text-sm font-semibold text-white">{{ event.venue }}</p>
                  <p class="text-xs text-zinc-400">{{ event.city }}, {{ event.country }}</p>
                </div>
              </div>
            </div>

            <!-- Tags -->
            <div class="flex flex-wrap gap-2 mb-4">
              <span v-for="tag in event.tags" :key="tag" class="badge-blue text-xs">{{ tag }}</span>
            </div>

            <!-- Rating row -->
            <div class="flex items-center gap-4 pt-4 border-t border-zinc-800">
              <div class="flex items-center gap-1.5">
                <div class="flex">
                  <svg v-for="i in 5" :key="i" class="w-4 h-4" :class="i <= Math.round(event.rating) ? 'text-amber-400' : 'text-zinc-700'" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                  </svg>
                </div>
                <span class="text-sm font-bold text-white">{{ event.rating }}</span>
                <span class="text-sm text-zinc-500">({{ event.reviewCount.toLocaleString() }} reviews)</span>
              </div>
              <div class="text-sm text-zinc-500">
                <span class="text-white font-semibold">{{ event.sold.toLocaleString() }}</span> / {{ event.capacity.toLocaleString() }} tickets sold
              </div>
            </div>
          </div>

          <!-- Description -->
          <div class="card p-6">
            <h2 class="text-lg font-bold text-white mb-4">About This Event</h2>
            <p class="text-zinc-400 leading-relaxed">{{ event.description }}</p>
          </div>

          <!-- Ticket types preview -->
          <div class="card p-6">
            <h2 class="text-lg font-bold text-white mb-4">Ticket Options</h2>
            <div class="space-y-3">
              <div
                v-for="t in event.ticketTypes"
                :key="t.id"
                class="flex items-center justify-between p-3 rounded-xl bg-zinc-800/50 border border-zinc-700/50"
              >
                <div>
                  <p class="font-semibold text-white text-sm">{{ t.name }}</p>
                  <p class="text-xs text-zinc-500 mt-0.5">
                    {{ t.available.toLocaleString() }} available · max {{ t.maxPerOrder }} per order
                  </p>
                </div>
                <div class="text-right">
                  <p class="font-bold text-violet-400">{{ formatPrice(t.price) }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Organizer -->
          <div class="card p-6">
            <h2 class="text-lg font-bold text-white mb-4">Organizer</h2>
            <div class="flex items-center gap-4">
              <div class="w-12 h-12 rounded-full bg-gradient-to-br from-violet-600 to-purple-700 flex items-center justify-center text-white font-black text-lg">
                {{ event.organizer.name[0] }}
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <p class="font-semibold text-white">{{ event.organizer.name }}</p>
                  <svg v-if="event.organizer.verified" class="w-4 h-4 text-violet-400" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 1 1-18 0 9 9 0 0 1 18 0z"/>
                  </svg>
                </div>
                <p v-if="event.organizer.verified" class="text-xs text-violet-400">✓ Verified Organizer</p>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== RIGHT SIDEBAR ===== -->
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

            <!-- Sold progress -->
            <div class="mb-6">
              <div class="flex justify-between text-xs text-zinc-500 mb-1.5">
                <span>Tickets Sold</span>
                <span>{{ soldPct }}%</span>
              </div>
              <div class="h-2 bg-zinc-800 rounded-full overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-1000"
                  :class="soldPct >= 80 ? 'bg-amber-500' : 'bg-violet-600'"
                  :style="{ width: `${soldPct}%` }"
                />
              </div>
              <p class="text-xs text-zinc-600 mt-1.5">
                {{ event.capacity - event.sold }} tickets remaining
              </p>
            </div>

            <RouterLink
              :to="`/booking/${event.id}`"
              class="btn-primary w-full justify-center text-base py-4 glow-violet"
            >
              Book Now
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
            </RouterLink>

            <div class="flex items-center justify-center gap-1.5 mt-3 text-xs text-zinc-500">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
              Secure checkout · Instant confirmation
            </div>
          </div>

          <!-- Share -->
          <div class="card p-4">
            <p class="text-sm font-semibold text-white mb-3">Share Event</p>
            <div class="flex gap-2">
              <button
                v-for="s in shares"
                :key="s.label"
                class="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg border border-zinc-700 hover:border-zinc-600 hover:bg-zinc-800 text-zinc-400 hover:text-white transition-all text-xs"
                :title="s.label"
              >
                <span v-html="s.icon" />
                <span>{{ s.label }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Not found -->
  <div v-else class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <p class="text-zinc-400 text-xl font-semibold">Event not found</p>
      <RouterLink to="/" class="btn-primary mt-4">Back to Home</RouterLink>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getEventById } from '@/data/events'

const props  = defineProps({ id: { type: String, required: true } })
const router = useRouter()

const event = computed(() => getEventById(props.id))

const soldPct = computed(() =>
  event.value ? Math.round((event.value.sold / event.value.capacity) * 100) : 0
)
const savePct = computed(() =>
  event.value?.originalPrice
    ? Math.round((1 - event.value.price / event.value.originalPrice) * 100)
    : 0
)
const formattedDate = computed(() =>
  event.value
    ? new Date(event.value.date).toLocaleDateString('en-US', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC',
      })
    : ''
)

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const shares = [
  { label: 'Copy', icon: '<svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>' },
  { label: 'Facebook', icon: '<svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>' },
  { label: 'Twitter', icon: '<svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 24 24"><path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"/></svg>' },
]
</script>
