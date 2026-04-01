<template>
  <div class="min-h-screen">

    <!-- ===== HERO ===== -->
    <section class="relative min-h-[92vh] flex items-center overflow-hidden">
      <!-- Background carousel -->
      <div class="absolute inset-0">
        <Transition name="bg-fade" mode="out-in">
          <img
            :key="featuredEvents[heroIndex]?.id"
            :src="featuredEvents[heroIndex]?.banner"
            :alt="featuredEvents[heroIndex]?.title"
            class="absolute inset-0 w-full h-full object-cover"
          />
        </Transition>
        <div class="absolute inset-0 bg-gradient-to-r from-zinc-950 via-zinc-950/80 to-transparent" />
        <div class="absolute inset-0 bg-gradient-to-t from-zinc-950 via-transparent to-zinc-950/40" />
      </div>

      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-32 w-full">
        <div class="max-w-2xl animate-slide-up">
          <!-- Badge -->
          <div class="inline-flex items-center gap-2 bg-violet-500/15 border border-violet-500/30 rounded-full px-4 py-1.5 mb-6">
            <span class="w-1.5 h-1.5 rounded-full bg-violet-400 animate-pulse"></span>
            <span class="text-xs font-semibold text-violet-300 tracking-wide uppercase">Featured Event</span>
          </div>

          <h1 class="text-4xl sm:text-5xl lg:text-6xl font-black text-white leading-tight mb-4">
            {{ featuredEvents[heroIndex]?.title }}
          </h1>

          <div class="flex flex-wrap items-center gap-4 mb-6">
            <div class="flex items-center gap-2 text-zinc-300">
              <svg class="w-4 h-4 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span class="text-sm">{{ formatDate(featuredEvents[heroIndex]?.date) }}</span>
            </div>
            <div class="flex items-center gap-2 text-zinc-300">
              <svg class="w-4 h-4 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M17.657 16.657 13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z"/><path d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
              </svg>
              <span class="text-sm">{{ featuredEvents[heroIndex]?.venue }}, {{ featuredEvents[heroIndex]?.city }}</span>
            </div>
          </div>

          <p class="text-zinc-400 text-base leading-relaxed mb-8 line-clamp-3">
            {{ featuredEvents[heroIndex]?.description }}
          </p>

          <div class="flex flex-wrap items-center gap-4">
            <RouterLink
              :to="`/event/${featuredEvents[heroIndex]?.id}`"
              class="btn-primary text-base px-8 py-3.5 glow-violet"
            >
              Get Tickets
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
            </RouterLink>
            <div class="flex items-baseline gap-1.5">
              <span class="text-zinc-500 text-sm">From</span>
              <span class="text-2xl font-black text-white">
                {{ formatPrice(featuredEvents[heroIndex]?.price) }}
              </span>
            </div>
          </div>

          <!-- Hero dots -->
          <div class="flex items-center gap-2 mt-10">
            <button
              v-for="(_, i) in featuredEvents"
              :key="i"
              class="h-1.5 rounded-full transition-all duration-300"
              :class="i === heroIndex ? 'w-8 bg-violet-500' : 'w-2 bg-zinc-600 hover:bg-zinc-500'"
              @click="heroIndex = i"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- ===== STATS STRIP ===== -->
    <section class="border-y border-zinc-800 bg-zinc-900/50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="grid grid-cols-2 sm:grid-cols-4 gap-6 divide-x divide-zinc-800">
          <div v-for="stat in stats" :key="stat.label" class="flex flex-col items-center text-center">
            <p class="text-2xl font-black text-white">{{ stat.value }}</p>
            <p class="text-xs text-zinc-500 mt-1">{{ stat.label }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ===== EVENT LISTING ===== -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">

      <!-- Header + filters -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <h2 class="text-3xl font-black text-white">Upcoming Events</h2>
          <p class="text-zinc-500 text-sm mt-1">{{ filteredEvents.length }} events found</p>
        </div>

        <!-- Search -->
        <div class="relative max-w-xs w-full">
          <svg class="w-4 h-4 text-zinc-500 absolute left-3 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            v-model="searchQ"
            type="text"
            placeholder="Search events…"
            class="input-field pl-10 text-sm py-2.5"
          />
        </div>
      </div>

      <!-- Category pills -->
      <div class="flex flex-wrap gap-2 mb-8">
        <button
          v-for="cat in categories"
          :key="cat.value"
          class="px-4 py-1.5 rounded-full text-sm font-medium border transition-all duration-200"
          :class="
            activeCategory === cat.value
              ? 'bg-violet-600 border-violet-600 text-white'
              : 'bg-zinc-900 border-zinc-700 text-zinc-400 hover:border-zinc-600 hover:text-white'
          "
          @click="activeCategory = cat.value"
        >
          {{ cat.label }}
        </button>
      </div>

      <!-- Sort -->
      <div class="flex items-center justify-between mb-6">
        <p class="text-sm text-zinc-500">
          <span class="text-white font-semibold">{{ filteredEvents.length }}</span> results
        </p>
        <select v-model="sortBy" class="input-field text-sm py-2 w-44">
          <option value="date">Sort: Date</option>
          <option value="price_asc">Price: Low to High</option>
          <option value="price_desc">Price: High to Low</option>
          <option value="rating">Top Rated</option>
        </select>
      </div>

      <!-- Grid -->
      <div
        v-if="filteredEvents.length"
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6"
      >
        <EventCard v-for="event in filteredEvents" :key="event.id" :event="event" />
      </div>

      <!-- Empty state -->
      <div v-else class="text-center py-24">
        <svg class="w-16 h-16 mx-auto text-zinc-700 mb-4" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
          <path d="M15.75 10.5V6a3.75 3.75 0 1 0-7.5 0v4.5m11.356-1.993 1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 0 1-1.12-1.243l1.264-12A1.125 1.125 0 0 1 5.513 7.5h12.974c.576 0 1.059.435 1.119 1.007z"/>
        </svg>
        <p class="text-zinc-400 text-lg font-semibold">No events found</p>
        <p class="text-zinc-600 text-sm mt-1">Try a different search or category</p>
        <button class="btn-secondary mt-4" @click="resetFilters">Clear Filters</button>
      </div>
    </section>

    <!-- ===== CTA BANNER ===== -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
      <div class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-violet-900 via-violet-800 to-purple-900 p-10 text-center">
        <div class="absolute inset-0 opacity-20 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-white to-transparent"></div>
        <div class="relative">
          <h2 class="text-3xl sm:text-4xl font-black text-white mb-4">
            Never Miss an Event Again
          </h2>
          <p class="text-violet-200 max-w-md mx-auto mb-8">
            Subscribe to get personalized event recommendations, exclusive early-bird deals, and instant alerts.
          </p>
          <div class="flex flex-col sm:flex-row gap-3 max-w-md mx-auto">
            <input
              v-model="emailSub"
              type="email"
              placeholder="Enter your email"
              class="flex-1 bg-white/10 border border-white/20 text-white placeholder-violet-300 rounded-xl px-4 py-3 focus:outline-none focus:border-white"
            />
            <button class="bg-white text-violet-900 font-bold px-6 py-3 rounded-xl hover:bg-violet-50 transition-colors shrink-0">
              Subscribe
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import EventCard from '@/components/EventCard.vue'
import { events, categories } from '@/data/events'

const heroIndex      = ref(0)
const searchQ        = ref('')
const activeCategory = ref('all')
const sortBy         = ref('date')
const emailSub       = ref('')

const featuredEvents = computed(() => events.filter((e) => e.featured))

// Auto-advance hero
let heroTimer = null
onMounted(() => {
  heroTimer = setInterval(() => {
    heroIndex.value = (heroIndex.value + 1) % featuredEvents.value.length
  }, 5000)
})
onUnmounted(() => clearInterval(heroTimer))

const filteredEvents = computed(() => {
  let list = [...events]

  if (activeCategory.value !== 'all') {
    list = list.filter((e) => e.category === activeCategory.value)
  }
  if (searchQ.value.trim()) {
    const q = searchQ.value.toLowerCase()
    list = list.filter(
      (e) =>
        e.title.toLowerCase().includes(q) ||
        e.venue.toLowerCase().includes(q) ||
        e.city.toLowerCase().includes(q)
    )
  }

  if (sortBy.value === 'price_asc')  list.sort((a, b) => a.price - b.price)
  if (sortBy.value === 'price_desc') list.sort((a, b) => b.price - a.price)
  if (sortBy.value === 'rating')     list.sort((a, b) => b.rating - a.rating)
  if (sortBy.value === 'date')       list.sort((a, b) => new Date(a.date) - new Date(b.date))

  return list
})

function resetFilters() {
  searchQ.value        = ''
  activeCategory.value = 'all'
  sortBy.value         = 'date'
}

const stats = [
  { value: '500+',  label: 'Events per Month' },
  { value: '1.2M',  label: 'Happy Customers'  },
  { value: '99.9%', label: 'Uptime'            },
  { value: '24/7',  label: 'Customer Support'  },
]

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC' })
}

function formatPrice(val) {
  if (!val) return ''
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>

<style scoped>
.bg-fade-enter-active,
.bg-fade-leave-active { transition: opacity 1s ease; }
.bg-fade-enter-from,
.bg-fade-leave-to    { opacity: 0; }
</style>
