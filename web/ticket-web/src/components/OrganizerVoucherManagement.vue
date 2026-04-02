<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-black text-white">Voucher Management</h2>
        <p class="text-zinc-400 text-sm mt-1">Create and manage vouchers for your events</p>
      </div>
      <button
        @click="showCreateForm = !showCreateForm"
        class="btn btn-primary"
      >
        {{ showCreateForm ? 'Cancel' : '+ Create Voucher' }}
      </button>
    </div>

    <!-- Create Form -->
    <div v-if="showCreateForm" class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6">
      <h3 class="text-lg font-semibold text-white mb-4">Create New Event Voucher</h3>
      
      <form @submit.prevent="handleCreateVoucher" class="space-y-4">
        <!-- Event Selection -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Select Event *</label>
          <select
            v-model="formData.eventId"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          >
            <option value="">Choose an event...</option>
            <option v-for="evt in publishedEvents" :key="evt.id" :value="evt.id">
              {{ evt.title }} ({{ formatDate(evt.date || evt.startTime) }})
            </option>
          </select>
        </div>

        <!-- Voucher Name -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Voucher Name *</label>
          <input
            v-model="formData.name"
            type="text"
            placeholder="e.g., Summer Special"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
        </div>

        <!-- Voucher Code -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Voucher Code *</label>
          <input
            v-model="formData.code"
            type="text"
            placeholder="e.g., LONG20"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500 uppercase"
            required
          />
          <p class="text-xs text-zinc-500 mt-1">Use A-Z, 0-9, dash or underscore</p>
        </div>

        <!-- Discount Type -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Discount Type *</label>
          <div class="grid grid-cols-2 gap-2">
            <label class="flex items-center border border-zinc-700 rounded-lg p-3 cursor-pointer text-zinc-200" :class="formData.discountType === 'PERCENTAGE' ? 'bg-violet-500/10 border-violet-500' : 'bg-zinc-950'">
              <input
                v-model="formData.discountType"
                type="radio"
                value="PERCENTAGE"
                class="mr-2"
              />
              <span>Percentage (%)</span>
            </label>
            <label class="flex items-center border border-zinc-700 rounded-lg p-3 cursor-pointer text-zinc-200" :class="formData.discountType === 'FIXED_AMOUNT' ? 'bg-violet-500/10 border-violet-500' : 'bg-zinc-950'">
              <input
                v-model="formData.discountType"
                type="radio"
                value="FIXED_AMOUNT"
                class="mr-2"
              />
              <span>Fixed Amount</span>
            </label>
          </div>
        </div>

        <!-- Discount Value -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">
            Discount Value {{ formData.discountType === 'PERCENTAGE' ? '(%)' : '(VND)' }} *
          </label>
          <input
            v-model.number="formData.discountValue"
            type="number"
            :min="formData.discountType === 'PERCENTAGE' ? '0.01' : '1000'"
            :step="formData.discountType === 'PERCENTAGE' ? '0.01' : '1000'"
            :placeholder="formData.discountType === 'PERCENTAGE' ? '20' : '100000'"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
        </div>

        <!-- Min Order Value -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Minimum Order Value (optional)</label>
          <input
            v-model.number="formData.minOrderValue"
            type="number"
            min="0"
            step="1000"
            placeholder="0 (no minimum)"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
          />
          <p class="text-xs text-zinc-500 mt-1">Leave empty for no minimum</p>
        </div>

        <!-- Usage Limit -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Usage Limit *</label>
          <input
            v-model.number="formData.usageLimit"
            type="number"
            min="1"
            step="1"
            placeholder="e.g., 100"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
          <p class="text-xs text-zinc-500 mt-1">How many times this voucher can be used</p>
        </div>

        <!-- Valid From -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Valid From *</label>
          <input
            v-model="formData.validFrom"
            type="datetime-local"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
        </div>

        <!-- Valid Until -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Valid Until *</label>
          <input
            v-model="formData.validUntil"
            type="datetime-local"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
        </div>

        <!-- Error Message -->
        <div v-if="formError" class="bg-red-500/10 border border-red-500/30 text-red-300 px-4 py-3 rounded-lg">
          {{ formError }}
        </div>

        <!-- Submit Button -->
        <div class="flex gap-3">
          <button
            type="submit"
            :disabled="loading"
            class="flex-1 btn btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? 'Creating...' : 'Create Voucher' }}
          </button>
          <button
            type="button"
            @click="resetForm"
            class="flex-1 btn btn-secondary"
          >
            Reset
          </button>
        </div>
      </form>
    </div>

    <!-- Vouchers List -->
    <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6">
      <h3 class="text-lg font-semibold text-white mb-4">My Vouchers</h3>
      
      <!-- Loading State -->
      <div v-if="loading && organizerVouchers.length === 0" class="flex justify-center py-8">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-violet-500"></div>
      </div>

      <!-- Empty State -->
      <div v-else-if="organizerVouchers.length === 0" class="text-center py-8">
        <p class="text-zinc-400">No vouchers created yet</p>
        <p class="text-zinc-500 text-sm">Create your first voucher to get started</p>
      </div>

      <!-- Vouchers Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="voucher in organizerVouchers"
          :key="voucher.id"
          class="border border-zinc-800 bg-zinc-950/70 rounded-xl p-4 hover:border-violet-500/40 transition"
        >
          <!-- Header -->
          <div class="flex items-start justify-between mb-3">
            <div>
              <h4 class="font-semibold text-zinc-100">{{ voucher.name }}</h4>
              <p class="text-xs text-zinc-500 mt-1">Event: {{ getEventLabel(voucher.eventId) }}</p>
            </div>
            <span class="badge badge-green text-xs">{{ voucher.discountType }}</span>
          </div>

          <!-- Discount -->
          <div class="mb-3">
            <p class="text-sm text-zinc-500">Discount</p>
            <p class="text-xl font-bold text-emerald-400">
              {{ isPercentDiscount(voucher.discountType) ? `${voucher.discountValue}%` : formatPrice(voucher.discountValue) }}
            </p>
          </div>

          <!-- Code -->
          <div class="mb-3 bg-zinc-900 p-2 rounded-lg border border-zinc-800">
            <p class="text-xs text-zinc-500 mb-1">Code</p>
            <div class="flex items-center gap-2">
              <code class="font-mono text-sm text-zinc-100 font-semibold flex-1">{{ voucher.code }}</code>
              <button
                @click="copyCode(voucher.code)"
                class="btn-xs bg-violet-600 hover:bg-violet-500 text-white px-2 py-1 rounded"
              >
                Copy
              </button>
            </div>
          </div>

          <!-- Usage Stats -->
          <div class="mb-3">
            <p class="text-sm text-zinc-500 mb-1">Usage</p>
            <div class="flex items-center justify-between">
              <span class="text-sm text-zinc-300">{{ voucher.usedCount }}/{{ voucher.usageLimit }}</span>
              <div class="flex-1 mx-2 h-2 bg-zinc-800 rounded-full overflow-hidden">
                <div 
                  class="h-full bg-violet-500"
                  :style="{ width: `${(voucher.usedCount / voucher.usageLimit) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>

          <!-- Validity -->
          <div class="mb-3 text-sm">
            <p class="text-zinc-500 mb-1">Valid</p>
            <p class="text-xs text-zinc-300">{{ formatDate(voucher.validFrom) }} - {{ formatDate(voucher.validUntil) }}</p>
          </div>

          <!-- Status Badge -->
          <div class="flex gap-2 mb-3">
            <span 
              :class="[
                'text-xs px-2 py-1 rounded-full',
                voucher.isActive ? 'bg-emerald-500/10 border border-emerald-500/30 text-emerald-300' : 'bg-zinc-800 border border-zinc-700 text-zinc-400'
              ]"
            >
              {{ voucher.isActive ? 'Active' : 'Inactive' }}
            </span>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <button
              @click="editVoucher(voucher)"
              class="flex-1 btn-sm btn-secondary"
            >
              Edit
            </button>
            <button
              @click="deleteVoucher(voucher.id)"
              class="flex-1 btn-sm bg-red-500 hover:bg-red-600 text-white"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useVoucherStore } from '@/stores/voucher.store'
