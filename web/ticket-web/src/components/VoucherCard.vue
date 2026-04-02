<template>
  <div class="bg-zinc-900/90 rounded-2xl border border-zinc-800 p-4 hover:border-violet-500/40 hover:shadow-lg hover:shadow-violet-950/30 transition">
    <!-- Header with type badge -->
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center gap-2">
        <span v-if="voucher.voucherType === 'USER_MONTHLY'" class="badge badge-blue text-xs">
          Monthly Voucher
        </span>
        <span v-else-if="voucher.voucherType === 'ORGANIZER_EVENT'" class="badge badge-green text-xs">
          Event Voucher
        </span>
        <span v-else class="badge badge-gray text-xs">{{ voucher.voucherType }}</span>
      </div>
      
      <div v-if="isExpiring" class="badge badge-red text-xs">
        Expires Soon
      </div>
    </div>

    <!-- Voucher Code -->
    <div class="mb-4">
      <p class="text-sm text-zinc-500 mb-1">Voucher Code</p>
      <div class="flex items-center gap-2 bg-zinc-950 p-3 rounded-xl border border-zinc-800">
        <code class="font-mono text-sm text-zinc-100 font-semibold flex-1">{{ voucher.code }}</code>
        <button
          @click="copyToClipboard"
          class="btn-sm bg-violet-600 hover:bg-violet-500 text-white px-3 py-1 rounded-lg transition"
        >
          {{ copied ? 'Copied!' : 'Copy' }}
        </button>
      </div>
    </div>

    <!-- Discount Info -->
    <div class="grid grid-cols-2 gap-3 mb-4">
      <div>
        <p class="text-xs text-zinc-500 mb-1">Discount</p>
        <p class="text-lg font-bold text-emerald-400">
          {{ discountDisplay }}
        </p>
      </div>
      <div>
        <p class="text-xs text-zinc-500 mb-1">Usage</p>
        <p class="text-sm text-zinc-200">
          <span class="font-semibold">{{ voucher.usedCount || 0 }}</span>
          <span v-if="voucher.usageLimit">/{{ voucher.usageLimit }}</span>
        </p>
      </div>
    </div>

    <!-- Validity Period -->
    <div class="mb-4">
      <p class="text-xs text-zinc-500 mb-2">Valid Period</p>
      <div class="flex items-center gap-2 text-sm text-zinc-300">
        <span>{{ formatDate(voucher.validFrom) }}</span>
        <span class="text-zinc-500">→</span>
        <span :class="isExpiring ? 'text-red-400 font-semibold' : ''">
          {{ formatDate(voucher.validUntil) }}
        </span>
      </div>
    </div>

    <!-- Min Order Value (if applicable) -->
    <div v-if="voucher.minOrderValue" class="mb-4 text-sm text-zinc-300">
      <span class="text-xs text-zinc-500">Min. Order Value: </span>
      {{ formatPrice(voucher.minOrderValue) }}
    </div>

    <!-- Status Badge -->
    <div class="flex items-center gap-2">
      <span 
        :class="[
          'text-xs px-2 py-1 rounded-full',
          voucher.isActive ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/30' : 'bg-zinc-800 text-zinc-400 border border-zinc-700'
        ]"
      >
        {{ voucher.isActive ? 'Active' : 'Inactive' }}
      </span>
      <span 
        v-if="isExpired"
        class="text-xs px-2 py-1 rounded-full bg-red-500/10 border border-red-500/30 text-red-300"
      >
        Expired
      </span>
    </div>

    <!-- Action Buttons (if provided) -->
    <div v-if="showActions" class="flex gap-2 mt-4">
      <button
        @click="$emit('edit')"
        class="flex-1 btn-sm btn-secondary text-center"
      >
        Edit
      </button>
      <button
        @click="$emit('delete')"
        class="flex-1 btn-sm bg-red-500 hover:bg-red-600 text-white text-center"
      >
        Delete
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  voucher: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['edit', 'delete'])

const copied = ref(false)

const isExpired = computed(() => {
  return new Date(props.voucher.validUntil) < new Date()
})

const isExpiring = computed(() => {
  const expiryDate = new Date(props.voucher.validUntil)
  const threeDaysFromNow = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)
  return !isExpired.value && expiryDate <= threeDaysFromNow
})

const discountDisplay = computed(() => {
  const discountType = String(props.voucher.discountType || '').toUpperCase()
  if (discountType === 'PERCENTAGE' || discountType === 'PERCENT') {
    return `${props.voucher.discountValue}%`
  } else if (discountType === 'FIXED_AMOUNT' || discountType === 'FIXED') {
    return formatPrice(props.voucher.discountValue)
  }
  return props.voucher.discountValue
})

function formatDate(dateString) {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN')
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function copyToClipboard() {
  navigator.clipboard.writeText(props.voucher.code)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}
</script>
