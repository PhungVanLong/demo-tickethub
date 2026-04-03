<template>
  <div class="min-h-screen py-24 animate-fade-in">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">

      <!-- Profile Header -->
      <div class="card p-6 sm:p-8 mb-8 bg-gradient-to-br from-zinc-900 to-zinc-900/50 relative overflow-hidden">
        <div class="absolute inset-0 bg-gradient-to-br from-violet-900/20 to-transparent pointer-events-none" />
        <div class="relative flex flex-col sm:flex-row items-start sm:items-center gap-6">
          <!-- Avatar -->
          <div class="relative">
            <img
              :src="auth.user?.avatar"
              :alt="auth.user?.name"
              class="w-20 h-20 rounded-2xl bg-zinc-800 ring-4 ring-violet-500/30"
            />
            <div class="absolute -bottom-1 -right-1 w-6 h-6 rounded-full bg-emerald-500 border-2 border-zinc-900 flex items-center justify-center">
              <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
            </div>
          </div>

          <!-- Info -->
          <div class="flex-1">
            <div class="flex items-center gap-3 flex-wrap">
              <h1 class="text-2xl font-black text-white">{{ auth.user?.name }}</h1>
              <span :class="auth.isAdmin ? 'badge-violet' : 'badge-blue'">
                {{ auth.isAdmin ? 'Admin' : 'Member' }}
              </span>
            </div>
            <p class="text-zinc-400 text-sm mt-1">{{ auth.user?.email }}</p>
            <p class="text-zinc-600 text-xs mt-1">Member since {{ formatJoinDate(auth.user?.joinedAt) }}</p>
          </div>

          <!-- Stats -->
          <div class="flex gap-6 sm:gap-8">
            <div class="text-center">
              <p class="text-2xl font-black text-white">{{ orders.length }}</p>
              <p class="text-xs text-zinc-500">Orders</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-black text-white">{{ totalSpent }}</p>
              <p class="text-xs text-zinc-500">Spent</p>
            </div>
            <div class="text-center">
              <p class="text-2xl font-black text-white">{{ upcomingCount }}</p>
              <p class="text-xs text-zinc-500">Upcoming</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex items-center gap-1 mb-8 bg-zinc-900 border border-zinc-800 p-1 rounded-2xl w-fit">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeTab === tab.id ? 'bg-violet-600 text-white shadow-lg' : 'text-zinc-400 hover:text-white'"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
          <span
            v-if="tab.count !== undefined"
            class="ml-1.5 text-xs px-1.5 py-0.5 rounded-full"
            :class="activeTab === tab.id ? 'bg-white/20' : 'bg-zinc-800 text-zinc-500'"
          >{{ tab.count }}</span>
        </button>
      </div>

      <!-- Tab: My Tickets -->
      <div v-if="activeTab === 'tickets'" class="space-y-4 animate-fade-in">
        <div v-if="orders.length === 0" class="text-center py-20">
          <svg class="w-16 h-16 mx-auto text-zinc-700 mb-4" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <path d="M20 12v10H4V12M22 7H2v5h20V7zM12 22V7M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7zM12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
          </svg>
          <p class="text-zinc-400 text-lg font-semibold">No tickets yet</p>
          <RouterLink to="/" class="btn-primary mt-4">Browse Events</RouterLink>
        </div>

        <div
          v-for="order in orders"
          :key="order.id"
          class="card p-5 sm:p-6 hover:border-zinc-700 transition-all duration-200"
        >
          <div class="flex flex-col sm:flex-row gap-5">
            <!-- Event image -->
            <img
              :src="order.image"
              :alt="order.eventTitle"
              class="w-full sm:w-36 h-32 sm:h-auto rounded-xl object-cover shrink-0"
            />

            <!-- Details -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-3 flex-wrap mb-2">
                <h3 class="font-bold text-white text-base leading-snug">{{ order.eventTitle }}</h3>
                <span :class="statusBadge(order.status)">{{ statusLabel(order.status) }}</span>
              </div>

              <div class="space-y-1.5 mb-3">
                <div class="flex items-center gap-2 text-sm text-zinc-400">
                  <svg class="w-3.5 h-3.5 text-zinc-600 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {{ formatDate(order.eventDate) }} · {{ order.eventTime }}
                </div>
                <div class="flex items-center gap-2 text-sm text-zinc-400">
                  <svg class="w-3.5 h-3.5 text-zinc-600 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.657 16.657 13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z"/><path d="M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/></svg>
                  {{ order.venue }}
                </div>
              </div>

              <!-- Ticket pills -->
              <div class="flex flex-wrap gap-1.5 mb-4">
                <span
                  v-for="t in order.tickets"
                  :key="t.type"
                  class="badge-blue text-xs"
                >
                  {{ t.qty }}× {{ t.type }}
                </span>
              </div>

              <div class="flex items-center justify-between flex-wrap gap-3">
                <div>
                  <p class="text-xs text-zinc-500">Total Paid</p>
                  <p class="text-base font-bold text-white">{{ formatPrice(order.total) }}</p>
                </div>
                <div class="flex gap-2">
                  <button
                    v-if="order.status === 'confirmed'"
                    class="btn-secondary py-2 px-4 text-sm"
                    @click="viewTicket(order)"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    View Tickets
                  </button>
                  <RouterLink :to="`/order/${order.orderId || order.id}`" class="btn-ghost py-2 px-3 text-sm">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
                    Details
                  </RouterLink>
                </div>
              </div>

              <p class="text-xs text-zinc-600 mt-2">
                Order #{{ order.orderCode || order.displayId || order.id }} · Paid via {{ order.paymentMethod }} · {{ formatDateTime(order.bookedAt) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab: Account Settings -->
      <div v-else-if="activeTab === 'settings'" class="animate-fade-in">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div class="card p-6">
            <h2 class="font-bold text-white text-lg mb-5">Personal Information</h2>
            <div class="space-y-4">
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Full Name</label>
                <input type="text" :value="auth.user?.name" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email</label>
                <input type="email" :value="auth.user?.email" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Phone</label>
                <input type="tel" placeholder="+84 912 345 678" class="input-field" />
              </div>
              <button class="btn-primary">Save Changes</button>
            </div>
          </div>

          <div class="card p-6">
            <h2 class="font-bold text-white text-lg mb-5">Change Password</h2>
            <form class="space-y-4" @submit.prevent>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Current Password</label>
                <input type="password" placeholder="••••••••" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">New Password</label>
                <input type="password" placeholder="••••••••" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Confirm Password</label>
                <input type="password" placeholder="••••••••" class="input-field" />
              </div>
              <button type="submit" class="btn-primary">Update Password</button>
            </form>
          </div>

          <div class="card p-6 lg:col-span-2">
            <h2 class="font-bold text-white text-lg mb-5">Notification Preferences</h2>
            <div class="space-y-4">
              <label v-for="pref in notifPrefs" :key="pref.id" class="flex items-center justify-between p-3 rounded-xl hover:bg-zinc-800/50 transition-colors cursor-pointer">
                <div>
                  <p class="text-sm font-medium text-white">{{ pref.label }}</p>
                  <p class="text-xs text-zinc-500">{{ pref.desc }}</p>
                </div>
                <button
                  class="relative w-11 h-6 rounded-full transition-colors duration-200 focus:outline-none"
                  :class="pref.enabled ? 'bg-violet-600' : 'bg-zinc-700'"
                  @click="pref.enabled = !pref.enabled"
                >
                  <span
                    class="absolute top-0.5 w-5 h-5 bg-white rounded-full shadow transition-transform duration-200"
                    :class="pref.enabled ? 'translate-x-5' : 'translate-x-0.5'"
                  />
                </button>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Ticket Modal -->
    <Transition name="fade">
      <div
        v-if="selectedOrder"
        class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
        @click.self="closeTicketModal"
      >
        <div class="bg-zinc-900 border border-zinc-700 rounded-3xl max-w-4xl w-full p-6 shadow-2xl animate-slide-up max-h-[90vh] overflow-y-auto">
          <div class="text-center mb-6">
            <h3 class="font-black text-white text-xl">Your E-Tickets</h3>
            <p class="text-zinc-500 text-sm mt-1">{{ selectedOrder.eventTitle }}</p>
            <p class="text-zinc-600 text-xs mt-1">Order #{{ selectedOrder.orderCode || selectedOrder.displayId || selectedOrder.id }}</p>
          </div>

          <div v-if="ticketModalLoading" class="py-16 flex flex-col items-center gap-4">
            <div class="w-10 h-10 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
            <p class="text-sm text-zinc-500">Loading tickets…</p>
          </div>

          <div v-else-if="ticketModalError" class="rounded-2xl border border-red-500/30 bg-red-500/10 p-4 text-sm text-red-300 mb-6">
            {{ ticketModalError }}
          </div>

          <div v-else-if="selectedTickets.length" class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
            <div v-for="ticket in selectedTickets" :key="ticket.id" class="rounded-2xl border border-zinc-800 bg-zinc-950/70 p-4 space-y-4">
              <TicketQrCode :data="ticket.qrCodeData" :size="220" />

              <div class="space-y-2 text-sm">
                <div class="flex justify-between gap-3">
                  <span class="text-zinc-500">Ticket</span>
                  <span class="font-mono text-white text-right">{{ ticket.ticketCode }}</span>
                </div>
                <div class="flex justify-between gap-3">
                  <span class="text-zinc-500">Type</span>
                  <span class="text-white text-right">{{ ticket.tierName }}</span>
                </div>
                <div class="flex justify-between gap-3">
                  <span class="text-zinc-500">Date</span>
                  <span class="text-white text-right">{{ formatDate(selectedOrder.eventDate) }}</span>
                </div>
                <div class="flex justify-between gap-3">
                  <span class="text-zinc-500">Venue</span>
                  <span class="text-white text-right max-w-[60%]">{{ selectedOrder.venue }}</span>
                </div>
                <div v-if="ticket.seatLabel" class="flex justify-between gap-3">
                  <span class="text-zinc-500">Seat</span>
                  <span class="text-white text-right">{{ ticket.seatLabel }}</span>
                </div>
                <div class="flex justify-between gap-3">
                  <span class="text-zinc-500">Status</span>
                  <span class="text-white capitalize">{{ ticket.status }}</span>
                </div>
              </div>

              <button class="btn-primary w-full justify-center" @click="downloadTicket(ticket)">
                Download QR
              </button>
            </div>
          </div>

          <div v-else class="rounded-2xl border border-zinc-800 bg-zinc-950/70 p-8 text-center text-sm text-zinc-500 mb-6">
            No issued tickets were returned by the backend for this order yet.
          </div>

          <button class="btn-primary w-full justify-center" @click="closeTicketModal">Close</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useAuthStore }    from '@/stores/auth.store'
