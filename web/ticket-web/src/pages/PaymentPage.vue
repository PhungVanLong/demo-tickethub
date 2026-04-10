<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-24">
    <!-- Processing / Simulation UI -->
    <Transition name="fade" mode="out-in">

      <!-- Result: Success -->
      <div v-if="result === 'success'" key="success" class="max-w-md w-full text-center animate-slide-up">
        <div class="w-24 h-24 rounded-full bg-emerald-500/20 border-2 border-emerald-500 flex items-center justify-center mx-auto mb-6">
          <svg class="w-12 h-12 text-emerald-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </div>
        <h1 class="text-3xl font-black text-white mb-2">Payment Successful!</h1>
        <p class="text-zinc-400 mb-1">Your order has been confirmed.</p>
        <p class="text-zinc-500 text-sm mb-8">Order ID: <span class="text-violet-400 font-mono">{{ orderId }}</span></p>
        <div class="flex gap-3 justify-center">
          <RouterLink to="/profile" class="btn-primary">View My Tickets</RouterLink>
          <RouterLink to="/" class="btn-secondary">Browse Events</RouterLink>
        </div>
      </div>

      <!-- Result: Failed -->
      <div v-else-if="result === 'failed'" key="failed" class="max-w-md w-full text-center animate-slide-up">
        <div class="w-24 h-24 rounded-full bg-red-500/20 border-2 border-red-500 flex items-center justify-center mx-auto mb-6">
          <svg class="w-12 h-12 text-red-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M18 6 6 18M6 6l12 12"/>
          </svg>
        </div>
        <h1 class="text-3xl font-black text-white mb-2">Payment Failed</h1>
        <p class="text-zinc-400 mb-6">{{ errorMsg || 'Your payment could not be processed.' }}</p>
        <div class="flex gap-3 justify-center">
          <RouterLink to="/checkout" class="btn-primary">Try Again</RouterLink>
          <RouterLink to="/" class="btn-secondary">Browse Events</RouterLink>
        </div>
      </div>

      <!-- Simulation UI -->
      <div v-else key="simulate" class="max-w-sm w-full">
        <div class="card p-8 text-center">
          <!-- Payment icon -->
          <div class="w-16 h-16 rounded-2xl bg-violet-600/20 border border-violet-500/30 flex items-center justify-center mx-auto mb-5">
            <svg class="w-8 h-8 text-violet-400" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <path d="M2 10h20"/>
            </svg>
          </div>

          <h1 class="text-2xl font-black text-white mb-1">Payment Gateway</h1>
          <p class="text-zinc-500 text-sm mb-6">Simulation Mode — Choose outcome below</p>

          <!-- Payment info -->
          <div class="bg-zinc-950 rounded-xl p-4 mb-6 text-left space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-zinc-500">Payment Code</span>
              <span class="text-zinc-300 font-mono text-xs truncate max-w-[160px]">{{ paymentCode }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-zinc-500">Order ID</span>
              <span class="text-zinc-300 font-mono text-xs truncate max-w-[160px]">{{ orderId }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span class="text-zinc-500">Status</span>
              <span class="badge-yellow">Awaiting Payment</span>
            </div>
          </div>

          <!-- Error -->
          <p v-if="errorMsg" class="text-red-400 text-sm mb-4 bg-red-500/10 border border-red-500/20 rounded-xl p-3">
            {{ errorMsg }}
          </p>

          <!-- Action buttons -->
          <div class="space-y-3">
            <button
              class="w-full bg-emerald-600 hover:bg-emerald-500 text-white font-semibold px-6 py-3.5 rounded-xl transition-all active:scale-95 inline-flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-wait"
              :disabled="processing"
              @click="simulate(true)"
            >
              <svg v-if="processing && simChoice === 'success'" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              Simulate Successful Payment
            </button>

            <button
              class="w-full bg-zinc-800 hover:bg-zinc-700 text-red-400 hover:text-red-300 font-semibold px-6 py-3.5 rounded-xl border border-zinc-700 transition-all active:scale-95 inline-flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-wait"
              :disabled="processing"
              @click="simulate(false)"
            >
              <svg v-if="processing && simChoice === 'failed'" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></svg>
              Simulate Failed Payment
            </button>
          </div>

          <p class="text-zinc-600 text-xs mt-4">This is a test environment. No real payment is processed.</p>
        </div>
      </div>

    </Transition>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useBookingStore } from '@/stores/booking.store'

const route        = useRoute()
const bookingStore = useBookingStore()

const paymentCode = computed(() => {
  const fromParams = route.params.paymentCode
  const fromQuery = route.query.paymentCode
  return String(fromParams || fromQuery || '').trim()
})
const orderId = computed(() => String(route.query.orderId ?? '').trim())
const callbackStatus = computed(() => String(route.query.status || route.query.result || '').toLowerCase())
const callbackMessage = computed(() => String(route.query.message || route.query.error || '').trim())

const result     = ref(null)   // null | 'success' | 'failed'
const processing = ref(false)
const simChoice  = ref(null)
const errorMsg   = ref('')

onMounted(async () => {
  const status = callbackStatus.value
  if (['failed', 'error', 'cancelled', 'canceled'].includes(status)) {
    result.value = 'failed'
    errorMsg.value = callbackMessage.value || 'Your payment could not be processed.'
    return
  }

  const polled = await pollOrderStatus(orderId.value)
  if (polled) {
    return
  }

  if (['success', 'paid', 'confirmed', 'ok'].includes(status)) {
    await bookingStore.fetchMyOrders()
    await warmUpIssuedTickets(orderId.value)
    result.value = 'success'
  }
})

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function warmUpIssuedTickets(orderId, attempts = 4, delayMs = 700) {
  if (!orderId) return

  for (let i = 0; i < attempts; i += 1) {
    const tickets = await bookingStore.fetchOrderTickets(orderId, true)
    if (Array.isArray(tickets) && tickets.length > 0) {
      return
    }

    if (i < attempts - 1) {
      await sleep(delayMs)
    }
  }
}

async function pollOrderStatus(orderIdentifier, attempts = 8, delayMs = 1200) {
  if (!orderIdentifier) return false

  for (let i = 0; i < attempts; i += 1) {
    const latestOrder = await bookingStore.fetchOrderById(orderIdentifier)
    const status = String(latestOrder?.status || latestOrder?.orderStatus || '').toLowerCase()

    if (status === 'confirmed') {
      await bookingStore.fetchMyOrders()
      await warmUpIssuedTickets(orderIdentifier)
      result.value = 'success'
      return true
    }

    if (status === 'cancelled' || status === 'refunded') {
      result.value = 'failed'
      errorMsg.value = 'Payment did not complete successfully.'
      return true
    }

    if (i < attempts - 1) {
      await sleep(delayMs)
    }
  }

  return false
}

async function simulate(succeed) {
  if (!paymentCode.value) {
    errorMsg.value = 'Payment code is missing. Please restart checkout.'
    result.value = 'failed'
    return
  }

  processing.value = true
  simChoice.value  = succeed ? 'success' : 'failed'
  errorMsg.value   = ''
  try {
    const res = await bookingStore.fakeWebhook(paymentCode.value, succeed)
    if (succeed && res) {
      await bookingStore.fetchMyOrders()
      await warmUpIssuedTickets(orderId.value)
      result.value = 'success'
    } else {
      result.value = 'failed'
      errorMsg.value = res?.message || bookingStore.error || 'Payment was declined.'
    }
  } catch (e) {
    errorMsg.value = e?.message || 'Something went wrong.'
  } finally {
    processing.value = false
    simChoice.value  = null
  }
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
