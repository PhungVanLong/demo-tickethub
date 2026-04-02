<template>
  <header
    class="fixed top-0 inset-x-0 z-50 transition-all duration-300"
    :class="scrolled ? 'bg-zinc-950/95 backdrop-blur-xl border-b border-zinc-800 shadow-lg shadow-black/30' : 'bg-transparent'"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">

        <!-- Logo -->
        <RouterLink to="/" class="flex items-center gap-2.5 group">
          <div class="w-8 h-8 rounded-lg bg-violet-600 flex items-center justify-center group-hover:bg-violet-500 transition-colors">
            <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/>
            </svg>
          </div>
          <span class="text-lg font-bold text-white">Ticket<span class="text-violet-400">Hub</span></span>
        </RouterLink>

        <!-- Desktop Nav -->
        <nav class="hidden md:flex items-center gap-1">
          <RouterLink to="/" class="btn-ghost text-sm" active-class="!text-white !bg-zinc-800">
            Browse Events
          </RouterLink>
          <RouterLink
            v-if="auth.isOrganizer && !auth.isAdmin"
            to="/organizer"
            class="btn-ghost text-sm"
            active-class="!text-white !bg-zinc-800"
          >
            My Events
          </RouterLink>
          <RouterLink
            v-if="auth.isAdmin"
            to="/admin"
            class="btn-ghost text-sm"
            active-class="!text-white !bg-zinc-800"
          >
            Admin Panel
          </RouterLink>
        </nav>

        <!-- Right side -->
        <div class="flex items-center gap-3">

          <!-- Search trigger -->
          <button
            class="hidden sm:flex btn-ghost text-sm"
            @click="showSearch = true"
            aria-label="Search events"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            <span class="hidden lg:inline">Search</span>
          </button>

          <!-- Logged in user -->
          <template v-if="auth.isLoggedIn">
            <div class="relative" ref="dropdownRef">
              <button
                @click="dropdownOpen = !dropdownOpen"
                class="flex items-center gap-2.5 bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 hover:border-zinc-600 rounded-xl px-3 py-2 transition-all duration-200"
              >
                <img
                  :src="auth.user.avatar"
                  :alt="auth.user.name"
                  class="w-7 h-7 rounded-full bg-zinc-700"
                />
                <span class="hidden sm:block text-sm font-medium text-white max-w-[120px] truncate">
                  {{ auth.user.name }}
                </span>
                <svg class="w-4 h-4 text-zinc-400 transition-transform" :class="dropdownOpen && 'rotate-180'" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                  <path d="m6 9 6 6 6-6"/>
                </svg>
              </button>

              <!-- Dropdown -->
              <Transition
                enter-active-class="transition ease-out duration-150"
                enter-from-class="opacity-0 scale-95 -translate-y-2"
                enter-to-class="opacity-100 scale-100 translate-y-0"
                leave-active-class="transition ease-in duration-100"
                leave-from-class="opacity-100 scale-100 translate-y-0"
                leave-to-class="opacity-0 scale-95 -translate-y-2"
              >
                <div
                  v-if="dropdownOpen"
                  class="absolute right-0 mt-2 w-56 bg-zinc-900 border border-zinc-700 rounded-2xl shadow-2xl shadow-black/50 overflow-hidden origin-top-right"
                >
                  <div class="px-4 py-3 border-b border-zinc-800">
                    <p class="text-sm font-semibold text-white truncate">{{ auth.user.name }}</p>
                    <p class="text-xs text-zinc-500 truncate">{{ auth.user.email }}</p>
                    <span
                      class="mt-1.5 inline-block"
                      :class="auth.isAdmin ? 'badge-violet' : 'badge-blue'"
                    >
                      {{ auth.isAdmin ? 'Admin' : 'Member' }}
                    </span>
                  </div>

                  <div class="py-1">
                    <RouterLink
                      to="/profile"
                      class="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors"
                      @click="dropdownOpen = false"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                      My Tickets
                    </RouterLink>
                    <RouterLink
                      v-if="auth.isOrganizer && !auth.isAdmin"
                      to="/organizer"
                      class="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors"
                      @click="dropdownOpen = false"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
                      My Events
                    </RouterLink>
                    <RouterLink
                      v-if="auth.isAdmin"
                      to="/admin"
                      class="flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors"
                      @click="dropdownOpen = false"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
                      Admin Panel
                    </RouterLink>
                    <button
                      class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors"
                      @click="toggleRole"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                      Switch to {{ auth.isAdmin ? 'User' : 'Admin' }}
                    </button>
                  </div>

                  <div class="py-1 border-t border-zinc-800">
                    <button
                      class="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:text-red-300 hover:bg-zinc-800 transition-colors"
                      @click="handleLogout"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                      Sign Out
                    </button>
                  </div>
                </div>
              </Transition>
            </div>
          </template>

          <!-- Not logged in -->
          <template v-else>
            <RouterLink to="/login"  class="btn-secondary text-sm py-2 px-4">Sign In</RouterLink>
            <RouterLink to="/register" class="btn-primary text-sm py-2 px-4">Sign Up</RouterLink>
          </template>

          <!-- Mobile menu button -->
          <button
            class="md:hidden btn-ghost p-2"
            @click="mobileOpen = !mobileOpen"
            aria-label="Toggle menu"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path v-if="!mobileOpen" d="M3 12h18M3 6h18M3 18h18"/>
              <path v-else d="M18 6 6 18M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile nav -->
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 -translate-y-2"
        enter-to-class="opacity-100 translate-y-0"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100 translate-y-0"
        leave-to-class="opacity-0 -translate-y-2"
      >
        <div v-if="mobileOpen" class="md:hidden border-t border-zinc-800 py-3 space-y-1">
          <RouterLink to="/" class="flex items-center px-3 py-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors" @click="mobileOpen = false">Browse Events</RouterLink>
          <RouterLink v-if="auth.isLoggedIn" to="/profile" class="flex items-center px-3 py-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors" @click="mobileOpen = false">My Tickets</RouterLink>
          <RouterLink v-if="auth.isOrganizer && !auth.isAdmin" to="/organizer" class="flex items-center px-3 py-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors" @click="mobileOpen = false">My Events</RouterLink>
          <RouterLink v-if="auth.isAdmin" to="/admin" class="flex items-center px-3 py-2.5 rounded-xl text-zinc-300 hover:text-white hover:bg-zinc-800 transition-colors" @click="mobileOpen = false">Admin Panel</RouterLink>
        </div>
      </Transition>
    </div>
  </header>

  <!-- Search Modal -->
  <Transition
    enter-active-class="transition ease-out duration-200"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition ease-in duration-150"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div
      v-if="showSearch"
      class="fixed inset-0 z-[100] bg-black/70 backdrop-blur-sm flex items-start justify-center pt-24 px-4"
      @click.self="showSearch = false"
    >
      <div class="bg-zinc-900 border border-zinc-700 rounded-2xl w-full max-w-xl shadow-2xl animate-slide-up">
        <div class="flex items-center gap-3 p-4 border-b border-zinc-800">
          <svg class="w-5 h-5 text-zinc-400 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            ref="searchInput"
            v-model="searchQuery"
            type="text"
            placeholder="Search events, artists, venues…"
            class="flex-1 bg-transparent text-white placeholder-zinc-500 focus:outline-none text-base"
            @keydown.escape="showSearch = false"
          />
          <button @click="showSearch = false" class="text-zinc-500 hover:text-white transition-colors">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></svg>
          </button>
        </div>
        <div class="p-4">
          <template v-if="searchResults.length">
            <p class="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Results</p>
            <RouterLink
              v-for="result in searchResults"
              :key="result.id"
              :to="`/event/${result.id}`"
              class="flex items-center gap-3 p-3 rounded-xl hover:bg-zinc-800 transition-colors group"
              @click="showSearch = false; searchQuery = ''"
            >
              <img :src="result.image" :alt="result.title" class="w-12 h-12 rounded-lg object-cover" />
              <div class="min-w-0">
                <p class="text-sm font-semibold text-white group-hover:text-violet-300 transition-colors truncate">{{ result.title }}</p>
                <p class="text-xs text-zinc-500">{{ result.date }} · {{ result.venue }}</p>
              </div>
            </RouterLink>
          </template>
          <template v-else-if="searchQuery">
            <p class="text-zinc-500 text-sm text-center py-6">No events found for "{{ searchQuery }}"</p>
          </template>
          <template v-else>
            <p class="text-zinc-600 text-sm text-center py-6">Start typing to search events…</p>
          </template>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useEventStore } from '@/stores/event.store'

