<template>
  <div class="space-y-6">
    <!-- Header with Create Button -->
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-black text-white">Platform Sales</h2>
      <button
        @click="showCreateForm = !showCreateForm"
        class="btn btn-primary"
      >
        {{ showCreateForm ? 'Cancel' : '+ Create Sale' }}
      </button>
    </div>

    <!-- Create Form -->
    <div v-if="showCreateForm" class="bg-zinc-900 border border-zinc-800 rounded-2xl p-6">
      <h3 class="text-lg font-semibold text-white mb-4">Create New Platform Sale</h3>
      
      <form @submit.prevent="handleCreateSale" class="space-y-4">
        <!-- Sale Name -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Sale Name *</label>
          <input
            v-model="formData.name"
            type="text"
            placeholder="e.g., Summer Sale 2026"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            required
          />
        </div>

        <!-- Description -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Description</label>
          <textarea
            v-model="formData.description"
            placeholder="Brief description of the sale"
            rows="3"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
          ></textarea>
        </div>

        <!-- Discount Percentage -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Discount Percentage * (%)</label>
          <div class="flex items-center gap-2">
            <input
              v-model.number="formData.discountPercentage"
              type="number"
              min="0.01"
              max="100"
              step="0.01"
              placeholder="e.g., 50"
              class="flex-1 px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
              required
            />
            <span class="text-zinc-400 font-semibold">%</span>
          </div>
        </div>

        <!-- Valid From -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Valid From *</label>
          <input
            v-model="formData.validFrom"
            type="date"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            readonly
            @focus="openDatePicker"
            @keydown.prevent
            required
          />
        </div>

        <!-- Valid Until -->
        <div>
          <label class="block text-sm font-medium text-zinc-300 mb-1">Valid Until *</label>
          <input
            v-model="formData.validUntil"
            type="date"
            class="w-full px-4 py-2 bg-zinc-950 text-zinc-200 border border-zinc-700 rounded-lg focus:ring-2 focus:ring-violet-500 focus:border-violet-500"
            readonly
            @focus="openDatePicker"
            @keydown.prevent
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
            {{ loading ? 'Creating...' : 'Create Sale' }}
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

    <!-- Sales List -->
    <div class="bg-zinc-900 border border-zinc-800 rounded-2xl overflow-hidden">
      <div class="p-6">
        <h3 class="text-lg font-semibold text-white mb-4">All Sales</h3>
        
        <!-- Loading State -->
        <div v-if="loading && allSales.length === 0" class="flex justify-center py-8">
          <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-violet-500"></div>
        </div>

        <!-- Empty State -->
        <div v-else-if="allSales.length === 0" class="text-center py-8">
          <p class="text-zinc-400">No sales created yet</p>
        </div>

        <!-- Sales Table -->
        <div v-else class="overflow-x-auto">
          <table class="w-full">
            <thead class="bg-zinc-950 border-b border-zinc-800">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-semibold text-zinc-400">Name</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-zinc-400">Discount</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-zinc-400">Valid Period</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-zinc-400">Status</th>
                <th class="px-6 py-3 text-left text-xs font-semibold text-zinc-400">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr 
                v-for="sale in allSales" 
                :key="sale.id"
                class="border-b border-zinc-800 hover:bg-zinc-800/50 transition"
              >
                <td class="px-6 py-4">
                  <div>
                    <p class="font-semibold text-zinc-100">{{ sale.name }}</p>
                    <p v-if="sale.description" class="text-sm text-zinc-400">{{ sale.description }}</p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="text-lg font-bold text-emerald-400">{{ sale.discountPercentage }}%</span>
                </td>
                <td class="px-6 py-4 text-sm text-zinc-300">
                  <div>
                    <p>{{ formatDateTime(sale.validFrom) }}</p>
                    <p class="text-zinc-500">to</p>
                    <p>{{ formatDateTime(sale.validUntil) }}</p>
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span 
                    :class="[
                      'text-xs px-3 py-1 rounded-full font-semibold',
                      sale.isActive
                        ? 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/30'
                        : 'bg-zinc-800 text-zinc-400 border border-zinc-700'
                    ]"
                  >
                    {{ sale.isActive ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td class="px-6 py-4">
                  <div class="flex gap-2">
                    <button
                      @click="editSale(sale)"
                      class="btn btn-sm btn-secondary"
                    >
                      Edit
                    </button>
                    <button
                      @click="confirmDelete(sale.id)"
                      class="btn btn-sm bg-red-500 hover:bg-red-600 text-white"
                    >
                      Deactivate
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePlatformSalesStore } from '@/stores/platformSales.store'

const salesStore = usePlatformSalesStore()

const allSales = computed(() => salesStore.allSales)
const loading = computed(() => salesStore.loading)

const showCreateForm = ref(false)
const formError = ref(null)

const formData = ref({
  name: '',
  description: '',
  discountPercentage: null,
  validFrom: '',
  validUntil: ''
})

function resetForm() {
  formData.value = {
    name: '',
    description: '',
    discountPercentage: null,
    validFrom: '',
    validUntil: ''
  }
  formError.value = null
}

async function handleCreateSale() {
  formError.value = null

  // Validation
  if (!formData.value.name) {
    formError.value = 'Sale name is required'
    return
  }

  if (!formData.value.discountPercentage || formData.value.discountPercentage <= 0) {
    formError.value = 'Discount percentage must be greater than 0'
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
    const fromIso = toStartOfDayIso(formData.value.validFrom)
    const untilIso = toEndOfDayIso(formData.value.validUntil)

    const payload = {
      name: formData.value.name,
      description: formData.value.description,
      discountPercentage: formData.value.discountPercentage,
      validFrom: fromIso,
      validUntil: untilIso
    }

    await salesStore.createSale(payload)
    resetForm()
    showCreateForm.value = false
  } catch (err) {
    formError.value = err.message || 'Failed to create sale'
  }
}

function editSale(sale) {
  // Implement edit functionality
  console.log('Edit sale:', sale)
}

async function confirmDelete(saleId) {
  if (confirm('Are you sure you want to deactivate this sale?')) {
    try {
      await salesStore.deactivateSale(saleId)
    } catch (err) {
      alert('Failed to deactivate sale: ' + err.message)
    }
  }
}

function formatDateTime(dateString) {
  const date = new Date(dateString)
  return date.toLocaleString('vi-VN')
}

function toStartOfDayIso(dateString) {
  return `${dateString}T00:00:00`
}

function toEndOfDayIso(dateString) {
  return `${dateString}T23:59:59`
}

function openDatePicker(event) {
  if (typeof event?.target?.showPicker === 'function') {
    event.target.showPicker()
  }
}

onMounted(async () => {
  try {
    await salesStore.fetchAllSales()
  } catch (err) {
    console.error('Failed to load sales:', err)
  }
})
</script>
