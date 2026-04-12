<template>
  <div class="w-full max-w-md mx-auto py-24">
    <div class="card p-8">
      <div class="text-center mb-8">
        <h1 class="text-2xl font-black text-white mb-1">Đặt lại mật khẩu</h1>
        <p class="text-zinc-500 text-sm">Nhập mã OTP và mật khẩu mới để hoàn tất</p>
      </div>
      <form @submit.prevent="handleResetPassword" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email</label>
          <input
            v-model="email"
            type="email"
            readonly
            class="input-field bg-zinc-800 opacity-80 cursor-not-allowed"
          />
        </div>
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Mã xác thực OTP</label>
          <div class="flex gap-2 justify-center">
            <input
              v-for="(digit, idx) in otpDigits"
              :key="idx"
              ref="el => otpRefs[idx] = el"
              v-model="otpDigits[idx]"
              type="text"
              maxlength="1"
              inputmode="numeric"
              pattern="[0-9]*"
              class="input-field w-12 text-center text-xl font-bold tracking-widest bg-zinc-800"
              :class="errorField === 'otp' ? 'border-red-500 focus:border-red-500' : ''"
              @input="onOtpInput(idx, $event)"
              @keydown.backspace="onOtpBackspace(idx, $event)"
            />
          </div>
        </div>
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Mật khẩu mới</label>
          <input v-model="newPassword" type="password" placeholder="Nhập mật khẩu mới" required class="input-field" :class="errorField === 'newPassword' ? 'border-red-500 focus:border-red-500' : ''" />
        </div>
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Nhập lại mật khẩu mới</label>
          <input v-model="confirmPassword" type="password" placeholder="Nhập lại mật khẩu mới" required class="input-field" :class="errorField === 'confirmPassword' ? 'border-red-500 focus:border-red-500' : ''" />
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
          <span v-else>Đặt lại mật khẩu</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const email = ref('')
const otpDigits = ref([ '', '', '', '', '', '' ])
const otpRefs = []
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')
const errorField = ref('')

onMounted(() => {
  email.value = route.query.email || ''
})

async function handleResetPassword() {
  error.value = ''
  success.value = ''
  errorField.value = ''
  loading.value = true
  try {
    const otp = otpDigits.value.join('').trim()
    if (otp.length !== 6 || !/^[0-9]{6}$/.test(otp)) {
      error.value = 'Vui lòng nhập đủ 6 số OTP.'
      errorField.value = 'otp'
      loading.value = false
      return
    }
    if (!newPassword.value.trim()) {
      error.value = 'Vui lòng nhập mật khẩu mới.'
      errorField.value = 'newPassword'
      loading.value = false
      return
    }
    if (!confirmPassword.value.trim()) {
      error.value = 'Vui lòng nhập lại mật khẩu mới.'
      errorField.value = 'confirmPassword'
      loading.value = false
      return
    }
    if (newPassword.value !== confirmPassword.value) {
      error.value = 'Mật khẩu nhập lại không khớp.'
      errorField.value = 'confirmPassword'
      loading.value = false
      return
    }
    await api.post('/api/auth/reset-password', {
      email: email.value,
      otp,
      newPassword: newPassword.value
    })
    success.value = 'Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.'
    setTimeout(() => {
      router.push({ name: 'Login' })
    }, 1200)
  } catch (e) {
    error.value = e?.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}

function onOtpInput(idx, e) {
  let val = e.target.value.replace(/\D/g, '')
  // Nếu dán nhiều số hoặc nhập nhiều ký tự, chỉ lấy số đầu tiên
  if (val.length > 1) val = val[0]
  otpDigits.value[idx] = val
  // Tự động chuyển sang ô tiếp theo nếu có số
  if (val && idx < otpDigits.value.length - 1) {
    otpRefs[idx + 1]?.focus()
  }
}

function onOtpBackspace(idx, e) {
  if (!otpDigits.value[idx] && idx > 0) {
    otpRefs[idx - 1]?.focus()
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
