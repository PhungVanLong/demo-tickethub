<template>
  <AdminPage v-if="auth.isAdmin" />

  <div v-else class="min-h-screen py-24 px-4">
    <div class="max-w-6xl mx-auto space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <p class="text-xs font-semibold text-zinc-500 uppercase tracking-wide">Dashboard</p>
          <h1 class="text-3xl font-black text-white">Welcome, {{ displayName }}</h1>
          <p class="text-sm text-zinc-400 mt-1">Role: <span class="font-semibold text-zinc-200">{{ roleLabel }}</span></p>
        </div>
      </div>

      <div v-if="dashboard.loading" class="card p-10 flex justify-center">
        <div class="w-8 h-8 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
      </div>

      <div v-else-if="dashboard.error" class="card p-6 border-red-500/30 bg-red-500/10 text-red-300 text-sm">
        {{ dashboard.error }}
      </div>

      <div v-else class="space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <div v-for="item in statCards" :key="item.key" class="card p-5">
            <p class="text-xs text-zinc-500 uppercase tracking-wide mb-1">{{ item.label }}</p>
            <p class="text-2xl font-black text-white">{{ item.value }}</p>
          </div>
        </div>

        <div class="card p-5">
          <div class="flex items-center justify-between gap-3 mb-3">
            <p class="text-xs text-zinc-500 uppercase tracking-wide">My Event Request Sheets</p>
            <span class="badge-yellow">Pending {{ dashboard.pendingSheets.length }}</span>
          </div>

          <div v-if="dashboard.sheetsLoading" class="py-4 flex justify-center">
            <div class="w-6 h-6 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
          </div>

          <div v-else-if="dashboard.requestSheets.length" class="space-y-2">
            <RouterLink
              v-for="sheet in dashboard.requestSheets"
              :key="sheet.id"
              :to="`/event/${sheet.id}`"
              class="block rounded-xl border border-zinc-800 bg-zinc-900/50 hover:border-zinc-700 transition-colors p-3"
            >
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <p class="text-sm font-semibold text-white">{{ sheet.title }}</p>
                  <p class="text-xs text-zinc-500 mt-1">{{ sheet.venue || 'Venue TBD' }} · {{ sheet.city || 'City TBD' }}</p>
                  <p class="text-xs text-zinc-600 mt-1">Updated: {{ formatDateTime(sheet.updatedAt || sheet.createdAt) }}</p>
                </div>
                <span :class="requestStatusBadge(sheet.status)">{{ requestStatusLabel(sheet.status) }}</span>
              </div>
              <div class="mt-2 text-xs text-violet-400">View event detail</div>
            </RouterLink>
          </div>

          <p v-else class="text-sm text-zinc-500">No request sheets yet.</p>
        </div>

        <div v-if="dashboard.requestSheets.length" class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-2">Newest Request</p>
          <div class="rounded-xl border border-zinc-800 bg-zinc-900/50 p-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-base font-bold text-white">{{ dashboard.requestSheets[0].title }}</p>
                <p class="text-xs text-zinc-500 mt-1">{{ dashboard.requestSheets[0].venue || 'Venue TBD' }} · {{ dashboard.requestSheets[0].city || 'City TBD' }}</p>
                <p class="text-xs text-zinc-600 mt-1">{{ formatDateTime(dashboard.requestSheets[0].updatedAt || dashboard.requestSheets[0].createdAt) }}</p>
              </div>
              <span :class="requestStatusBadge(dashboard.requestSheets[0].status)">{{ requestStatusLabel(dashboard.requestSheets[0].status) }}</span>
            </div>
            <RouterLink :to="`/event/${dashboard.requestSheets[0].id}`" class="btn-secondary mt-3 py-2 px-3 text-xs">
              Open Detail
            </RouterLink>
          </div>
        </div>

        <div class="card p-5 space-y-4">
          <div class="flex items-center justify-between gap-3">
            <p class="text-xs text-zinc-500 uppercase tracking-wide">Orders Overview</p>
            <RouterLink to="/profile" class="text-xs text-violet-400 hover:text-violet-300">View all orders</RouterLink>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div class="rounded-xl border border-yellow-500/30 bg-yellow-500/10 p-4">
              <p class="text-xs uppercase tracking-wide text-yellow-300">Pending Orders</p>
              <p class="text-2xl font-black text-white mt-1">{{ dashboard.pendingOrders.length }}</p>
            </div>
            <div class="rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-4">
              <p class="text-xs uppercase tracking-wide text-emerald-300">Confirmed Orders</p>
              <p class="text-2xl font-black text-white mt-1">{{ dashboard.confirmedOrders.length }}</p>
            </div>
          </div>

          <div v-if="dashboard.ordersLoading" class="py-4 flex justify-center">
            <div class="w-6 h-6 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
          </div>

          <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-3">
            <div class="rounded-xl border border-zinc-800 bg-zinc-900/50 p-3">
              <p class="text-xs text-zinc-500 mb-2">Pending</p>
              <div v-if="dashboard.pendingOrders.length" class="space-y-2">
                <div v-for="order in dashboard.pendingOrders.slice(0, 3)" :key="`pending-${order.orderCode}`" class="text-sm">
                  <p class="text-zinc-200 font-medium">{{ order.eventTitle }}</p>
                  <p class="text-zinc-500 text-xs">#{{ order.orderCode }} · {{ formatDateTime(order.createdAt) }}</p>
                </div>
              </div>
              <p v-else class="text-sm text-zinc-500">No pending orders.</p>
            </div>

            <div class="rounded-xl border border-zinc-800 bg-zinc-900/50 p-3">
              <p class="text-xs text-zinc-500 mb-2">Confirmed</p>
              <div v-if="dashboard.confirmedOrders.length" class="space-y-2">
                <div v-for="order in dashboard.confirmedOrders.slice(0, 3)" :key="`confirmed-${order.orderCode}`" class="text-sm">
                  <p class="text-zinc-200 font-medium">{{ order.eventTitle }}</p>
                  <p class="text-zinc-500 text-xs">#{{ order.orderCode }} · {{ formatDateTime(order.createdAt) }}</p>
                </div>
              </div>
              <p v-else class="text-sm text-zinc-500">No confirmed orders.</p>
            </div>
          </div>
        </div>

        <div class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-2">Account</p>
          <p class="text-sm text-zinc-300">User ID: <span class="text-zinc-100 font-medium">{{ dashboard.profile?.userId ?? '—' }}</span></p>
          <p class="text-sm text-zinc-300">Email: <span class="text-zinc-100 font-medium">{{ dashboard.profile?.email ?? '—' }}</span></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useDashboardStore } from '@/stores/dashboard.store'
