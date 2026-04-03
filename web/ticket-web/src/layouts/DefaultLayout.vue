<template>
  <div class="min-h-screen flex flex-col bg-zinc-950">
    <Navbar />

    <main class="flex-1 pt-16">
      <RouterView v-slot="{ Component, route }">
        <Transition
          :name="route.meta.transition ?? 'page'"
          mode="out-in"
          appear
        >
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>

    <footer class="border-t border-zinc-800 bg-zinc-950">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 mb-10">
          <!-- Brand -->
          <div class="lg:col-span-1">
            <div class="flex items-center gap-2.5 mb-4">
              <div class="w-8 h-8 rounded-lg bg-violet-600 flex items-center justify-center">
                <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/>
                </svg>
              </div>
              <span class="text-lg font-bold text-white">Ticket<span class="text-violet-400">Hub</span></span>
            </div>
            <p class="text-zinc-500 text-sm leading-relaxed">
              Your premium destination for events in Vietnam. Book tickets, discover experiences.
            </p>
          </div>

          <!-- Links -->
          <div v-for="group in footerLinks" :key="group.title">
            <p class="text-xs font-semibold text-zinc-400 uppercase tracking-widest mb-4">{{ group.title }}</p>
            <ul class="space-y-2.5">
              <li v-for="link in group.links" :key="link">
                <a href="#" class="text-sm text-zinc-500 hover:text-white transition-colors">{{ link }}</a>
              </li>
            </ul>
          </div>
        </div>

        <div class="divider mb-6" />

        <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
          <p class="text-xs text-zinc-600">© 2026 TicketHub · All rights reserved.</p>
          <div class="flex items-center gap-4">
            <a
              v-for="social in socials"
              :key="social.label"
              href="#"
              class="text-zinc-600 hover:text-violet-400 transition-colors"
              :aria-label="social.label"
            >
              <span v-html="social.icon" class="w-5 h-5 block" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import Navbar from '@/components/Navbar.vue'

const footerLinks = [
  {
    title: 'Discover',
    links: ['Browse Events', 'Featured', 'Concerts', 'Festivals', 'Sports'],
  },
  {
    title: 'Support',
    links: ['Help Center', 'Contact Us', 'Refund Policy', 'Terms of Service'],
  },
  {
    title: 'Company',
    links: ['About Us', 'Careers', 'Press', 'Advertise'],
  },
]

const socials = [
  {
    label: 'Facebook',
    icon: '<svg fill="currentColor" viewBox="0 0 24 24"><path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z"/></svg>',
  },
  {
    label: 'Instagram',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="2" y="2" width="20" height="20" rx="5"/><path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z"/><line x1="17.5" y1="6.5" x2="17.51" y2="6.5"/></svg>',
  },
  {
    label: 'Twitter / X',
    icon: '<svg fill="currentColor" viewBox="0 0 24 24"><path d="M22 4s-.7 2.1-2 3.4c1.6 10-9.4 17.3-18 11.6 2.2.1 4.4-.6 6-2C3 15.5.5 9.6 3 5c2.2 2.6 5.6 4.1 9 4-.9-4.2 4-6.6 7-3.8 1.1 0 3-1.2 3-1.2z"/></svg>',
  },
  {
    label: 'YouTube',
    icon: '<svg fill="currentColor" viewBox="0 0 24 24"><path d="M22.54 6.42a2.78 2.78 0 0 0-1.95-1.96C18.88 4 12 4 12 4s-6.88 0-8.59.46a2.78 2.78 0 0 0-1.95 1.96A29 29 0 0 0 1 12a29 29 0 0 0 .46 5.58A2.78 2.78 0 0 0 3.41 19.6C5.12 20 12 20 12 20s6.88 0 8.59-.46a2.78 2.78 0 0 0 1.95-1.95A29 29 0 0 0 23 12a29 29 0 0 0-.46-5.58z"/><polygon fill="white" points="9.75 15.02 15.5 12 9.75 8.98 9.75 15.02"/></svg>',
  },
]
</script>
