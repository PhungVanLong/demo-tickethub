<template>
  <div class="rounded-2xl bg-white p-4 shadow-lg shadow-black/30">
    <div v-if="loading" class="flex aspect-square items-center justify-center text-zinc-500">
      <div class="h-8 w-8 rounded-full border-4 border-zinc-300 border-t-zinc-700 animate-spin" />
    </div>
    <img
      v-else-if="dataUrl"
      :src="dataUrl"
      alt="Ticket QR code"
      class="aspect-square w-full rounded-xl object-contain"
    />
    <div v-else class="flex aspect-square items-center justify-center rounded-xl border border-dashed border-zinc-300 text-center text-xs text-zinc-500">
      QR unavailable
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { createTicketQrDataUrl } from '@/utils/ticketQr'

const props = defineProps({
  data: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 240,
  },
})

const dataUrl = ref('')
const loading = ref(false)
let renderVersion = 0

watch(
  () => [props.data, props.size],
  async ([data, size]) => {
    const currentVersion = ++renderVersion

    if (!data) {
      dataUrl.value = ''
      loading.value = false
      return
    }

    loading.value = true
    try {
      const nextUrl = await createTicketQrDataUrl(data, { width: size })
      if (currentVersion === renderVersion) {
        dataUrl.value = nextUrl
      }
    } catch {
      if (currentVersion === renderVersion) {
        dataUrl.value = ''
      }
    } finally {
      if (currentVersion === renderVersion) {
        loading.value = false
      }
    }
  },
  { immediate: true }
)
</script>