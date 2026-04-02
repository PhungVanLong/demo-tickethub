<template>
  <div class="min-h-screen py-24 px-4">
    <div class="max-w-3xl mx-auto">

      <!-- Loading -->
      <div v-if="loading" class="flex items-center justify-center py-32">
        <div class="flex flex-col items-center gap-4">
          <div class="w-12 h-12 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
          <p class="text-zinc-500 text-sm">Loading order…</p>
        </div>
      </div>

      <!-- Not found -->
      <div v-else-if="!order" class="text-center py-32">
        <p class="text-zinc-400 text-lg mb-4">Order not found.</p>
        <RouterLink to="/profile" class="btn-primary">Back to My Tickets</RouterLink>
      </div>

      <!-- Order detail -->
      <div v-else class="animate-fade-in">

        <!-- Back -->
        <RouterLink to="/profile" class="inline-flex items-center gap-2 text-zinc-500 hover:text-white text-sm transition-colors mb-6">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
          Back to My Tickets
        </RouterLink>

        <!-- Header row -->
        <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-8">
          <div>
            <h1 class="text-2xl font-black text-white">Order Details</h1>
            <p class="text-zinc-500 text-sm mt-1 font-mono">{{ order.id }}</p>
          </div>
          <span
            class="self-start sm:self-auto text-sm font-semibold px-3 py-1.5 rounded-xl capitalize"
            :class="statusClass(order.status)"
          >{{ order.status }}</span>
        </div>

        <!-- Event card -->
        <div class="card p-5 flex gap-4 mb-6">
          <img
            v-if="order.image"
            :src="order.image"
            :alt="order.eventTitle"
            class="w-20 h-20 rounded-xl object-cover shrink-0"
          />
          <div class="min-w-0">
            <p class="text-xs text-zinc-500 uppercase tracking-wide font-semibold mb-1">Event</p>
            <h2 class="font-bold text-white text-lg leading-tight">{{ order.eventTitle }}</h2>
            <p class="text-sm text-zinc-400 mt-1">
              {{ formatDate(order.eventDate) }}
              <span v-if="order.eventTime"> · {{ order.eventTime }}</span>
              <span v-if="order.venue"> · {{ order.venue }}</span>
            </p>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6">
          <!-- Tickets list -->
          <div class="lg:col-span-3 space-y-4">
            <h3 class="font-bold text-white">Tickets</h3>

            <div
              v-for="(ticket, idx) in order.tickets"
              :key="idx"
              class="card p-5 flex items-start gap-4"
            >
              <!-- QR Code placeholder -->
              <div class="w-16 h-16 bg-white rounded-xl shrink-0 flex items-center justify-center">
                <div class="grid grid-cols-4 grid-rows-4 gap-0.5 w-12 h-12">
                  <div
                    v-for="i in 16"
                    :key="i"
                    class="rounded-sm"
                    :class="qrCell(idx, i) ? 'bg-zinc-900' : 'bg-white'"
                  />
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-semibold text-white">{{ ticket.type }}</p>
                <p class="text-sm text-zinc-500">Qty: {{ ticket.qty }} · {{ formatPrice(ticket.price) }} each</p>
                <p class="text-sm font-semibold text-violet-400 mt-1">{{ formatPrice(ticket.price * ticket.qty) }}</p>
              </div>
            </div>

            <div v-if="!order.tickets?.length" class="card p-6 text-center text-zinc-500 text-sm">
              No ticket details available
            </div>
          </div>

          <!-- Summary & actions -->
          <div class="lg:col-span-2 space-y-4">
            <!-- Price breakdown -->
            <div class="card p-5">
              <h3 class="font-bold text-white mb-4">Summary</h3>
              <div class="space-y-2">
                <div class="flex justify-between text-sm text-zinc-400">
                  <span>Subtotal</span>
                  <span>{{ formatPrice(subtotal) }}</span>
                </div>
                <div class="flex justify-between text-sm text-zinc-400">
                  <span>Service fee</span>
                  <span>{{ formatPrice(order.total - subtotal > 0 ? order.total - subtotal : 0) }}</span>
                </div>
                <div class="h-px bg-zinc-800 my-2" />
                <div class="flex justify-between font-bold text-white">
                  <span>Total</span>
                  <span>{{ formatPrice(order.total) }}</span>
                </div>
              </div>

              <div class="mt-4 pt-4 border-t border-zinc-800 space-y-2 text-xs text-zinc-500">
                <div class="flex justify-between">
                  <span>Ordered</span>
                  <span>{{ formatDate(order.bookedAt) }}</span>
                </div>
                <div class="flex justify-between">
                  <span>Payment</span>
                  <span>{{ order.paymentMethod || '—' }}</span>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="space-y-2">
              <RouterLink
                :to="`/event/${order.eventId}`"
                class="btn-secondary w-full justify-center text-sm"
              >
                View Event
              </RouterLink>
              <button
                v-if="order.status === 'pending' || order.status === 'confirmed'"
                class="w-full text-red-400 hover:text-red-300 border border-zinc-700 hover:border-red-500/40 bg-transparent hover:bg-red-500/10 font-medium px-4 py-3 rounded-xl text-sm transition-all"
                @click="cancelOrder"
              >
                Cancel Order
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute }        from 'vue-router'
import { useBookingStore } from '@/stores/booking.store'

const props        = defineProps({ orderId: { type: String, required: true } })
const route        = useRoute()
const bookingStore = useBookingStore()

const order   = ref(null)
const loading = ref(true)

onMounted(async () => {
  const id = props.orderId || route.params.orderId
  order.value = await bookingStore.fetchOrderById(id)
  loading.value = false
})

const subtotal = computed(() =>
  (order.value?.tickets ?? []).reduce((s, t) => s + t.price * t.qty, 0)
)

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val ?? 0)
}

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })
}

function statusClass(s) {
  return {
    confirmed: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30',
    pending:   'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30',
    cancelled: 'bg-red-500/20 text-red-400 border border-red-500/30',
    refunded:  'bg-blue-500/20 text-blue-400 border border-blue-500/30',
  }[s] ?? 'bg-zinc-800 text-zinc-400 border border-zinc-700'
}

function qrCell(ticketIdx, cellIdx) {
  // Deterministic pseudo-random based on ticket+cell index
  return ((ticketIdx * 17 + cellIdx * 7) % 3) !== 0
}

async function cancelOrder() {
  if (!confirm('Cancel this order?')) return
  await bookingStore.cancelOrder(order.value.id)
  order.value = { ...order.value, status: 'cancelled' }
}
</script>