import { useBookingStore } from '@/stores/booking.store'
import TicketQrCode from '@/components/TicketQrCode.vue'
import { downloadTicketQrPng } from '@/utils/ticketQr'

const auth    = useAuthStore()
const booking = useBookingStore()

// Fetch real orders on mount (non-blocking; template stays reactive)
onMounted(() => booking.fetchMyOrders())

// Alias so the template doesn't need changes
const orders = computed(() => booking.myOrders)

const activeTab     = ref('tickets')
const selectedOrder = ref(null)
const selectedTickets = ref([])
const ticketModalLoading = ref(false)
const ticketModalError = ref('')

const tabs = computed(() => [
  { id: 'tickets',  label: 'My Tickets', count: orders.value.length },
  { id: 'settings', label: 'Settings'                               },
])

const upcomingCount = computed(() =>
  orders.value.filter((o) => new Date(o.eventDate) >= new Date() && o.status === 'confirmed').length
)

const totalSpent = computed(() => {
  const sum = orders.value.reduce((s, o) => s + (o.status !== 'cancelled' ? (o.total ?? 0) : 0), 0)
  return new Intl.NumberFormat('vi-VN', { notation: 'compact', style: 'currency', currency: 'VND' }).format(sum)
})

const notifPrefs = reactive([
  { id: 'booking',   label: 'Booking Confirmations',   desc: 'Email me when a booking is confirmed', enabled: true  },
  { id: 'reminder',  label: 'Event Reminders',          desc: '24h before your events',              enabled: true  },
  { id: 'promo',     label: 'Promotional Offers',       desc: 'Exclusive deals and discounts',        enabled: false },
  { id: 'updates',   label: 'Event Updates',            desc: 'When event details change',            enabled: true  },
])

