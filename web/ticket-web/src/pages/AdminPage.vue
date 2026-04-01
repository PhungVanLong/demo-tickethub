<template>
  <div class="min-h-screen py-24 animate-fade-in">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <p class="text-xs font-semibold text-violet-400 uppercase tracking-widest mb-1">Admin Panel</p>
          <h1 class="text-3xl font-black text-white">Dashboard</h1>
        </div>
        <button class="btn-primary" @click="showCreateModal = true">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Create Event
        </button>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div
          v-for="kpi in kpis"
          :key="kpi.label"
          class="card p-5 hover:border-zinc-700 transition-colors"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center" :class="kpi.iconBg">
              <span v-html="kpi.icon" class="w-5 h-5" :class="kpi.iconColor" />
            </div>
            <span class="text-xs font-semibold" :class="kpi.trendUp ? 'text-emerald-400' : 'text-red-400'">
              {{ kpi.trend }}
            </span>
          </div>
          <p class="text-2xl font-black text-white">{{ kpi.value }}</p>
          <p class="text-xs text-zinc-500 mt-1">{{ kpi.label }}</p>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex items-center gap-1 mb-6 bg-zinc-900 border border-zinc-800 p-1 rounded-2xl w-fit">
        <button
          v-for="tab in adminTabs"
          :key="tab.id"
          class="px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeTab === tab.id ? 'bg-violet-600 text-white' : 'text-zinc-400 hover:text-white'"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- TAB: Events -->
      <div v-if="activeTab === 'events'" class="space-y-4 animate-fade-in">
        <!-- Filter row -->
        <div class="flex flex-col sm:flex-row gap-3">
          <input v-model="eventSearch" type="text" placeholder="Search events…" class="input-field text-sm py-2.5 max-w-xs" />
          <select v-model="eventStatusFilter" class="input-field text-sm py-2.5 w-44">
            <option value="all">All Status</option>
            <option value="on_sale">On Sale</option>
            <option value="pending">Pending Approval</option>
            <option value="rejected">Rejected</option>
          </select>
        </div>

        <AdminTable
          :columns="eventColumns"
          :rows="filteredAdminEvents"
          :total-rows="filteredAdminEvents.length"
          :current-page="1"
          :page-size="10"
          empty-text="No events found"
        >
          <template #cell-title="{ row }">
            <div class="flex items-center gap-3">
              <img :src="row.image" :alt="row.title" class="w-10 h-10 rounded-lg object-cover shrink-0" />
              <div class="min-w-0">
                <p class="font-semibold text-white text-sm truncate max-w-[200px]">{{ row.title }}</p>
                <p class="text-xs text-zinc-500">{{ row.category }}</p>
              </div>
            </div>
          </template>
          <template #cell-date="{ value }">
            <span class="text-zinc-300 text-sm">{{ formatDate(value) }}</span>
          </template>
          <template #cell-price="{ value }">
            <span class="text-violet-400 font-semibold text-sm">{{ formatPrice(value) }}</span>
          </template>
          <template #cell-sold="{ row }">
            <div class="flex items-center gap-2">
              <div class="flex-1 h-1.5 bg-zinc-800 rounded-full w-16 overflow-hidden">
                <div
                  class="h-full rounded-full bg-violet-600"
                  :style="{ width: `${Math.round((row.sold / row.capacity) * 100)}%` }"
                />
              </div>
              <span class="text-xs text-zinc-400 tabular-nums">{{ Math.round((row.sold / row.capacity) * 100) }}%</span>
            </div>
          </template>
          <template #cell-status="{ row }">
            <select
              v-model="row.status"
              class="bg-zinc-800 border border-zinc-700 text-xs text-zinc-300 rounded-lg px-2 py-1 focus:outline-none focus:border-violet-500"
              @change="onEventStatusChange(row)"
            >
              <option value="on_sale">On Sale</option>
              <option value="pending">Pending</option>
              <option value="rejected">Rejected</option>
            </select>
          </template>
          <template #cell-actions="{ row }">
            <div class="flex items-center gap-1">
              <button class="btn-ghost py-1 px-2 text-xs" @click="editEvent(row)">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Edit
              </button>
              <button class="py-1 px-2 rounded-lg text-xs text-red-400 hover:text-red-300 hover:bg-zinc-800 transition-colors" @click="confirmDelete(row)">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
              </button>
            </div>
          </template>
        </AdminTable>
      </div>

      <!-- TAB: Orders -->
      <div v-else-if="activeTab === 'orders'" class="space-y-4 animate-fade-in">
        <div class="flex flex-col sm:flex-row gap-3">
          <input v-model="orderSearch" type="text" placeholder="Search by order ID or event…" class="input-field text-sm py-2.5 max-w-xs" />
          <select v-model="orderStatusFilter" class="input-field text-sm py-2.5 w-44">
            <option value="all">All Status</option>
            <option value="confirmed">Confirmed</option>
            <option value="pending">Pending</option>
            <option value="cancelled">Cancelled</option>
          </select>
        </div>
        <AdminTable
          :columns="orderColumns"
          :rows="filteredAdminOrders"
          :total-rows="filteredAdminOrders.length"
          :current-page="1"
          :page-size="10"
          empty-text="No orders found"
        >
          <template #cell-id="{ value }">
            <span class="font-mono text-xs text-violet-400">{{ value }}</span>
          </template>
          <template #cell-eventTitle="{ value }">
            <span class="text-white text-sm font-medium line-clamp-1 max-w-[200px]">{{ value }}</span>
          </template>
          <template #cell-total="{ value }">
            <span class="text-white font-semibold">{{ formatPrice(value) }}</span>
          </template>
          <template #cell-status="{ row }">
            <span :class="orderBadge(row.status)" class="capitalize">{{ row.status }}</span>
          </template>
          <template #cell-bookedAt="{ value }">
            <span class="text-zinc-400 text-xs">{{ formatDateTime(value) }}</span>
          </template>
        </AdminTable>
      </div>

      <!-- TAB: Users -->
      <div v-else-if="activeTab === 'users'" class="animate-fade-in">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="user in mockUsers" :key="user.id" class="card p-5 hover:border-zinc-700 transition-colors">
            <div class="flex items-center gap-3 mb-3">
              <img :src="user.avatar" :alt="user.name" class="w-10 h-10 rounded-full bg-zinc-800" />
              <div>
                <p class="font-semibold text-white text-sm">{{ user.name }}</p>
                <p class="text-xs text-zinc-500">{{ user.email }}</p>
              </div>
              <span class="ml-auto" :class="user.role === 'admin' ? 'badge-violet' : 'badge-blue'">{{ user.role }}</span>
            </div>
            <div class="flex items-center gap-4 text-sm">
              <div>
                <p class="text-zinc-500 text-xs">Orders</p>
                <p class="font-bold text-white">{{ user.orders }}</p>
              </div>
              <div>
                <p class="text-zinc-500 text-xs">Total Spent</p>
                <p class="font-bold text-white">{{ formatPrice(user.spent) }}</p>
              </div>
              <div class="ml-auto">
                <span :class="user.active ? 'badge-green' : 'badge-red'">{{ user.active ? 'Active' : 'Banned' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Create/Edit Event Modal -->
    <Transition name="fade">
      <div
        v-if="showCreateModal"
        class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-start justify-center p-4 pt-20 overflow-y-auto"
        @click.self="showCreateModal = false"
      >
        <div class="bg-zinc-900 border border-zinc-700 rounded-3xl max-w-2xl w-full p-6 shadow-2xl animate-slide-up mb-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-black text-white">{{ editingEvent ? 'Edit Event' : 'Create New Event' }}</h2>
            <button class="btn-ghost p-2" @click="showCreateModal = false">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Event Title</label>
                <input v-model="eventForm.title" type="text" placeholder="Enter event title" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Category</label>
                <select v-model="eventForm.category" class="input-field">
                  <option value="">Select category</option>
                  <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Date</label>
                <input v-model="eventForm.date" type="date" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Time</label>
                <input v-model="eventForm.time" type="time" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Ticket Price (VND)</label>
                <input v-model.number="eventForm.price" type="number" placeholder="0" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Venue</label>
                <input v-model="eventForm.venue" type="text" placeholder="Venue name" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">City</label>
                <input v-model="eventForm.city" type="text" placeholder="City" class="input-field" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Capacity</label>
                <input v-model.number="eventForm.capacity" type="number" placeholder="Total capacity" class="input-field" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Description</label>
                <textarea v-model="eventForm.description" rows="4" placeholder="Describe the event…" class="input-field resize-none" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Image URL</label>
                <input v-model="eventForm.image" type="url" placeholder="https://…" class="input-field" />
              </div>
            </div>

            <div class="flex gap-3 pt-2">
              <button class="btn-secondary flex-1 justify-center" @click="showCreateModal = false">Cancel</button>
              <button class="btn-primary flex-1 justify-center" @click="saveEvent">
                {{ editingEvent ? 'Save Changes' : 'Publish Event' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Delete Confirm Modal -->
    <Transition name="fade">
      <div
        v-if="deleteTarget"
        class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
        @click.self="deleteTarget = null"
      >
        <div class="bg-zinc-900 border border-zinc-700 rounded-2xl max-w-sm w-full p-6 shadow-2xl animate-slide-up text-center">
          <div class="w-14 h-14 rounded-full bg-red-500/20 border border-red-500/30 flex items-center justify-center mx-auto mb-4">
            <svg class="w-7 h-7 text-red-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <h3 class="text-lg font-bold text-white mb-2">Delete Event?</h3>
          <p class="text-zinc-400 text-sm mb-6">
            Are you sure you want to delete <span class="text-white font-semibold">{{ deleteTarget?.title }}</span>? This action cannot be undone.
          </p>
          <div class="flex gap-3">
            <button class="btn-secondary flex-1 justify-center" @click="deleteTarget = null">Cancel</button>
            <button
              class="flex-1 justify-center bg-red-600 hover:bg-red-500 text-white font-semibold px-6 py-3 rounded-xl transition-all active:scale-95 inline-flex items-center gap-2"
              @click="deleteEvent"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import AdminTable       from '@/components/AdminTable.vue'
import { events as allEvents, categories } from '@/data/events'
import { adminOrders }  from '@/data/orders'

const activeTab          = ref('events')
const showCreateModal    = ref(false)
const editingEvent       = ref(null)
const deleteTarget       = ref(null)
const eventSearch        = ref('')
const eventStatusFilter  = ref('all')
const orderSearch        = ref('')
const orderStatusFilter  = ref('all')

// Local mutable copy of events so admin can edit status
const localEvents = reactive(allEvents.map((e) => ({ ...e })))

const eventForm = reactive({
  title: '', category: '', date: '', time: '', price: 0,
  venue: '', city: '', capacity: 0, description: '', image: '',
})

const adminTabs = [
  { id: 'events', label: 'Events'  },
  { id: 'orders', label: 'Orders'  },
  { id: 'users',  label: 'Users'   },
]

const eventColumns = [
  { key: 'title',   label: 'Event'     },
  { key: 'date',    label: 'Date'      },
  { key: 'price',   label: 'Price'     },
  { key: 'sold',    label: 'Capacity'  },
  { key: 'status',  label: 'Status'    },
  { key: 'actions', label: 'Actions', align: 'right' },
]

const orderColumns = [
  { key: 'id',         label: 'Order ID'   },
  { key: 'eventTitle', label: 'Event'      },
  { key: 'total',      label: 'Total'      },
  { key: 'status',     label: 'Status'     },
  { key: 'bookedAt',   label: 'Placed At'  },
]

const filteredAdminEvents = computed(() => {
  let list = localEvents
  if (eventStatusFilter.value !== 'all') list = list.filter((e) => e.status === eventStatusFilter.value)
  if (eventSearch.value.trim()) {
    const q = eventSearch.value.toLowerCase()
    list = list.filter((e) => e.title.toLowerCase().includes(q) || e.category.toLowerCase().includes(q))
  }
  return list
})

const filteredAdminOrders = computed(() => {
  let list = adminOrders
  if (orderStatusFilter.value !== 'all') list = list.filter((o) => o.status === orderStatusFilter.value)
  if (orderSearch.value.trim()) {
    const q = orderSearch.value.toLowerCase()
    list = list.filter((o) => o.id.toLowerCase().includes(q) || o.eventTitle.toLowerCase().includes(q))
  }
  return list
})

const kpis = computed(() => [
  {
    label: 'Total Revenue', value: '₫ 42.8M', trend: '↑ 12.4%', trendUp: true,
    iconBg: 'bg-emerald-500/20', iconColor: 'text-emerald-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>',
  },
  {
    label: 'Total Events', value: localEvents.length, trend: '↑ 3 new', trendUp: true,
    iconBg: 'bg-violet-500/20', iconColor: 'text-violet-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
  },
  {
    label: 'Total Orders', value: adminOrders.length, trend: '↑ 8.1%', trendUp: true,
    iconBg: 'bg-blue-500/20', iconColor: 'text-blue-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>',
  },
  {
    label: 'Active Users', value: '8,241', trend: '↑ 5.3%', trendUp: true,
    iconBg: 'bg-amber-500/20', iconColor: 'text-amber-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  },
])

const mockUsers = [
  { id: 1, name: 'Alex Nguyen',  email: 'alex@email.com', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex', role: 'admin', orders: 3, spent: 7050000, active: true  },
  { id: 2, name: 'Nguyen Van A', email: 'nva@email.com',  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=NVA',  role: 'user',  orders: 5, spent: 12300000, active: true  },
  { id: 3, name: 'Tran Thi B',   email: 'ttb@email.com',  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=TTB',  role: 'user',  orders: 1, spent: 650000,  active: false },
  { id: 4, name: 'Le Van C',     email: 'lvc@email.com',  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=LVC',  role: 'user',  orders: 8, spent: 22000000, active: true  },
  { id: 5, name: 'Pham Thi D',   email: 'ptd@email.com',  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=PTD',  role: 'user',  orders: 2, spent: 4200000,  active: true  },
  { id: 6, name: 'Hoang Van E',  email: 'hve@email.com',  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=HVE',  role: 'user',  orders: 0, spent: 0,       active: true  },
]

function onEventStatusChange(row) {
  // In production: call API to update status
  console.log(`Event "${row.title}" status → ${row.status}`)
}

function editEvent(row) {
  editingEvent.value  = row
  Object.assign(eventForm, { ...row })
  showCreateModal.value = true
}

function confirmDelete(row) {
  deleteTarget.value = row
}

function deleteEvent() {
  const idx = localEvents.findIndex((e) => e.id === deleteTarget.value.id)
  if (idx !== -1) localEvents.splice(idx, 1)
  deleteTarget.value = null
}

function saveEvent() {
  if (editingEvent.value) {
    const idx = localEvents.findIndex((e) => e.id === editingEvent.value.id)
    if (idx !== -1) Object.assign(localEvents[idx], { ...eventForm })
  } else {
    localEvents.push({
      id: Date.now(),
      ...eventForm,
      status: 'pending',
      sold: 0,
      rating: 0,
      reviewCount: 0,
      featured: false,
      tags: [],
      organizer: { name: 'New Organizer', verified: false },
      ticketTypes: [],
    })
  }
  showCreateModal.value = false
  editingEvent.value    = null
  Object.assign(eventForm, { title: '', category: '', date: '', time: '', price: 0, venue: '', city: '', capacity: 0, description: '', image: '' })
}

function formatDate(d) {
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC' })
}
function formatDateTime(d) {
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
function orderBadge(status) {
  return { confirmed: 'badge-green', pending: 'badge-yellow', cancelled: 'badge-red' }[status] ?? 'badge-blue'
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
