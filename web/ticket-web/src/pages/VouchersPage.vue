<template>
  <div class="min-h-screen py-24">
    <div class="max-w-6xl mx-auto px-4">
      <!-- Header -->
      <div class="mb-8">
        <p class="text-xs font-semibold text-violet-400 uppercase tracking-widest mb-2">Savings Wallet</p>
        <h1 class="text-3xl font-black text-white mb-2">My Vouchers</h1>
        <p class="text-zinc-400">Manage and use your vouchers to save on event tickets</p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
      </div>

      <!-- Tabs -->
      <div v-else class="space-y-8">
        <!-- Tab Navigation -->
        <div class="flex gap-2 border-b border-zinc-800 overflow-x-auto">
          <button
            @click="activeTab = 'active'"
            :class="[
              'px-4 py-2 text-sm whitespace-nowrap font-semibold border-b-2 transition',
              activeTab === 'active'
                ? 'text-violet-300 border-violet-500'
                : 'text-zinc-400 border-transparent hover:text-zinc-200'
            ]"
          >
            Active Vouchers
            <span class="ml-2 bg-violet-500/15 text-violet-300 text-xs font-bold px-2 py-1 rounded-full border border-violet-500/30">
              {{ activeVouchers.length }}
            </span>
          </button>
          
          <button
            @click="activeTab = 'expiring'"
            :class="[
              'px-4 py-2 text-sm whitespace-nowrap font-semibold border-b-2 transition',
              activeTab === 'expiring'
                ? 'text-violet-300 border-violet-500'
                : 'text-zinc-400 border-transparent hover:text-zinc-200'
            ]"
          >
            Expiring Soon
            <span v-if="expiringSoon.length > 0" class="ml-2 bg-red-500/10 text-red-300 text-xs font-bold px-2 py-1 rounded-full border border-red-500/30">
              {{ expiringSoon.length }}
            </span>
          </button>
          
          <button
            @click="activeTab = 'all'"
            :class="[
              'px-4 py-2 text-sm whitespace-nowrap font-semibold border-b-2 transition',
              activeTab === 'all'
                ? 'text-violet-300 border-violet-500'
                : 'text-zinc-400 border-transparent hover:text-zinc-200'
            ]"
          >
            All Vouchers
            <span class="ml-2 bg-zinc-800 text-zinc-300 text-xs font-bold px-2 py-1 rounded-full border border-zinc-700">
              {{ myVouchers.length }}
            </span>
          </button>
        </div>

        <!-- Active Tab Content -->
        <div v-if="activeTab === 'active'" class="space-y-4">
          <div v-if="activeVouchers.length === 0" class="text-center py-12">
            <p class="text-zinc-300 text-lg">No active vouchers yet</p>
            <p class="text-zinc-500 text-sm mt-2">Monthly vouchers will be added automatically on the 1st of each month</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <voucher-card
              v-for="voucher in activeVouchers"
              :key="voucher.id"
              :voucher="voucher"
            />
          </div>
        </div>

        <!-- Expiring Soon Tab Content -->
        <div v-if="activeTab === 'expiring'" class="space-y-4">
          <div v-if="expiringSoon.length === 0" class="text-center py-12">
            <p class="text-zinc-300 text-lg">No vouchers expiring soon</p>
            <p class="text-zinc-500 text-sm mt-2">You have plenty of time to use your vouchers!</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <voucher-card
              v-for="voucher in expiringSoon"
              :key="voucher.id"
              :voucher="voucher"
            />
          </div>
        </div>

        <!-- All Vouchers Tab Content -->
        <div v-if="activeTab === 'all'" class="space-y-4">
          <div v-if="myVouchers.length === 0" class="text-center py-12">
            <p class="text-zinc-300 text-lg">No vouchers available</p>
            <p class="text-zinc-500 text-sm mt-2">Check back later or browse our events</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <voucher-card
              v-for="voucher in myVouchers"
              :key="voucher.id"
              :voucher="voucher"
            />
          </div>
        </div>
      </div>

      <!-- Info Box -->
      <div class="mt-8 bg-zinc-900/80 border border-violet-500/30 rounded-2xl p-6">
        <h3 class="font-semibold text-violet-300 mb-2">How to Use Vouchers</h3>
        <ul class="text-zinc-300 text-sm space-y-1">
          <li>• Monthly vouchers are automatically added on the 1st of each month</li>
          <li>• Each voucher can be used once before expiry</li>
          <li>• Copy the voucher code and paste it during checkout</li>
          <li>• The highest discount will be applied if multiple vouchers are available</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useVoucherStore } from '@/stores/voucher.store'
import VoucherCard from '@/components/VoucherCard.vue'

const voucherStore = useVoucherStore()
const activeTab = ref('active')

const myVouchers = computed(() => voucherStore.myVouchers)
const activeVouchers = computed(() => voucherStore.activeVouchers)
const expiringSoon = computed(() => voucherStore.expiringSoon)
const loading = computed(() => voucherStore.loading)

onMounted(async () => {
  try {
    await voucherStore.fetchMyVouchers()
  } catch (err) {
    console.error('Failed to load vouchers:', err)
  }
})
</script>
