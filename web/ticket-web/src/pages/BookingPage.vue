<template>
  <div v-if="event" class="min-h-screen py-24 animate-fade-in">
    <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">

      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-sm text-zinc-500 mb-8">
        <RouterLink to="/" class="hover:text-white transition-colors">Home</RouterLink>
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m9 18 6-6-6-6"/></svg>
        <RouterLink :to="`/event/${event.id}`" class="hover:text-white transition-colors truncate max-w-[200px]">{{ event.title }}</RouterLink>
        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m9 18 6-6-6-6"/></svg>
        <span class="text-white">Book Tickets</span>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-5 gap-8">

        <!-- LEFT: ticket selector -->
        <div class="lg:col-span-3 space-y-6">

          <!-- Event mini-header -->
          <div class="card p-5 flex gap-4">
            <img :src="event.image" :alt="event.title" class="w-20 h-20 rounded-xl object-cover shrink-0" />
            <div class="min-w-0">
              <span class="badge-violet text-xs mb-1">{{ event.category }}</span>
              <h1 class="font-bold text-white text-lg leading-tight line-clamp-2">{{ event.title }}</h1>
              <p class="text-xs text-zinc-500 mt-1">
                {{ formattedDate }} · {{ event.time }} · {{ event.venue }}, {{ event.city }}
              </p>
            </div>
          </div>

          <!-- Ticket selector -->
          <div class="card p-5">
            <TicketSelector
              :ticket-types="event.ticketTypes"
              v-model="selections"
            />
          </div>

          <!-- Important notes -->
          <div class="card p-5">
            <h3 class="font-semibold text-white text-sm mb-3 flex items-center gap-2">
              <svg class="w-4 h-4 text-amber-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
              Important Notes
            </h3>
            <ul class="space-y-2 text-xs text-zinc-400">
              <li class="flex items-start gap-2">
                <span class="text-violet-400 mt-0.5">•</span>
                Tickets are non-refundable and non-transferable.
              </li>
              <li class="flex items-start gap-2">
                <span class="text-violet-400 mt-0.5">•</span>
                Each order is subject to a 5% service fee.
              </li>
              <li class="flex items-start gap-2">
                <span class="text-violet-400 mt-0.5">•</span>
                Valid government-issued ID required for entry.
              </li>
              <li class="flex items-start gap-2">
                <span class="text-violet-400 mt-0.5">•</span>
                E-tickets will be sent to your registered email.
              </li>
            </ul>
          </div>
        </div>

        <!-- RIGHT: order summary + proceed -->
        <div class="lg:col-span-2 space-y-4 lg:sticky lg:top-24 self-start">
          <!-- Summary card -->
          <div class="card p-5">
            <h3 class="font-semibold text-white mb-4">Order Summary</h3>

            <div v-if="totalTickets > 0" class="space-y-2 mb-4">
              <div
                v-for="sel in activeSelections"
                :key="sel.ticketType.id"
                class="flex items-center justify-between text-sm"
              >
                <span class="text-zinc-400">{{ sel.qty }}x {{ sel.ticketType.name }}</span>
                <span class="text-white font-medium">{{ formatPrice(sel.ticketType.price * sel.qty) }}</span>
              </div>
            </div>

            <div v-else class="py-6 text-center">
              <svg class="w-8 h-8 mx-auto text-zinc-700 mb-2" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
                <path d="M20 12v10H4V12M22 7H2v5h20V7zM12 22V7M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7zM12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
              </svg>
              <p class="text-zinc-500 text-sm">No tickets selected yet</p>
            </div>

            <template v-if="totalTickets > 0">
              <div class="divider mb-3" />
              <div class="flex justify-between text-sm text-zinc-400 mb-1">
                <span>Subtotal</span>
                <span>{{ formatPrice(subtotal) }}</span>
              </div>
              <div class="flex justify-between text-sm text-zinc-400 mb-3">
                <span>Service fee (5%)</span>
                <span>{{ formatPrice(serviceFee) }}</span>
              </div>
              <div class="flex justify-between font-bold text-white text-base">
                <span>Total</span>
                <span>{{ formatPrice(grandTotal) }}</span>
              </div>
            </template>
          </div>

          <button
            class="btn-primary w-full justify-center text-base py-4"
            :disabled="totalTickets === 0"
            :class="totalTickets === 0 ? 'opacity-40 cursor-not-allowed' : 'glow-violet'"
            @click="proceedToCheckout"
          >
            Proceed to Checkout
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
          </button>

          <p v-if="totalTickets === 0" class="text-center text-xs text-zinc-600">
            Select at least one ticket to continue
          </p>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="min-h-screen flex items-center justify-center">
    <p class="text-zinc-400">Event not found. <RouterLink to="/" class="text-violet-400 hover:underline">Go back</RouterLink></p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter }    from 'vue-router'
import TicketSelector   from '@/components/TicketSelector.vue'
import { getEventById } from '@/data/events'
import { useCartStore } from '@/stores/cart'

const props  = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const cart   = useCartStore()

const event = computed(() => getEventById(props.id))

// Initialise selection array with qty = 0 for each ticket type
const selections = ref(
  (event.value?.ticketTypes ?? []).map((t) => ({ ticketType: t, qty: 0 }))
)

const activeSelections = computed(() => selections.value.filter((s) => s.qty > 0))
const totalTickets     = computed(() => selections.value.reduce((s, t) => s + t.qty, 0))
const subtotal         = computed(() => selections.value.reduce((s, t) => s + t.ticketType.price * t.qty, 0))
const serviceFee       = computed(() => Math.round(subtotal.value * 0.05))
const grandTotal       = computed(() => subtotal.value + serviceFee.value)

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

function proceedToCheckout() {
  if (totalTickets.value === 0) return
  cart.setBooking(event.value, activeSelections.value)
  router.push('/checkout')
}
</script>
