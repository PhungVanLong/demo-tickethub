<template>
  <div>
  <!-- Redirect if cart is empty -->
  <div v-if="!cart.event" class="min-h-screen flex items-center justify-center">
    <div class="text-center">
      <p class="text-zinc-400 text-lg mb-4">Your cart is empty.</p>
      <RouterLink to="/" class="btn-primary">Browse Events</RouterLink>
    </div>
  </div>

    <!-- Checkout form -->
    <div v-if="cart.event" class="min-h-screen py-24 animate-fade-in">
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">

        <h1 class="text-3xl font-black text-white mb-2">Checkout</h1>
        <p class="text-zinc-500 mb-8">Complete your purchase securely</p>

        <!-- Steps -->
        <div class="flex items-center gap-0 mb-10">
          <div
            v-for="(step, i) in steps"
            :key="step"
            class="flex items-center"
          >
            <div class="flex items-center gap-2">
              <div
                class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
                :class="i < currentStep ? 'bg-emerald-500 text-white' : i === currentStep ? 'bg-violet-600 text-white' : 'bg-zinc-800 text-zinc-500'"
              >
                <svg v-if="i < currentStep" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
                <span v-else>{{ i + 1 }}</span>
              </div>
              <span
                class="text-sm font-medium hidden sm:block"
                :class="i === currentStep ? 'text-white' : i < currentStep ? 'text-emerald-400' : 'text-zinc-600'"
              >{{ step }}</span>
            </div>
            <div v-if="i < steps.length - 1" class="w-12 sm:w-20 h-px mx-2 sm:mx-4" :class="i < currentStep ? 'bg-emerald-500' : 'bg-zinc-700'" />
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <!-- Form area -->
          <div class="lg:col-span-2">

            <!-- Step 0: Contact -->
            <Transition name="step-fade" mode="out-in">
              <div v-if="currentStep === 0" key="step0" class="space-y-6">
                <div class="card p-6">
                  <h2 class="font-bold text-white text-lg mb-5">Contact Information</h2>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">First Name</label>
                      <input v-model="form.firstName" type="text" placeholder="Alex" class="input-field" />
                      <p v-if="errors.firstName" class="text-red-400 text-xs mt-1">{{ errors.firstName }}</p>
                    </div>
                    <div>
                      <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Last Name</label>
                      <input v-model="form.lastName" type="text" placeholder="Nguyen" class="input-field" />
                      <p v-if="errors.lastName" class="text-red-400 text-xs mt-1">{{ errors.lastName }}</p>
                    </div>
                    <div class="sm:col-span-2">
                      <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Email Address</label>
                      <input v-model="form.email" type="email" placeholder="alex@email.com" class="input-field" />
                      <p v-if="errors.email" class="text-red-400 text-xs mt-1">{{ errors.email }}</p>
                    </div>
                    <div class="sm:col-span-2">
                      <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Phone Number</label>
                      <input v-model="form.phone" type="tel" placeholder="+84 912 345 678" class="input-field" />
                    </div>
                  </div>
                </div>
                <button class="btn-primary w-full justify-center py-4 text-base" @click="nextStep">
                  Continue to Payment
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
                </button>
              </div>

              <!-- Step 1: Payment -->
              <div v-else-if="currentStep === 1" key="step1" class="space-y-6">
                <div class="card p-6">
                  <h2 class="font-bold text-white text-lg mb-5">Payment Method</h2>

                  <!-- Payment options -->
                  <div class="grid grid-cols-1 gap-3 mb-6">
                    <label
                      v-for="method in paymentMethods"
                      :key="method.id"
                      class="flex items-center gap-4 p-4 rounded-xl border cursor-pointer transition-all"
                      :class="form.paymentMethod === method.id ? 'border-violet-500 bg-violet-500/10' : 'border-zinc-700 hover:border-zinc-600'"
                    >
                      <input type="radio" v-model="form.paymentMethod" :value="method.id" class="hidden" />
                      <div
                        class="w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition-colors"
                        :class="form.paymentMethod === method.id ? 'border-violet-500' : 'border-zinc-600'"
                      >
                        <div v-if="form.paymentMethod === method.id" class="w-2.5 h-2.5 rounded-full bg-violet-500" />
                      </div>
                      <div class="flex items-center gap-3 flex-1">
                        <span class="text-2xl">{{ method.icon }}</span>
                        <div>
                          <p class="font-semibold text-white text-sm">{{ method.name }}</p>
                          <p class="text-xs text-zinc-500">{{ method.desc }}</p>
                        </div>
                      </div>
                    </label>
                  </div>

                  <!-- Card fields (visible only when card selected) -->
                  <Transition name="step-fade">
                    <div v-if="form.paymentMethod === 'card'" class="space-y-4 pt-4 border-t border-zinc-800">
                      <div>
                        <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Card Number</label>
                        <input
                          v-model="form.cardNumber"
                          type="text"
                          placeholder="1234 5678 9012 3456"
                          maxlength="19"
                          class="input-field font-mono"
                          @input="formatCardNumber"
                        />
                        <p v-if="errors.cardNumber" class="text-red-400 text-xs mt-1">{{ errors.cardNumber }}</p>
                      </div>
                      <div class="grid grid-cols-2 gap-4">
                        <div>
                          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Expiry</label>
                          <input v-model="form.expiry" type="text" placeholder="MM/YY" maxlength="5" class="input-field font-mono" @input="formatExpiry" />
                          <p v-if="errors.expiry" class="text-red-400 text-xs mt-1">{{ errors.expiry }}</p>
                        </div>
                        <div>
                          <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">CVV</label>
                          <input v-model="form.cvv" type="password" placeholder="•••" maxlength="4" class="input-field font-mono" />
                          <p v-if="errors.cvv" class="text-red-400 text-xs mt-1">{{ errors.cvv }}</p>
                        </div>
                      </div>
                      <div>
                        <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Name on Card</label>
                        <input v-model="form.cardName" type="text" placeholder="ALEX NGUYEN" class="input-field uppercase" />
                      </div>
                    </div>
                  </Transition>
                </div>

                <div class="flex gap-3">
                  <button class="btn-secondary flex-1 justify-center py-4" @click="currentStep--">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
                    Back
                  </button>
                  <button
                    class="btn-primary flex-1 justify-center py-4 text-base"
                    :class="processing ? 'opacity-70 cursor-wait' : ''"
                    :disabled="processing"
                    @click="placeOrder"
                  >
                    <svg v-if="processing" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
                    </svg>
                    {{ processing ? 'Processing…' : 'Place Order' }}
                    <svg v-if="!processing" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  </button>
                </div>
                <p v-if="errors.global" class="text-red-400 text-sm text-center mt-2">{{ errors.global }}</p>
              </div>
            </Transition>
          </div>

          <!-- Summary sidebar -->
          <div class="lg:sticky lg:top-24 self-start">
            <CheckoutSummary />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter }           from 'vue-router'
