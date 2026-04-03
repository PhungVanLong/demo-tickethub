<template>
  <div class="min-h-screen bg-zinc-950 flex flex-col">
    <!-- Minimal header -->
    <header class="border-b border-zinc-900 px-6 py-4">
      <RouterLink to="/" class="flex items-center gap-2.5 w-fit">
        <div class="w-8 h-8 rounded-lg bg-violet-600 flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 14H9V8h2v8zm4 0h-2V8h2v8z"/>
          </svg>
        </div>
        <span class="text-lg font-bold text-white">Ticket<span class="text-violet-400">Hub</span></span>
      </RouterLink>
    </header>

    <!-- Content -->
    <main class="flex-1 flex items-center justify-center px-4 py-12">
      <RouterView />
    </main>

    <!-- Footer note -->
    <div class="text-center py-4 text-xs text-zinc-700 border-t border-zinc-900">
      © 2026 TicketHub · All rights reserved.
    </div>
  </div>
</template>
