<template>
  <RouterLink
    :to="`/event/${event.id}`"
    class="card card-hover group block cursor-pointer animate-fade-in"
  >
    <!-- Thumbnail -->
    <div class="relative overflow-hidden aspect-[16/9]">
      <img
        :src="event.image"
        :alt="event.title"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
        loading="lazy"
      />

      <!-- Gradient overlay -->
      <div class="absolute inset-0 bg-gradient-to-t from-zinc-950/80 via-transparent to-transparent" />

      <!-- Badges top-left -->
      <div class="absolute top-3 left-3 flex items-center gap-1.5">
        <span class="badge-violet text-xs">{{ event.category }}</span>
        <span v-if="event.featured" class="badge bg-amber-500/90 text-amber-900 border-0 text-xs font-bold">
          ★ Featured
        </span>
      </div>

      <!-- Status top-right -->
      <div class="absolute top-3 right-3">
        <span v-if="soldOutPercent >= 100" class="badge-red">Sold Out</span>
        <span v-else-if="soldOutPercent >= 80" class="badge-yellow">Almost Full</span>
        <span v-else class="badge-green">On Sale</span>
      </div>

      <!-- Date chip bottom-left -->
      <div class="absolute bottom-3 left-3">
        <div class="bg-zinc-950/80 backdrop-blur-sm border border-zinc-700/50 rounded-lg px-2.5 py-1.5 text-center min-w-[44px]">
          <p class="text-[10px] text-violet-400 font-semibold uppercase tracking-widest leading-none">
            {{ monthShort }}
          </p>
          <p class="text-xl font-black text-white leading-none">{{ day }}</p>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="p-4">
      <h3 class="font-semibold text-white leading-snug group-hover:text-violet-300 transition-colors line-clamp-2 mb-2">
        {{ event.title }}
      </h3>

      <div class="space-y-1.5 mb-3">
        <div class="flex items-center gap-1.5 text-xs text-zinc-400">
          <svg class="w-3.5 h-3.5 text-zinc-500 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M17.657 16.657 13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z"/>
            <path d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
          </svg>
          <span class="truncate">{{ event.venue }}, {{ event.city }}</span>
        </div>
        <div class="flex items-center gap-1.5 text-xs text-zinc-400">
          <svg class="w-3.5 h-3.5 text-zinc-500 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
          </svg>
          <span>{{ formattedDate }} · {{ event.time }}</span>
        </div>
      </div>

      <!-- Rating & sold -->
      <div class="flex items-center gap-3 mb-3">
        <div class="flex items-center gap-1">
          <svg class="w-3.5 h-3.5 text-amber-400" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
          <span class="text-xs font-semibold text-white">{{ event.rating }}</span>
          <span class="text-xs text-zinc-500">({{ event.reviewCount.toLocaleString() }})</span>
        </div>
        <div class="flex-1 text-right">
          <span class="text-xs text-zinc-500">{{ soldOutPercent }}% sold</span>
        </div>
      </div>

      <!-- Progress bar -->
      <div class="h-1 bg-zinc-800 rounded-full mb-3 overflow-hidden">
        <div
          class="h-full rounded-full transition-all duration-700"
          :class="soldOutPercent >= 80 ? 'bg-amber-500' : 'bg-violet-600'"
          :style="{ width: `${Math.min(soldOutPercent, 100)}%` }"
        />
      </div>

      <!-- Price row -->
      <div class="flex items-center justify-between">
        <div>
          <p class="text-xs text-zinc-500 mb-0.5">From</p>
          <div class="flex items-baseline gap-1.5">
            <span class="text-lg font-bold text-white">{{ formatPrice(event.price) }}</span>
            <span v-if="event.originalPrice" class="text-xs text-zinc-600 line-through">
              {{ formatPrice(event.originalPrice) }}
            </span>
          </div>
        </div>
        <div class="bg-violet-600/20 border border-violet-500/30 rounded-xl px-3 py-1.5 text-xs font-semibold text-violet-400 group-hover:bg-violet-600 group-hover:text-white group-hover:border-violet-600 transition-all duration-200">
          Get Tickets →
        </div>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  event: { type: Object, required: true },
})

const MONTHS = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
const dateObj = computed(() => new Date(props.event.date))
const monthShort = computed(() => MONTHS[dateObj.value.getUTCMonth()])
const day = computed(() => String(dateObj.value.getUTCDate()).padStart(2, '0'))
const formattedDate = computed(() =>
  dateObj.value.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC' })
)
const soldOutPercent = computed(() =>
  Math.round((props.event.sold / props.event.capacity) * 100)
)

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>