import CheckoutSummary         from '@/components/CheckoutSummary.vue'
import { useBookingStore }     from '@/stores/booking.store'
import { useAuthStore }        from '@/stores/auth.store'
import { extractApiError }     from '@/utils/apiError'

const cart    = useBookingStore()
const auth    = useAuthStore()
const router  = useRouter()

const steps       = ['Contact', 'Payment', 'Confirm']
const currentStep = ref(0)
const processing  = ref(false)

function splitFullName(fullName = '') {
  const nameParts = String(fullName || '').trim().split(/\s+/).filter(Boolean)
  return {
    firstName: nameParts[0] ?? '',
    lastName: nameParts.slice(1).join(' ') ?? '',
  }
}

const nameInfo = splitFullName(auth.user?.name)
const form = reactive({
  firstName:     nameInfo.firstName,
  lastName:      nameInfo.lastName,
  email:         auth.user?.email ?? '',
  phone:         '',
  paymentMethod: 'card',
  cardNumber:    '',
  expiry:        '',
  cvv:           '',
  cardName:      '',
})

const errors = reactive({})

const paymentMethods = [
  { id: 'card',    icon: '💳', name: 'Credit / Debit Card', desc: 'Visa, Mastercard, JCB' },
  { id: 'momo',    icon: '💜', name: 'MoMo Wallet',         desc: 'Pay with MoMo e-wallet' },
  { id: 'zalopay', icon: '💙', name: 'ZaloPay',             desc: 'Pay with ZaloPay' },
  { id: 'bank',    icon: '🏦', name: 'Bank Transfer',        desc: 'Direct bank transfer' },
]

