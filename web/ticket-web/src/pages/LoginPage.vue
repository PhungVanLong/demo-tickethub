<template>
  <div class="w-full max-w-md">
    <!-- Card -->
    <div class="card p-8">
      <!-- Header -->
      <div class="text-center mb-8">
        <h1 class="text-2xl font-black text-white mb-1">Welcome back</h1>
        <p class="text-zinc-500 text-sm">Sign in to your TicketHub account</p>
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

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="you@example.com"
            required
            autocomplete="email"
            class="input-field"
            :class="errors.email ? 'border-red-500 focus:border-red-500' : ''"
          />
          <p v-if="errors.email" class="text-red-400 text-xs mt-1">{{ errors.email }}</p>
        </div>

        <div>
          <div class="flex items-center justify-between mb-1.5">
            <label class="text-xs font-semibold text-zinc-400 uppercase tracking-wide">Password</label>
            <a href="#" class="text-xs text-violet-400 hover:text-violet-300 transition-colors">Forgot password?</a>
          </div>
          <div class="relative">
            <input
              v-model="password"
              :type="showPw ? 'text' : 'password'"
              placeholder="••••••••"
              required
              autocomplete="current-password"
              class="input-field pr-10"
              :class="errors.password ? 'border-red-500 focus:border-red-500' : ''"
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
          <p v-if="errors.password" class="text-red-400 text-xs mt-1">{{ errors.password }}</p>
        </div>

        <button
          type="submit"
          class="btn-primary w-full justify-center py-3.5 text-base mt-2 glow-violet"
          :disabled="auth.loading"
          :class="auth.loading ? 'opacity-70 cursor-wait' : ''"
        >
          <svg v-if="auth.loading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          {{ auth.loading ? 'Signing in…' : 'Sign In' }}
        </button>
      </form>

      <!-- Divider -->
      <div class="flex items-center gap-3 my-6">
        <div class="flex-1 h-px bg-zinc-800" />
        <span class="text-xs text-zinc-600">or continue with</span>
        <div class="flex-1 h-px bg-zinc-800" />
      </div>

      <!-- Social logins -->
      <div class="grid grid-cols-1 gap-3">
        <div class="flex justify-center">
          <div ref="googleButtonRef" class="min-h-[44px]" />
        </div>
        <p v-if="!GOOGLE_CLIENT_ID" class="text-xs text-zinc-500 text-center">
          Google Sign-In is not configured yet. Add VITE_GOOGLE_CLIENT_ID in env.
        </p>
      </div>
    </div>

    <!-- Sign up link -->
    <p class="text-center text-sm text-zinc-500 mt-6">
      Don't have an account?
      <RouterLink to="/register" class="text-violet-400 hover:text-violet-300 font-semibold transition-colors">
        Sign Up Free
      </RouterLink>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const auth   = useAuthStore()
const router = useRouter()
const route  = useRoute()

const email   = ref('')
const password = ref('')
const showPw  = ref(false)
const errors  = reactive({})
const googleButtonRef = ref(null)

const GOOGLE_CLIENT_ID = String(import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim()

function loadGoogleScript() {
  return new Promise((resolve, reject) => {
    if (window.google?.accounts?.id) {
      resolve()
      return
    }

    const existing = document.querySelector('script[data-google-identity="true"]')
    if (existing) {
      existing.addEventListener('load', () => resolve(), { once: true })
      existing.addEventListener('error', () => reject(new Error('Failed to load Google SDK')), { once: true })
      return
    }

    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.dataset.googleIdentity = 'true'
    script.onload = () => resolve()
    script.onerror = () => reject(new Error('Failed to load Google SDK'))
    document.head.appendChild(script)
  })
}

async function handleGoogleCredential(credentialResponse) {
  const idToken = String(credentialResponse?.credential || '').trim()
  if (!idToken) {
    auth.error = 'Google sign-in did not return a valid token.'
    return
  }

  const ok = await auth.loginWithGoogleIdToken(idToken)
  if (ok) {
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/')
  }
}

async function setupGoogleSignIn() {
  if (!GOOGLE_CLIENT_ID || !googleButtonRef.value) {
    return
  }

  auth.error = null
  try {
    await loadGoogleScript()

    window.google.accounts.id.initialize({
      client_id: GOOGLE_CLIENT_ID,
      callback: handleGoogleCredential,
      auto_select: false,
      cancel_on_tap_outside: true,
    })

    googleButtonRef.value.innerHTML = ''
    window.google.accounts.id.renderButton(googleButtonRef.value, {
      theme: 'outline',
      size: 'large',
      shape: 'pill',
      text: 'continue_with',
      width: 320,
    })
  } catch (e) {
    auth.error = e?.message || 'Google sign-in is temporarily unavailable.'
  }
}

onMounted(() => {
  setupGoogleSignIn()
})

async function handleLogin() {
  // Clear previous errors
  Object.keys(errors).forEach((k) => delete errors[k])
  let valid = true
  if (!email.value.trim()) { errors.email = 'Email is required'; valid = false }
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (email.value && !emailRe.test(email.value)) { errors.email = 'Enter a valid email'; valid = false }
  if (!password.value) { errors.password = 'Password is required'; valid = false }
  if (!valid) return

  const ok = await auth.login(email.value.trim(), password.value)
  if (ok) {
    const redirect = route.query.redirect
    router.push(typeof redirect === 'string' ? redirect : '/')
  }
}
</script>
