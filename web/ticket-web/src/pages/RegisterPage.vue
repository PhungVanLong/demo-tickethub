<template>
  <div class="w-full max-w-md">
    <div class="card p-8">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-2xl font-black text-white mb-1">Create account</h1>
        <p class="text-zinc-500 text-sm">Join TicketHub and start booking events</p>
      </div>

      <!-- Error alert -->
      <Transition name="fade">
        <div
          v-if="auth.error"
          class="mb-5 flex items-center gap-3 bg-red-500/10 border border-red-500/30 text-red-400 rounded-xl px-4 py-3 text-sm"
        >
          <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          {{ auth.error }}
        </div>
      </Transition>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <!-- Full name -->
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Full Name</label>
          <input
            v-model="form.name"
            type="text"
            placeholder="Alex Nguyen"
            required
            autocomplete="name"
            class="input-field"
            :class="errors.name ? 'border-red-500' : ''"
          />
          <p v-if="errors.name" class="text-red-400 text-xs mt-1">{{ errors.name }}</p>
        </div>

        <!-- Email -->
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email</label>
          <input
            v-model="form.email"
            type="email"
            placeholder="you@example.com"
            required
            autocomplete="email"
            class="input-field"
            :class="errors.email ? 'border-red-500' : ''"
          />
          <p v-if="errors.email" class="text-red-400 text-xs mt-1">{{ errors.email }}</p>
        </div>

        <!-- Password -->
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Password</label>
          <div class="relative">
            <input
              v-model="form.password"
              :type="showPw ? 'text' : 'password'"
              placeholder="Min. 8 characters"
              required
              autocomplete="new-password"
              class="input-field pr-10"
              :class="errors.password ? 'border-red-500' : ''"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300"
              @click="showPw = !showPw"
            >
              <svg v-if="!showPw" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
          <!-- Password strength bar -->
          <div class="mt-2 flex gap-1">
            <div
              v-for="i in 4"
              :key="i"
              class="h-1 flex-1 rounded-full transition-colors duration-300"
              :class="passwordStrength >= i ? strengthColor : 'bg-zinc-800'"
            />
          </div>
          <p v-if="errors.password" class="text-red-400 text-xs mt-1">{{ errors.password }}</p>
        </div>

        <!-- Confirm Password -->
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Confirm Password</label>
          <input
            v-model="form.confirmPassword"
            :type="showPw ? 'text' : 'password'"
            placeholder="Repeat password"
            required
            autocomplete="new-password"
            class="input-field"
            :class="errors.confirmPassword ? 'border-red-500' : ''"
          />
          <p v-if="errors.confirmPassword" class="text-red-400 text-xs mt-1">{{ errors.confirmPassword }}</p>
        </div>

        <!-- Terms -->
        <label class="flex items-start gap-3 cursor-pointer">
          <input v-model="form.agree" type="checkbox" class="mt-0.5 rounded border-zinc-700 bg-zinc-800 text-violet-600 focus:ring-violet-600" />
          <span class="text-xs text-zinc-400">
            I agree to the
            <a href="#" class="text-violet-400 hover:underline">Terms of Service</a>
            and
            <a href="#" class="text-violet-400 hover:underline">Privacy Policy</a>
          </span>
        </label>
        <p v-if="errors.agree" class="text-red-400 text-xs -mt-2">{{ errors.agree }}</p>

        <!-- Submit -->
        <button
          type="submit"
          class="btn-primary w-full justify-center py-3.5 text-base glow-violet"
          :disabled="auth.loading"
          :class="auth.loading ? 'opacity-70 cursor-wait' : ''"
        >
          <svg v-if="auth.loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          {{ auth.loading ? 'Creating account…' : 'Create Account' }}
        </button>
      </form>
    </div>

    <p class="text-center text-sm text-zinc-500 mt-6">
      Already have an account?
      <RouterLink to="/login" class="text-violet-400 hover:text-violet-300 font-semibold transition-colors">
        Sign In
      </RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const auth   = useAuthStore()
const router = useRouter()

const showPw = ref(false)
const form   = reactive({
  name:            '',
  email:           '',
  password:        '',
  confirmPassword: '',
  agree:           false,
})
const errors = reactive({})

const passwordStrength = computed(() => {
  const p = form.password
  if (!p) return 0
  let score = 0
  if (p.length >= 8)  score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthColor = computed(() => {
  if (passwordStrength.value <= 1) return 'bg-red-500'
  if (passwordStrength.value === 2) return 'bg-amber-500'
  if (passwordStrength.value === 3) return 'bg-yellow-400'
  return 'bg-emerald-500'
})

async function handleRegister() {
  Object.keys(errors).forEach((k) => delete errors[k])
  let valid = true
  if (!form.name.trim()) { errors.name = 'Full name is required'; valid = false }
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRe.test(form.email)) { errors.email = 'Enter a valid email'; valid = false }
  if (form.password.length < 8)  { errors.password = 'Minimum 8 characters'; valid = false }
  if (form.password !== form.confirmPassword) { errors.confirmPassword = 'Passwords do not match'; valid = false }
  if (!form.agree) { errors.agree = 'You must accept the terms'; valid = false }
  if (!valid) return

  const ok = await auth.register({
    name:     form.name.trim(),
    fullName: form.name.trim(),
    email:    form.email.trim(),
    password: form.password,
  })
  if (ok) router.push('/')
}
</script>