function validateContact() {
  Object.keys(errors).forEach((k) => delete errors[k])
  let valid = true
  if (!form.firstName.trim()) { errors.firstName = 'Required'; valid = false }
  if (!form.lastName.trim())  { errors.lastName  = 'Required'; valid = false }
  const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRe.test(form.email)) { errors.email = 'Valid email required'; valid = false }
  return valid
}

function validatePayment() {
  Object.keys(errors).forEach((k) => delete errors[k])
  if (form.paymentMethod !== 'card') return true
  let valid = true
  if (form.cardNumber.replace(/\s/g, '').length < 16) { errors.cardNumber = 'Enter full card number'; valid = false }
  const expiryValidation = validateExpiry(form.expiry)
  if (!expiryValidation.valid)                          { errors.expiry     = expiryValidation.message; valid = false }
  if (form.cvv.length < 3)                             { errors.cvv        = 'CVV required'; valid = false }
  return valid
}

function nextStep() {
  if (currentStep.value === 0 && !validateContact()) return
  currentStep.value++
}

function formatCardNumber(e) {
  let v = e.target.value.replace(/\D/g, '').slice(0, 16)
  form.cardNumber = v.replace(/(.{4})/g, '$1 ').trim()
}

function formatExpiry(e) {
  let v = e.target.value.replace(/\D/g, '').slice(0, 4)
  if (v.length >= 1) {
    const first = Number(v[0])
    if (first > 1) v = `0${v}`
  }
  if (v.length > 2) v = v.slice(0, 2) + '/' + v.slice(2)
  form.expiry = v
}

function validateExpiry(value) {
  if (!/^\d{2}\/\d{2}$/.test(value)) {
    return { valid: false, message: 'MM/YY format required' }
  }

  const [monthStr, yearStr] = value.split('/')
  const month = Number(monthStr)
  const year = Number(yearStr)

  if (!Number.isInteger(month) || month < 1 || month > 12) {
    return { valid: false, message: 'Month must be 01-12' }
  }

  const now = new Date()
  const currentMonth = now.getMonth() + 1
  const currentYear = now.getFullYear() % 100

  if (year < currentYear || (year === currentYear && month < currentMonth)) {
    return { valid: false, message: 'Card has expired' }
  }

  return { valid: true, message: '' }
}

async function placeOrder() {
  if (!validatePayment()) return
  processing.value = true

  try {
    // 0. Set purchaser info BEFORE creating order
    cart.setPurchaserInfo(form)
    
    // 1. Create the order
    const order = await cart.createOrder()
    if (!order) throw new Error(cart.error || 'Order creation failed')

    // 2. Create payment intent
    const orderId = order.orderId ?? order.id
    const pi = await cart.createPaymentIntent(orderId, form.paymentMethod)
    if (!pi && cart.error) throw new Error(cart.error)

    const paymentCode = pi?.paymentCode ?? pi?.paymentId ?? `sim-${Date.now()}`
    const payUrl = String(pi?.payUrl || '').trim()
    const isFakeGatewayUrl = /\/api\/checkout\/payments\/fake-gateway/i.test(payUrl)

    // 3. Prefer real/external payment URLs. For backend fake-gateway endpoints,
    // keep flow in-app to avoid 403 pages caused by missing Bearer header on browser navigation.
    cart.clear()
    if (payUrl && !isFakeGatewayUrl) {
      window.location.href = payUrl
      return
    }

    router.push({ path: '/payment-result', query: { paymentCode, orderId } })
  } catch (e) {
    errors.global = extractApiError(e, cart.error || 'Payment failed. Please try again.').message
  } finally {
    processing.value = false
  }
}
</script>

<style scoped>
.step-fade-enter-active, .step-fade-leave-active { transition: all 0.2s ease; }
.step-fade-enter-from, .step-fade-leave-to       { opacity: 0; transform: translateX(10px); }
.fade-enter-active, .fade-leave-active           { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to                 { opacity: 0; }
</style>
