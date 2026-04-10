<template>
  <div class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6 space-y-4">
    <div class="flex items-center justify-between gap-3">
      <div>
        <h3 class="text-lg font-bold text-white">Platform Voucher Management</h3>
        <p class="text-xs text-zinc-500 mt-1">Admin can create system-wide vouchers for all eligible checkouts.</p>
      </div>
      <button class="btn-primary text-sm py-2 px-3" @click="showForm = !showForm">
        {{ showForm ? 'Cancel' : '+ Create Platform Voucher' }}
      </button>
    </div>

    <div v-if="showForm" class="rounded-xl border border-zinc-800 bg-zinc-950/50 p-4">
      <form class="grid grid-cols-1 md:grid-cols-2 gap-4" @submit.prevent="submit">
        <div class="md:col-span-2">
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Voucher Name *</label>
          <input v-model="form.name" class="input-field" type="text" placeholder="Platform New User Voucher" required />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Voucher Code *</label>
          <input v-model="form.code" class="input-field uppercase" type="text" placeholder="PLATFORM10" required />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Discount Type *</label>
          <select v-model="form.discountType" class="input-field" required>
            <option value="PERCENTAGE">PERCENTAGE</option>
            <option value="FIXED_AMOUNT">FIXED_AMOUNT</option>
          </select>
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Discount Value *</label>
          <input
            v-model.number="form.discountValue"
            class="input-field"
            type="number"
            :min="form.discountType === 'PERCENTAGE' ? 0.01 : 1000"
            :step="form.discountType === 'PERCENTAGE' ? 0.01 : 1000"
            required
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Min Order Value</label>
          <input v-model.number="form.minOrderValue" class="input-field" type="number" min="0" step="1000" placeholder="0" />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Usage Limit *</label>
          <input v-model.number="form.usageLimit" class="input-field" type="number" min="1" step="1" required />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Valid From *</label>
          <input v-model="form.validFrom" class="input-field" type="datetime-local" required />
        </div>

        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Valid Until *</label>
          <input v-model="form.validUntil" class="input-field" type="datetime-local" required />
        </div>

        <div v-if="error" class="md:col-span-2 rounded-xl border border-red-500/30 bg-red-500/10 p-3 text-sm text-red-300">
          {{ error }}
        </div>

        <div v-if="success" class="md:col-span-2 rounded-xl border border-emerald-500/30 bg-emerald-500/10 p-3 text-sm text-emerald-300">
          {{ success }}
        </div>

        <div class="md:col-span-2 flex gap-3 pt-2">
          <button type="submit" class="btn-primary" :disabled="voucherStore.loading || !auth.isAdmin">
            {{ voucherStore.loading ? 'Creating...' : 'Create Voucher' }}
          </button>
          <button type="button" class="btn-secondary" @click="resetForm">Reset</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useVoucherStore } from '@/stores/voucher.store'
import { useAuthStore } from '@/stores/auth.store'

const voucherStore = useVoucherStore()
const auth = useAuthStore()

const showForm = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  name: '',
  code: '',
  discountType: 'PERCENTAGE',
  discountValue: null,
  minOrderValue: null,
  usageLimit: null,
  validFrom: '',
  validUntil: '',
})

function toBackendDateTime(value) {
  const raw = String(value || '').trim()
  if (!raw) return ''
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(raw)) return `${raw}:00`
  return raw
}

function resetForm() {
  form.name = ''
  form.code = ''
  form.discountType = 'PERCENTAGE'
  form.discountValue = null
  form.minOrderValue = null
  form.usageLimit = null
  form.validFrom = ''
  form.validUntil = ''
  error.value = ''
  success.value = ''
}

async function submit() {
  error.value = ''
  success.value = ''

  if (!auth.isAdmin) {
    error.value = 'Only ADMIN can create platform vouchers.'
    return
  }

  const normalizedCode = String(form.code || '').trim().toUpperCase()
  if (!/^[A-Z0-9_-]{3,30}$/.test(normalizedCode)) {
    error.value = 'Voucher code must be 3-30 chars: A-Z, 0-9, _ or -'
    return
  }

  if (!form.discountValue || Number(form.discountValue) <= 0) {
    error.value = 'Discount value must be greater than 0'
    return
  }

  if (!form.usageLimit || Number(form.usageLimit) < 1) {
    error.value = 'Usage limit must be at least 1'
    return
  }

  if (!form.validFrom || !form.validUntil) {
    error.value = 'Valid from and valid until are required'
    return
  }

  if (new Date(form.validFrom) >= new Date(form.validUntil)) {
    error.value = 'Valid from must be before valid until'
    return
  }

  const minOrderValue = Number(form.minOrderValue)
  const payload = {
    name: String(form.name || '').trim(),
    code: normalizedCode,
    discountType: form.discountType,
    discountValue: Number(form.discountValue),
    minOrderValue: Number.isFinite(minOrderValue) && minOrderValue > 0 ? minOrderValue : undefined,
    usageLimit: Number(form.usageLimit),
    validFrom: toBackendDateTime(form.validFrom),
    validUntil: toBackendDateTime(form.validUntil),
  }

  try {
    await voucherStore.createPlatformVoucher(payload)
    success.value = `Platform voucher ${normalizedCode} created successfully.`
    resetForm()
    showForm.value = false
  } catch (e) {
    error.value = voucherStore.error || e.message || 'Failed to create platform voucher'
  }
}
</script>
