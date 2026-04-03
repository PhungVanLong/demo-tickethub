<template>
  <div class="min-h-screen py-24 px-4">
    <div class="max-w-6xl mx-auto">

      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-black text-white">Organizer Dashboard</h1>
          <p class="text-zinc-500 mt-1">Manage your events and track performance</p>
        </div>
        <RouterLink 
          to="/organizer/events/create" 
          class="btn-primary"
          :class="{ 'opacity-50 pointer-events-none': !hasOrganizerRole() }"
          :title="hasOrganizerRole() ? '' : 'Requires ORGANIZER role from JWT'"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
          New Event
        </RouterLink>
      </div>

      <!-- Stats cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-1">Total Events</p>
          <p class="text-2xl font-black text-white">{{ organizer.myEvents.length }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-1">Published</p>
          <p class="text-2xl font-black text-emerald-400">{{ organizer.publishedCount }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-1">Pending Approval</p>
          <p class="text-2xl font-black text-yellow-400">{{ organizer.pendingCount }}</p>
        </div>
        <div class="card p-5">
          <p class="text-xs text-zinc-500 uppercase tracking-wide mb-1">Total Sold</p>
          <p class="text-2xl font-black text-violet-400">{{ organizer.stats?.totalSoldTickets ?? '—' }}</p>
        </div>
      </div>

      <!-- Events table -->
      <div class="card overflow-hidden">
        <div class="flex items-center justify-between p-5 border-b border-zinc-800">
          <h2 class="font-bold text-white">My Events</h2>
          <input
            v-model="search"
            type="text"
            placeholder="Search…"
            class="input-field text-sm py-2 max-w-[240px]"
          />
        </div>

        <!-- Loading -->
        <div v-if="organizer.loading" class="flex justify-center py-12">
          <div class="w-8 h-8 rounded-full border-4 border-violet-600 border-t-transparent animate-spin" />
        </div>

        <!-- Empty -->
        <div v-else-if="!filteredEvents.length" class="py-16 text-center">
          <svg class="w-12 h-12 mx-auto text-zinc-700 mb-3" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
            <rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
          <p class="text-zinc-500">{{ search ? 'No events match your search' : 'No events yet — create your first one!' }}</p>
        </div>

        <!-- Table -->
        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead>
              <tr class="text-xs text-zinc-500 uppercase tracking-wide border-b border-zinc-800">
                <th class="text-left px-5 py-3 font-semibold">Event</th>
                <th class="text-left px-5 py-3 font-semibold hidden sm:table-cell">Date</th>
                <th class="text-left px-5 py-3 font-semibold hidden md:table-cell">Status</th>
                <th class="text-right px-5 py-3 font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-zinc-800/60">
              <tr
                v-for="event in filteredEvents"
                :key="event.id"
                class="hover:bg-zinc-800/30 transition-colors"
              >
                <td class="px-5 py-4">
                  <div class="flex items-center gap-3">
                    <img
                      v-if="event.image"
                      :src="event.image"
                      :alt="event.title"
                      class="w-10 h-10 rounded-lg object-cover shrink-0"
                    />
                    <div class="w-10 h-10 rounded-lg bg-zinc-800 shrink-0 flex items-center justify-center" v-else>
                      <svg class="w-5 h-5 text-zinc-600" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/></svg>
                    </div>
                    <div class="min-w-0">
                      <p class="font-medium text-white truncate max-w-[200px]">{{ event.title }}</p>
                      <p class="text-xs text-zinc-500">{{ event.venue }}, {{ event.city }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-5 py-4 text-zinc-400 hidden sm:table-cell">{{ formatDate(event.date) }}</td>
                <td class="px-5 py-4 hidden md:table-cell">
                  <span :class="statusBadge(event.status)" class="capitalize">{{ event.status }}</span>
                </td>
                <td class="px-5 py-4 text-right">
                  <div class="flex items-center gap-2 justify-end">
                    <RouterLink
                      :to="`/event/${event.id}`"
                      class="btn-ghost py-1 px-2 text-xs"
                    >
                      View
                    </RouterLink>
                    <RouterLink
                      :to="`/organizer/events/${event.id}/edit`"
                      class="btn-ghost py-1 px-2 text-xs"
                      :class="{ 'opacity-50 pointer-events-none': !hasOrganizerRole() }"
                    >
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      Edit
                    </RouterLink>
                    <button
                      :disabled="!hasOrganizerRole()"
                      class="py-1 px-2 rounded-lg text-xs text-red-400 hover:text-red-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                      @click="confirmDelete(event)"
                    >
                      <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Voucher Management Section -->
      <div class="mt-16">
        <OrganizerVoucherManagement />
      </div>

      <!-- Delete confirm -->
      <Transition name="fade">
        <div
          v-if="deleteTarget"
          class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
          @click.self="deleteTarget = null"
        >
          <div class="bg-zinc-900 border border-zinc-700 rounded-2xl max-w-sm w-full p-6 text-center shadow-2xl">
            <div class="w-12 h-12 rounded-full bg-red-500/20 border border-red-500/30 flex items-center justify-center mx-auto mb-4">
              <svg class="w-6 h-6 text-red-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/></svg>
            </div>
            <h3 class="text-lg font-bold text-white mb-2">Delete Event?</h3>
            <p class="text-zinc-400 text-sm mb-6">
              Are you sure you want to delete <span class="text-white font-semibold">{{ deleteTarget.title }}</span>?
            </p>
            <div class="flex gap-3">
              <button class="btn-secondary flex-1 justify-center" @click="deleteTarget = null">Cancel</button>
              <button
                class="flex-1 justify-center bg-red-600 hover:bg-red-500 text-white font-semibold px-4 py-3 rounded-xl transition-all active:scale-95 inline-flex items-center gap-2"
                @click="doDelete"
              >Delete</button>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import OrganizerVoucherManagement from '@/components/OrganizerVoucherManagement.vue'
import { useAuthStore }      from '@/stores/auth.store'
import { useOrganizerStore } from '@/stores/organizer.store'

const auth      = useAuthStore()
const organizer = useOrganizerStore()

const search      = ref('')
const deleteTarget = ref(null)

// Helper to check if current user has ORGANIZER role from JWT
function hasOrganizerRole() {
  return auth.isOrganizer && auth.tokenValidated
}

// Helper to warn and prevent action if not authorized
function requireOrganizerRole(actionName = 'action') {
  if (!hasOrganizerRole()) {
    console.warn(`[Security] Unauthorized ${actionName}: User does not have ORGANIZER role in JWT`)
    return false
  }
  return true
}

onMounted(async () => {
  if (auth.user?.id) {
    await organizer.fetchMyEvents(auth.user.id)
    await organizer.fetchStats(auth.user.id)
  }
})

const filteredEvents = computed(() => {
  if (!search.value.trim()) return organizer.myEvents
  const q = search.value.toLowerCase()
  return organizer.myEvents.filter((e) =>
    e.title.toLowerCase().includes(q) || (e.city ?? '').toLowerCase().includes(q)
  )
})

function formatDate(d) {
  if (!d) return '—'
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC' })
}

function statusBadge(s) {
  return {
    published: 'badge-green',
    approved:  'badge-green',
    pending:   'badge-yellow',
    draft:     'badge-blue',
    rejected:  'badge-red',
    cancelled: 'badge-red',
  }[s] ?? 'badge-blue'
}

function confirmDelete(event) { 
  if (!requireOrganizerRole('event deletion')) return
  deleteTarget.value = event 
}

async function doDelete() {
  if (!requireOrganizerRole('event deletion')) return
  await organizer.deleteMyEvent(deleteTarget.value.id)
  deleteTarget.value = null
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
