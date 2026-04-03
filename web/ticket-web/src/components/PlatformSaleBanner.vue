<template>
  <div v-if="activeSales.length > 0" class="bg-gradient-to-r from-violet-700 via-fuchsia-700 to-violet-600 text-white mb-6 rounded-2xl overflow-hidden shadow-lg shadow-violet-950/40 border border-violet-500/30">
    <div class="p-6">
      <!-- Banner Content -->
      <div class="flex flex-col md:flex-row items-center justify-between">
        <div class="mb-4 md:mb-0">
          <p class="text-sm font-semibold opacity-90 mb-1">🎉 LIMITED TIME OFFER</p>
          <h2 class="text-3xl md:text-4xl font-bold">
            Save up to <span class="text-yellow-300">{{ bestDiscount?.discountPercentage }}%</span>
          </h2>
          <p v-if="bestDiscount?.description" class="text-lg opacity-90 mt-2">
            {{ bestDiscount.description }}
          </p>
        </div>
        
        <!-- Sales List (Mobile Carousel / Desktop Grid) -->
        <div class="w-full md:w-auto">
          <div v-if="activeSales.length > 1" class="flex gap-2 overflow-x-auto md:overflow-auto">
            <div
              v-for="sale in activeSales"
              :key="sale.id"
              class="bg-zinc-950/35 backdrop-blur px-4 py-2 rounded-lg border border-white/15 flex-shrink-0 whitespace-nowrap"
            >
              <p class="text-sm font-semibold">{{ sale.name }}</p>
              <p class="text-lg font-bold">{{ sale.discountPercentage }}% OFF</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Call-to-Action -->
      <div class="mt-4 flex flex-col sm:flex-row gap-3">
        <router-link
          to="/"
          class="inline-flex items-center justify-center px-6 py-2 rounded-lg font-semibold text-center flex-1 sm:flex-none bg-white text-violet-700 hover:bg-zinc-100 transition"
        >
          Discover Events
        </router-link>
        
        <!-- Countdown Timer (optional) -->
        <div v-if="daysRemaining !== null" class="bg-zinc-950/35 border border-white/15 px-4 py-2 rounded-lg text-center">
          <p class="text-xs opacity-75">Offer ends in</p>
          <p class="text-xl font-bold">{{ daysRemaining }} days</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { usePlatformSalesStore } from '@/stores/platformSales.store'

const salesStore = usePlatformSalesStore()

const activeSales = computed(() => salesStore.activeSales)
const bestDiscount = computed(() => salesStore.bestDiscount)

const daysRemaining = computed(() => {
  if (!bestDiscount.value) return null
  const endDate = new Date(bestDiscount.value.validUntil)
  const now = new Date()
  const msPerDay = 24 * 60 * 60 * 1000
  const days = Math.ceil((endDate - now) / msPerDay)
  return days > 0 ? days : null
})

onMounted(async () => {
  await salesStore.fetchActiveSales()
})
</script>