async function viewTicket(order) {
  selectedOrder.value = order
  selectedTickets.value = []
  ticketModalError.value = ''
  ticketModalLoading.value = true

  try {
    selectedTickets.value = await booking.fetchOrderTickets(order.orderId ?? order.id, true)
  } catch {
    ticketModalError.value = booking.error || 'Failed to load issued tickets'
  } finally {
    ticketModalLoading.value = false
  }
}

function closeTicketModal() {
  selectedOrder.value = null
  selectedTickets.value = []
  ticketModalError.value = ''
  ticketModalLoading.value = false
}

async function downloadTicket(ticket) {
  const latestTicket = await booking.downloadTicket(ticket.id)
  const ticketToDownload = latestTicket || ticket

  if (!ticketToDownload?.qrCodeData) {
    alert(booking.error || 'Ticket QR data is unavailable')
    return
  }

  try {
    await downloadTicketQrPng(ticketToDownload)
  } catch {
    alert('Failed to generate ticket QR image')
  }
}

function statusBadge(status) {
  if (status === 'confirmed') return 'badge-green'
  if (status === 'pending')   return 'badge-yellow'
  if (status === 'cancelled') return 'badge-red'
  if (status === 'refunded')  return 'badge-blue'
  return 'badge-blue'
}
function statusLabel(status) {
  return { confirmed: 'Confirmed', pending: 'Pending', cancelled: 'Cancelled', refunded: 'Refunded' }[status] ?? status
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric', timeZone: 'UTC' })
}
function formatDateTime(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
function formatJoinDate(d) {
  return d ? new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'long' }) : ''
}
function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val ?? 0)
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