import { useOrganizerStore } from '@/stores/organizer.store'
import { useAuthStore } from '@/stores/auth.store'

const voucherStore = useVoucherStore()
const organizerStore = useOrganizerStore()
const authStore = useAuthStore()

const showCreateForm = ref(false)
const formError = ref(null)
const loading = computed(() => voucherStore.loading)
const organizerVouchers = computed(() => voucherStore.organizerVouchers)

const formData = ref({
  eventId: '',
  name: '',
  code: '',
  discountType: 'PERCENTAGE',
  discountValue: null,
  minOrderValue: null,
  usageLimit: null,
  validFrom: '',
  validUntil: ''
})

// Get only published events
const publishedEvents = computed(() => {
  const source = Array.isArray(organizerStore.myEvents) ? organizerStore.myEvents : []
  return source.filter((eventItem) => {
    const status = String(eventItem?.status || '').toLowerCase()
    return status === 'published' || status === 'approved'
  })
})

const eventNameById = computed(() => {
  const source = Array.isArray(organizerStore.myEvents) ? organizerStore.myEvents : []
  const map = new Map()

  source.forEach((eventItem) => {
    const id = eventItem?.id
    const title = String(eventItem?.title || eventItem?.name || '').trim()
    if (id != null && title) {
      map.set(String(id), title)
    }
  })

  return map
})