import AdminPage from '@/pages/AdminPage.vue'

const auth = useAuthStore()
const dashboard = useDashboardStore()

onMounted(async () => {
  if (!auth.isAdmin) {
    await dashboard.fetchDashboard()
  }
})

const displayName = computed(() => dashboard.profile?.fullName || auth.user?.name || 'Member')
const roleLabel = computed(() => dashboard.role || auth.user?.role || 'CUSTOMER')

const statCards = computed(() => {
  const entries = Object.entries(dashboard.stats || {})
    .filter(([, value]) => ['string', 'number'].includes(typeof value))
    .slice(0, 8)

  if (!entries.length) {
    return [{ key: 'empty', label: 'Stats', value: '—' }]
  }

  return entries.map(([key, value]) => ({
    key,
    label: key.replace(/([A-Z])/g, ' $1').replace(/^./, (c) => c.toUpperCase()),
    value,
  }))
})

function formatDateTime(value) {
  if (!value) return 'Unknown time'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'Unknown time'
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function requestStatusLabel(status) {
  const key = String(status || '').toLowerCase()
  return {
    pending: 'Pending',
    published: 'Approved',
    approved: 'Approved',
    rejected: 'Rejected',
    draft: 'Draft',
  }[key] || key.toUpperCase()
}

function requestStatusBadge(status) {
  const key = String(status || '').toLowerCase()
  if (key === 'pending') return 'badge-yellow'
  if (key === 'published' || key === 'approved') return 'badge-green'
  if (key === 'rejected') return 'badge-red'
  return 'badge-blue'
}
</script>
