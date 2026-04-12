<template>
  <div class="w-full max-w-md mx-auto py-24">
    <div class="card p-8">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-black text-white mb-1">Quên mật khẩu</h1>
        <p class="text-zinc-500 text-sm">Nhập email để nhận mã xác thực đặt lại mật khẩu</p>
      </div>
      <form @submit.prevent="handleSendOtp" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="you@example.com"
            required
            autocomplete="email"
            class="input-field"
            :class="errorField === 'email' ? 'border-red-500 focus:border-red-500' : ''"
          />
        </div>
        <Transition name="fade">
          <div v-if="error" class="mb-3 flex items-center gap-3 bg-red-500/10 border border-red-500/30 text-red-400 rounded-xl px-4 py-3 text-sm">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            {{ error }}
          </div>
        </Transition>
        <Transition name="fade">
          <div v-if="success" class="mb-3 flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 rounded-xl px-4 py-3 text-sm">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10"/><path d="M8 12l2 2 4-4"/>
            </svg>
            {{ success }}
          </div>
        </Transition>
        <button type="submit" class="btn-primary w-full justify-center py-3.5 text-base mt-2 glow-violet" :disabled="loading">
          <span v-if="loading">Đang xử lý...</span>
          <span v-else>Gửi mã xác thực</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'

const email = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')
const errorField = ref('')
const router = useRouter()

async function handleSendOtp() {
  error.value = ''
  success.value = ''
  errorField.value = ''
  loading.value = true
  try {
    if (!email.value.trim()) {
      error.value = 'Vui lòng nhập email.'
      errorField.value = 'email'
      loading.value = false
      return
    }
    await api.post('/api/auth/forgot-password', { email: email.value })
    success.value = 'Mã xác thực đã được gửi về email của bạn.'
    setTimeout(() => {
      router.push({ name: 'ResetPassword', query: { email: email.value } })
    }, 1200)
  } catch (e) {
    error.value = e?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.input-field {
  width: 100%;
  padding: 0.75rem;
  border-radius: 0.75rem;
  border: 1px solid #27272a;
  background: #18181b;
  color: #fff;
  outline: none;
  margin-bottom: 0.5rem;
  font-size: 1rem;
  transition: border 0.2s;
}
.input-field:focus {
  border-color: #a78bfa;
}
.btn-primary {
  background: linear-gradient(90deg, #7c3aed 0%, #a78bfa 100%);
  color: #fff;
  border-radius: 0.75rem;
  padding: 0.75rem;
  font-weight: bold;
  transition: background 0.2s;
  box-shadow: 0 2px 8px 0 #7c3aed33;
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.card {
  background: #18181b;
  border-radius: 1.25rem;
  box-shadow: 0 2px 16px 0 #00000022;
  border: 1px solid #27272a;
}
.glow-violet {
  box-shadow: 0 0 0 2px #a78bfa44;
}
</style>