function getEventLabel(eventId) {
  const key = String(eventId ?? '').trim()
  if (!key) return 'Unknown event'
  return eventNameById.value.get(key) || `#${key}`
}

function resetForm() {
  formData.value = {
    eventId: '',
    name: '',
    code: '',
    discountType: 'PERCENTAGE',
    discountValue: null,
    minOrderValue: null,
    usageLimit: null,
    validFrom: '',
    validUntil: ''
  }
  formError.value = null
}

function toBackendDateTime(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''

  // datetime-local usually returns YYYY-MM-DDTHH:mm
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(raw)) return `${raw}:00`
  return raw
}

async function handleCreateVoucher() {
  formError.value = null

  // Validation
  if (!formData.value.eventId) {
    formError.value = 'Please select an event'
    return
  }

  if (!formData.value.name) {
    formError.value = 'Voucher name is required'
    return
  }

  const normalizedCode = String(formData.value.code || '').trim().toUpperCase()
  if (!normalizedCode) {
    formError.value = 'Voucher code is required'
    return
  }
  if (!/^[A-Z0-9_-]{3,30}$/.test(normalizedCode)) {
    formError.value = 'Voucher code must be 3-30 chars: A-Z, 0-9, _ or -'
    return
  }

  if (!formData.value.discountValue || formData.value.discountValue <= 0) {
    formError.value = 'Discount value must be greater than 0'
    return
  }

  if (!formData.value.usageLimit || formData.value.usageLimit < 1) {
    formError.value = 'Usage limit must be at least 1'
    return
  }

  if (!formData.value.validFrom || !formData.value.validUntil) {
    formError.value = 'Valid from and until dates are required'
    return
  }

  const from = new Date(formData.value.validFrom)
  const until = new Date(formData.value.validUntil)

  if (from >= until) {
    formError.value = 'Valid from date must be before valid until date'
    return
  }

  try {
    const payload = {
      name: formData.value.name,
      code: normalizedCode,
      discountType: formData.value.discountType,
      discountValue: formData.value.discountValue,
      minOrderValue: formData.value.minOrderValue || 0,
      usageLimit: formData.value.usageLimit,
      validFrom: toBackendDateTime(formData.value.validFrom),
      validUntil: toBackendDateTime(formData.value.validUntil)
    }

    const selectedEventId = Number(formData.value.eventId)
    const eventIdPayload = Number.isFinite(selectedEventId) && selectedEventId > 0
      ? selectedEventId
      : formData.value.eventId

    await voucherStore.createEventVoucher(eventIdPayload, payload)
    resetForm()
    showCreateForm.value = false
  } catch (err) {
    formError.value = voucherStore.error || err.message || 'Failed to create voucher'
  }
}

function copyCode(code) {
  navigator.clipboard.writeText(code)
  alert('Voucher code copied to clipboard!')
}

function editVoucher(voucher) {
  console.log('Edit voucher:', voucher)
  // Implement edit functionality
}

async function deleteVoucher(voucherId) {
  if (confirm('Are you sure you want to delete this voucher?')) {
    try {
      await voucherStore.deleteEventVoucher(voucherId)
    } catch (err) {
      alert('Failed to delete voucher: ' + err.message)
    }
  }
}

function formatDate(dateString) {
  const date = new Date(dateString)
  if (Number.isNaN(date.getTime())) return 'N/A'
  return date.toLocaleDateString('vi-VN')
}

function formatPrice(price) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

function isPercentDiscount(type) {
  const normalized = String(type || '').toUpperCase()
  return normalized === 'PERCENTAGE' || normalized === 'PERCENT'
}

onMounted(async () => {
  try {
    await voucherStore.fetchOrganizerVouchers()

    // Fetch organizer events for voucher event picker
    if (!organizerStore.myEvents.length && authStore.user?.id) {
      await organizerStore.fetchMyEvents(authStore.user.id)
    }
  } catch (err) {
    console.error('Failed to load data:', err)
  }
})
</script>
