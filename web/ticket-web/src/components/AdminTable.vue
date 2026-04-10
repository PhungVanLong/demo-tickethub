<template>
  <div class="overflow-x-auto rounded-2xl border border-zinc-800">
    <table class="w-full text-sm">
      <thead>
        <tr class="bg-zinc-900 border-b border-zinc-800">
          <th
            v-for="col in columns"
            :key="col.key"
            class="px-4 py-3.5 text-left text-xs font-semibold text-zinc-400 uppercase tracking-wider whitespace-nowrap"
            :class="col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : ''"
          >
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody class="divide-y divide-zinc-800/60">
        <tr
          v-for="(row, idx) in rows"
          :key="getRowKey(row, idx)"
          class="bg-zinc-950 hover:bg-zinc-900/70 transition-colors duration-150 group"
        >
          <td
            v-for="col in columns"
            :key="col.key"
            class="px-4 py-3.5 text-zinc-300 group-hover:text-white transition-colors"
            :class="col.align === 'right' ? 'text-right' : col.align === 'center' ? 'text-center' : ''"
          >
            <!-- Custom slot per column -->
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              {{ row[col.key] }}
            </slot>
          </td>
        </tr>
        <tr v-if="rows.length === 0">
          <td :colspan="columns.length" class="px-4 py-12 text-center text-zinc-500">
            <svg class="w-10 h-10 mx-auto mb-3 text-zinc-700" fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
            </svg>
            {{ emptyText || 'No data available' }}
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-between px-4 py-3 border-t border-zinc-800 bg-zinc-900/50">
      <p class="text-xs text-zinc-500">
        Showing {{ (currentPage - 1) * pageSize + 1 }}–{{ Math.min(currentPage * pageSize, totalRows) }} of {{ totalRows }}
      </p>
      <div class="flex items-center gap-1">
        <button
          class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          :disabled="currentPage === 1"
          @click="$emit('page', currentPage - 1)"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
        </button>
        <button
          v-for="p in pageNumbers"
          :key="p"
          class="w-8 h-8 rounded-lg text-xs font-semibold transition-colors"
          :class="p === currentPage ? 'bg-violet-600 text-white' : 'text-zinc-400 hover:text-white hover:bg-zinc-800'"
          @click="$emit('page', p)"
        >{{ p }}</button>
        <button
          class="w-8 h-8 rounded-lg flex items-center justify-center text-zinc-400 hover:text-white hover:bg-zinc-800 transition-colors disabled:opacity-30 disabled:cursor-not-allowed"
          :disabled="currentPage === totalPages"
          @click="$emit('page', currentPage + 1)"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m9 18 6-6-6-6"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  columns:     { type: Array,  required: true },
  rows:        { type: Array,  required: true },
  totalRows:   { type: Number, default: 0     },
  currentPage: { type: Number, default: 1     },
  pageSize:    { type: Number, default: 10    },
  emptyText:   { type: String, default: ''    },
})

defineEmits(['page'])

function getRowKey(row, idx) {
  const candidates = [
    row?.id,
    row?.orderId,
    row?.eventId,
    row?.paymentId,
    row?.paymentCode,
    row?.email,
  ]

  for (const value of candidates) {
    if (value != null && String(value).trim()) {
      return String(value)
    }
  }

  return `row-${idx}`
}

const totalPages = computed(() => Math.ceil(props.totalRows / props.pageSize))

const pageNumbers = computed(() => {
  const pages = []
  const start = Math.max(1, props.currentPage - 2)
  const end   = Math.min(totalPages.value, props.currentPage + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>
