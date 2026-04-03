<template>
  <div class="card divide-y divide-zinc-800">
    <!-- Header -->
    <div class="p-4">
      <h3 class="font-semibold text-white text-base mb-3">Order Summary</h3>

      <!-- Event mini-card -->
      <div class="flex gap-3">
        <img
          :src="cart.event?.image"
          :alt="cart.event?.title"
          class="w-16 h-16 rounded-lg object-cover shrink-0"
        />
        <div class="min-w-0">
          <p class="font-semibold text-white text-sm leading-snug line-clamp-2">
            {{ cart.event?.title }}
          </p>
          <p class="text-xs text-zinc-500 mt-1">
            {{ formattedDate }} · {{ cart.event?.time }}
          </p>
          <p class="text-xs text-zinc-500 truncate">{{ cart.event?.venue }}</p>
        </div>
      </div>
    </div>

    <!-- Ticket lines -->
    <div class="p-4 space-y-2">
      <div
        v-for="sel in cart.selections"
        :key="sel.ticketType.id"
        class="flex items-center justify-between text-sm"
      >
        <div class="flex items-center gap-2 min-w-0">
          <span class="w-5 h-5 rounded-full bg-violet-500/20 border border-violet-500/30 flex items-center justify-center text-[10px] font-bold text-violet-400 shrink-0">
            {{ sel.qty }}
          </span>
          <span class="text-zinc-300 truncate">{{ sel.ticketType.name }}</span>
        </div>
        <span class="text-white font-medium tabular-nums shrink-0 ml-2">
          {{ formatPrice(sel.ticketType.price * sel.qty) }}
        </span>
      </div>
    </div>

    <!-- Fees -->
    <div class="p-4 space-y-2 text-sm">
      <div class="flex items-center justify-between text-zinc-400">
        <span>Subtotal</span>
        <span class="tabular-nums">{{ formatPrice(cart.subtotal) }}</span>
      </div>
      <div class="flex items-center justify-between text-zinc-400">
        <div class="flex items-center gap-1">
          <span>Service Fee</span>
          <span class="text-[10px] bg-zinc-800 border border-zinc-700 rounded px-1.5 py-0.5 text-zinc-500">5%</span>
        </div>
        <span class="tabular-nums">{{ formatPrice(cart.serviceFee) }}</span>
      </div>
    </div>

    <!-- Grand total -->
    <div class="p-4">
      <div class="flex items-center justify-between">
        <span class="font-semibold text-white">Total</span>
        <span class="text-xl font-black text-white tabular-nums">{{ formatPrice(cart.grandTotal) }}</span>
      </div>
    </div>

    <!-- Promo code (visual only) -->
    <div class="p-4">
      <div class="flex gap-2">
        <input
          v-model="promoCode"
          type="text"
          placeholder="Promo code"
          class="input-field text-sm py-2.5"
          @keydown.enter="applyPromo"
          :disabled="cart.loading"
        />
        <button
          class="btn-secondary py-2.5 px-4 text-sm whitespace-nowrap"
          @click="applyPromo"
          :disabled="cart.loading"
        >
          {{ cart.loading ? 'Checking...' : 'Apply' }}
        </button>
      </div>
      <p v-if="promoMsg" class="text-xs mt-1.5" :class="promoSuccess ? 'text-emerald-400' : 'text-red-400'">
        {{ promoMsg }}
      </p>
    </div>

    <!-- Secure badge -->
    <div class="p-4">
      <div class="flex items-center gap-2 text-xs text-zinc-500">
        <svg class="w-4 h-4 text-emerald-500 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
        </svg>
        Secured by SSL · 256-bit encryption
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useBookingStore } from '@/stores/booking.store'

const cart = useBookingStore()

const promoCode   = ref('')
const promoMsg    = ref('')
const promoSuccess = ref(false)

async function applyPromo() {
  if (!promoCode.value.trim()) {
    promoMsg.value = ''
    promoSuccess.value = false
    return
  }

  const result = await cart.applyVoucher(promoCode.value.trim())
  
  if (result) {
    promoSuccess.value = true
    const discountText = result.discountType === 'PERCENTAGE' 
      ? `${result.discount}% off applied!` 
      : `Discount applied!`
    promoMsg.value = discountText
  } else {
    promoSuccess.value = false
    promoMsg.value = cart.error || 'Invalid promo code'
  }
}

const formattedDate = computed(() => {
  if (!cart.event?.date) return ''
  return new Date(cart.event.date).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC',
  })
})

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>