const auth        = useAuthStore()
const router      = useRouter()
const scrolled    = ref(false)
const dropdownOpen = ref(false)
const mobileOpen  = ref(false)
const showSearch  = ref(false)
const searchQuery = ref('')
const dropdownRef = ref(null)
const searchInput = ref(null)

const eventStore    = useEventStore()
const searchResults = computed(() => {
  if (!searchQuery.value.trim()) return []
  const q = searchQuery.value.toLowerCase()
  return eventStore.events.filter(
    (e) =>
      (e.title  ?? '').toLowerCase().includes(q) ||
      (e.venue  ?? '').toLowerCase().includes(q) ||
      (e.city   ?? '').toLowerCase().includes(q) ||
      (e.category ?? '').toLowerCase().includes(q)
  ).slice(0, 5)
})

watch(showSearch, async (val) => {
  if (val) {
    await nextTick()
    searchInput.value?.focus()
  } else {
    searchQuery.value = ''
  }
})

function handleScroll() {
  scrolled.value = window.scrollY > 20
}

function handleClickOutside(e) {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

function handleLogout() {
  dropdownOpen.value = false
  auth.logout()
  router.push('/')
}

function toggleRole() {
  auth.switchRole()
  dropdownOpen.value = false
  if (auth.isAdmin) {
    router.push('/admin')
  } else {
    router.push('/')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
})
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})
</script>
