<template>
  <div class="space-y-3">
    <h3 class="text-base font-semibold text-white">Select Tickets</h3>

    <div
      v-for="type in ticketTypes"
      :key="type.id || type.name"
      class="card p-4 transition-all duration-200"
      :class="
        getQty(getTypeId(type)) > 0
          ? 'border-violet-500/50 bg-violet-500/5'
          : 'hover:border-zinc-700'
      "
    >
      <div class="flex items-start justify-between gap-4">
        <!-- Info -->
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2 flex-wrap">
            <p class="font-semibold text-white text-sm">{{ type.name }}</p>
            <span v-if="typeAvailability(type) <= 20 && typeAvailability(type) > 0" class="badge-red text-[10px]">
              Only {{ typeAvailability(type) }} left!
            </span>
            <span v-else-if="typeAvailability(type) <= 100 && typeAvailability(type) > 20" class="badge-yellow text-[10px]">
              Limited
            </span>
            <span v-else-if="typeAvailability(type) <= 0" class="badge-red text-[10px]">
              Sold out
            </span>
          </div>
          <p class="text-lg font-bold text-violet-400 mt-0.5">{{ formatPrice(type.price) }}</p>
          <p class="text-xs text-zinc-500 mt-0.5">Max {{ typeMaxPerOrder(type) }} per order</p>
        </div>

        <!-- Qty control -->
        <div class="flex items-center gap-3 shrink-0">
          <button
            class="w-9 h-9 rounded-full border border-zinc-700 flex items-center justify-center text-zinc-400
                   hover:border-zinc-500 hover:text-white active:scale-90
                   disabled:opacity-30 disabled:cursor-not-allowed transition-all duration-150"
                 :disabled="getQty(getTypeId(type)) === 0"
            @click="decrement(type)"
            aria-label="Remove ticket"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </button>

          <span
            class="w-8 text-center font-bold text-white text-base tabular-nums transition-all"
            :class="getQty(getTypeId(type)) > 0 ? 'text-violet-300' : ''"
          >
            {{ getQty(getTypeId(type)) }}
          </span>

          <button
            class="w-9 h-9 rounded-full border border-zinc-700 flex items-center justify-center text-zinc-400
                   hover:border-violet-500 hover:text-violet-400 active:scale-90
                   disabled:opacity-30 disabled:cursor-not-allowed transition-all duration-150"
                 :disabled="getQty(getTypeId(type)) >= typeMaxPerOrder(type) || getQty(getTypeId(type)) >= typeAvailability(type)"
            @click="increment(type)"
            aria-label="Add ticket"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Sub-total for this type -->
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 max-h-0"
        enter-to-class="opacity-100 max-h-10"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100 max-h-10"
        leave-to-class="opacity-0 max-h-0"
      >
        <div
          v-if="getQty(getTypeId(type)) > 0"
          class="mt-3 pt-3 border-t border-violet-500/20 flex items-center justify-between overflow-hidden"
        >
          <span class="text-xs text-zinc-400">{{ getQty(getTypeId(type)) }} × {{ formatPrice(type.price) }}</span>
          <span class="text-sm font-semibold text-white">{{ formatPrice(type.price * getQty(getTypeId(type))) }}</span>
        </div>
      </Transition>
    </div>

    <!-- Total summary strip -->
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0 translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
    >
      <div
        v-if="totalTickets > 0"
        class="flex items-center justify-between bg-violet-600/10 border border-violet-500/30 rounded-xl px-4 py-3"
      >
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4 text-violet-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path d="M20 12v10H4V12M22 7H2v5h20V7zM12 22V7M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7zM12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/>
          </svg>
          <span class="text-sm text-zinc-300">
            <span class="font-bold text-white">{{ totalTickets }}</span> ticket{{ totalTickets > 1 ? 's' : '' }} selected
          </span>
        </div>
        <span class="text-base font-bold text-white">{{ formatPrice(subtotal) }}</span>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  ticketTypes: { type: Array, required: true },
  modelValue:  { type: Array, required: true },  // [{ ticketType, qty }]
})

const emit = defineEmits(['update:modelValue'])

function getTypeId(type) {
  return type?.id ?? type?.ticketTierId ?? type?.tierId ?? type?.ticketTypeId ?? type?.name
}

function typeAvailability(type) {
  const raw = type?.available
    ?? type?.quantityAvailable
    ?? type?.availableCount
    ?? type?.remainingCount
    ?? type?.remaining
    ?? type?.stock
    ?? 0
  const value = Number(raw)
  return Number.isFinite(value) ? Math.max(0, value) : 0
}

function typeMaxPerOrder(type) {
  const raw = type?.maxPerOrder ?? type?.maxPerUser ?? type?.limitPerOrder ?? 4
  const value = Number(raw)
  return Number.isFinite(value) ? Math.max(1, value) : 4
}

function getQty(typeId) {
  return props.modelValue.find((s) => getTypeId(s.ticketType) === typeId)?.qty ?? 0
}

function setQty(type, qty) {
  const typeId = getTypeId(type)
  const updated = props.ticketTypes.map((t) => ({
    ticketType: t,
    qty: getTypeId(t) === typeId ? qty : getQty(getTypeId(t)),
  }))
  emit('update:modelValue', updated)
}

function increment(type) {
  const typeId = getTypeId(type)
  const current = getQty(typeId)
  if (current < typeMaxPerOrder(type) && current < typeAvailability(type)) {
    setQty(type, current + 1)
  }
}

function decrement(type) {
  const current = getQty(getTypeId(type))
  if (current > 0) setQty(type, current - 1)
}

const totalTickets = computed(() =>
  props.modelValue.reduce((sum, s) => sum + s.qty, 0)
)

const subtotal = computed(() =>
  props.modelValue.reduce((sum, s) => sum + s.ticketType.price * s.qty, 0)
)

function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>
