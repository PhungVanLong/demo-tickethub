<template>
  <div class="min-h-screen py-24 px-4">
    <div class="max-w-3xl mx-auto">

      <!-- Header -->
      <div class="flex items-center gap-4 mb-8">
        <RouterLink to="/dashboard" class="btn-ghost p-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
        </RouterLink>
        <div>
          <h1 class="text-2xl font-black text-white">{{ isEditing ? 'Edit Event' : 'Create New Event' }}</h1>
          <p class="text-zinc-500 text-sm mt-0.5">{{ isEditing ? 'Update event details' : 'Fill in details, then add seat map info for review/publishing' }}</p>
        </div>
      </div>

      <!-- Step indicator -->
      <div class="flex items-center gap-0 mb-8">
        <div v-for="(step, i) in steps" :key="step" class="flex items-center">
          <div class="flex items-center gap-2">
            <div
              class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors"
              :class="i < currentStep ? 'bg-emerald-500 text-white' : i === currentStep ? 'bg-violet-600 text-white' : 'bg-zinc-800 text-zinc-500'"
            >
              <svg v-if="i < currentStep" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><polyline points="20 6 9 17 4 12"/></svg>
              <span v-else>{{ i + 1 }}</span>
            </div>
            <span class="text-sm font-medium hidden sm:block" :class="i === currentStep ? 'text-white' : i < currentStep ? 'text-emerald-400' : 'text-zinc-600'">{{ step }}</span>
          </div>
          <div v-if="i < steps.length - 1" class="w-10 sm:w-16 h-px mx-2 sm:mx-3" :class="i < currentStep ? 'bg-emerald-500' : 'bg-zinc-700'" />
        </div>
      </div>


      <!-- Error -->
      <div v-if="organizer.formError" class="bg-red-500/10 border border-red-500/20 text-red-400 rounded-xl px-4 py-3 text-sm mb-6">
        <div v-if="organizer.formError && typeof organizer.formError === 'string' && !Object.keys(organizer.formFieldErrors || {}).length">
          {{ organizer.formError }}
        </div>
        <div v-else-if="Object.keys(organizer.formFieldErrors || {}).length">
          <ul class="list-disc pl-5">
            <li v-for="(msg, field) in organizer.formFieldErrors" :key="field">{{ msg }}</li>
          </ul>
        </div>
      </div>

      <div v-if="organizer.formWarning" class="bg-amber-500/10 border border-amber-500/30 text-amber-300 rounded-xl px-4 py-3 text-sm mb-6">
        {{ organizer.formWarning }}
      </div>

      <!-- STEP 0: Event Details -->
      <Transition name="step-fade" mode="out-in">
        <div v-if="currentStep === 0" key="step0" class="card p-6 space-y-5">
          <h2 class="font-bold text-white text-lg">Event Details</h2>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Event Title *</label>
            <input v-model="eventForm.title" type="text" placeholder="e.g. Coldplay Music of the Spheres" class="input-field" :class="ev('title') ? 'border-red-500' : ''" />
            <p v-if="ev('title')" class="text-red-400 text-xs mt-1">{{ ev('title') }}</p>
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Description</label>
            <textarea v-model="eventForm.description" rows="4" placeholder="Describe your event…" class="input-field resize-none" />
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Start Date & Time *</label>
              <input v-model="eventForm.startTime" type="datetime-local" class="input-field" :class="ev('startTime') ? 'border-red-500' : ''" />
              <p v-if="ev('startTime')" class="text-red-400 text-xs mt-1">{{ ev('startTime') }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">End Date & Time *</label>
              <input v-model="eventForm.endTime" type="datetime-local" class="input-field" :class="ev('endTime') ? 'border-red-500' : ''" />
              <p v-if="ev('endTime')" class="text-red-400 text-xs mt-1">{{ ev('endTime') }}</p>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Venue *</label>
              <input v-model="eventForm.venue" type="text" placeholder="Venue name" class="input-field" />
              <p v-if="ev('venue')" class="text-red-400 text-xs mt-1">{{ ev('venue') }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">City *</label>
              <input v-model="eventForm.city" type="text" placeholder="Ho Chi Minh City" class="input-field" />
              <p v-if="ev('city')" class="text-red-400 text-xs mt-1">{{ ev('city') }}</p>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Country</label>
              <input v-model="eventForm.country" type="text" placeholder="Vietnam" class="input-field" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Tags (comma separated)</label>
              <input v-model="eventForm.tagsText" type="text" placeholder="Pop, Live" class="input-field" />
            </div>
          </div>


          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Default Price (optional)</label>
              <input v-model.number="eventForm.defaultPrice" type="number" min="0" step="1000" placeholder="500000" class="input-field" />
              <p class="text-xs text-zinc-500 mt-1">Nếu > 0 và có Default Tier Quantity, backend sẽ tự tạo tier mặc định.</p>
              <p v-if="eventForm.defaultPrice > 0 && (!eventForm.defaultTierQuantity || eventForm.defaultTierQuantity < 1)" class="text-red-400 text-xs mt-1">Cần nhập số lượng tier mặc định &ge; 1</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Default Tier Quantity</label>
              <input v-model.number="eventForm.defaultTierQuantity" type="number" min="1" step="1" placeholder="200" class="input-field" :disabled="!eventForm.defaultPrice || eventForm.defaultPrice <= 0" />
            </div>
          </div>

          <label class="inline-flex items-center gap-2 text-sm text-zinc-300">
            <input v-model="eventForm.featured" type="checkbox" class="rounded border-zinc-600 bg-zinc-900" />
            Mark as featured
          </label>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Location Coordinates</label>
            <input v-model="eventForm.locationCoords" type="text" placeholder="10.762622,106.660172" class="input-field" />
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Banner Image URL</label>
            <input v-model="eventForm.bannerUrl" type="url" placeholder="https://…" class="input-field" />
            <div v-if="eventForm.bannerUrl" class="mt-2 rounded-xl overflow-hidden border border-zinc-700 bg-zinc-900">
              <img
                :src="eventForm.bannerUrl"
                alt="Banner preview"
                class="w-full max-h-48 object-cover"
                @error="$event.target.style.display='none'"
                @load="$event.target.style.display='block'"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Thumbnail Image URL</label>
            <input v-model="eventForm.imageUrl" type="url" placeholder="https://…" class="input-field" />
          </div>

          <div class="flex justify-end pt-2">
            <button v-if="!isEditing" class="btn-primary" @click="nextStep(0)" :disabled="eventForm.defaultPrice > 0 && (!eventForm.defaultTierQuantity || eventForm.defaultTierQuantity < 1)">
              Next: Seat Map
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
            </button>
            <button
              v-else
              class="btn-primary"
              :class="organizer.formLoading ? 'opacity-70 cursor-wait' : ''"
              :disabled="organizer.formLoading"
              @click="submit"
            >
              <svg v-if="organizer.formLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              {{ isEditing ? 'Save Changes' : 'Create Event' }}
            </button>
          </div>
        </div>

        <!-- STEP 1: Seat Map -->
        <div v-else-if="!isEditing && currentStep === 1" key="step1" class="card p-6 space-y-5">
          <div class="flex items-center justify-between">
            <h2 class="font-bold text-white text-lg">Seat Map</h2>
            <span class="text-xs text-zinc-500">Optional — skip if not needed</span>
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Layout Name</label>
            <input v-model="seatMapForm.name" type="text" placeholder="Main Hall" class="input-field" />
          </div>

          <div v-if="canManageSeatMap" class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Total Rows</label>
              <input v-model.number="seatMapForm.totalRows" type="number" min="1" placeholder="10" class="input-field" />
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seats per Row</label>
              <input v-model.number="seatMapForm.totalCols" type="number" min="1" placeholder="20" class="input-field" />
            </div>
          </div>

          <p v-if="canManageSeatMap" class="text-xs text-zinc-500">
            Total seat capacity: <span class="text-zinc-300 font-semibold">{{ totalSeatCapacity }}</span>
          </p>

          <div v-if="canManageSeatMap">
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seat Map Image File</label>
            <input type="file" accept="image/*" class="input-field" @change="onSeatMapFileChange" />
            <p class="text-xs text-zinc-500 mt-1">Upload image directly to backend seatmap API.</p>
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seat Map API Image URL (optional)</label>
            <input v-model="seatMapForm.apiImageUrl" type="url" placeholder="https://…" class="input-field" />
          </div>

          <div>
            <div v-if="seatMapPreviewUrl" class="mt-2 rounded-xl overflow-hidden border border-zinc-700 bg-zinc-900">
              <img
                :src="seatMapPreviewUrl"
                alt="Seat map preview"
                class="w-full max-h-48 object-cover"
                @error="$event.target.style.display='none'"
                @load="$event.target.style.display='block'"
              />
            </div>
          </div>

          <div class="flex justify-between pt-2">
            <button class="btn-secondary" @click="currentStep--">Back</button>
            <div class="flex gap-3">
              <button class="btn-ghost text-sm" @click="skipSeatMap">Skip</button>
              <button class="btn-primary" @click="nextStep(1)">
                Next: Ticket Tiers
                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
              </button>
            </div>
          </div>
        </div>

        <!-- STEP 2: Ticket Tiers -->
        <div v-else-if="!isEditing && canManageSeatMap && currentStep === 2" key="step2" class="space-y-4">
          <div v-if="tiers.length === 0" class="bg-amber-500/10 border border-amber-500/30 text-amber-300 rounded-xl px-4 py-3 text-sm mb-4">
            Sự kiện chưa có hạng vé nào. Bạn cần tạo ít nhất 1 hạng vé để có thể bán vé.
            <button class="ml-2 btn-primary btn-xs" @click="addTier">Tạo hạng vé</button>
          </div>
          <div class="flex items-center justify-between">
            <h2 class="font-bold text-white text-lg">Ticket Tiers</h2>
            <button class="btn-ghost text-sm" @click="addTier">
              <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M12 5v14M5 12h14"/></svg>
              Add Tier
            </button>
          </div>

          <div
            v-for="(tier, i) in tiers"
            :key="i"
            class="card p-5 space-y-4"
          >
            <div class="flex items-center justify-between">
              <h3 class="font-semibold text-white text-sm">Tier {{ i + 1 }}</h3>
              <button
                v-if="tiers.length > 1"
                class="text-red-400 hover:text-red-300 transition-colors"
                @click="tiers.splice(i, 1)"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></svg>
              </button>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Tier Name *</label>
                <input v-model="tier.name" type="text" placeholder="e.g. VIP, General" class="input-field" />
                <p v-if="!tier.name" class="text-red-400 text-xs mt-1">Bắt buộc</p>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Type</label>
                <select v-model="tier.tierType" class="input-field">
                  <option value="GENERAL">GENERAL</option>
                  <option value="VIP">VIP</option>
                  <option value="VVIP">VVIP</option>
                  <option value="STANDING">STANDING</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Price (VND) *</label>
                <input v-model.number="tier.price" type="number" min="1" placeholder="0" class="input-field" />
                <p v-if="!tier.price || tier.price <= 0" class="text-red-400 text-xs mt-1">Giá phải > 0</p>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Total Quantity *</label>
                <input v-model.number="tier.quantityTotal" type="number" min="1" placeholder="100" class="input-field" />
                <p v-if="!tier.quantityTotal || tier.quantityTotal <= 0" class="text-red-400 text-xs mt-1">Số lượng phải > 0</p>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Sale Start</label>
                <input v-model="tier.saleStart" type="datetime-local" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Sale End</label>
                <input v-model="tier.saleEnd" type="datetime-local" class="input-field" />
              </div>
            </div>
          </div>

          <div class="flex justify-between pt-2">
            <button class="btn-secondary" @click="currentStep--">Back</button>
            <button
              class="btn-primary"
              :class="organizer.formLoading ? 'opacity-70 cursor-wait' : ''"
              :disabled="organizer.formLoading || tiers.some(t => !t.name || !t.price || t.price <= 0 || !t.quantityTotal || t.quantityTotal <= 0)"
              @click="submit"
            >
              <svg v-if="organizer.formLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              {{ isEditing ? 'Save Changes' : 'Publish Event' }}
            </button>
          </div>
        </div>
      </Transition>

      <div v-if="isEditing" class="mt-10">
        <OrganizerVoucherManagement :fixed-event-id="eventId" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore }        from '@/stores/auth.store'
import { useOrganizerStore }   from '@/stores/organizer.store'
import { embedSeatMapImageInDescription } from '@/utils/seatMapImage'
import OrganizerVoucherManagement from '@/components/OrganizerVoucherManagement.vue'

const router    = useRouter()
const route     = useRoute()
const auth      = useAuthStore()
const organizer = useOrganizerStore()

const eventId   = route.params.id
const isEditing = !!eventId

const canManageSeatMap = computed(() => {
  const role = String(auth.user?.role || '').toUpperCase()
  return ['ADMIN', 'ORGANIZER', 'CUSTOMER', 'MEMBER'].includes(role)
})
const steps = computed(() => {
  if (isEditing) return ['Event Details']
  return ['Event Details', 'Seat Map', 'Ticket Tiers']
})
const currentStep = ref(0)
const formErrors  = reactive({})
const includeSeatMap = ref(true)

const eventForm = reactive({
  title:       '',
  description: '',
  venue:       '',
  city:        '',
  country:     'Vietnam',
  locationCoords: '',
  startTime:   '',
  endTime:     '',
  bannerUrl:   '',
  imageUrl:    '',
  featured:    false,
  tagsText:    '',
  defaultPrice: null,
  defaultTierQuantity: 1,
})

const seatMapForm = reactive({
  name:       'Main Hall',
  totalRows:  10,
  totalCols:  20,
  apiImageUrl: '',
  requestImageUrl: '',
  file:       null,
  layoutJson: '',
})

const filePreviewUrl = ref('')
const seatMapPreviewUrl = computed(() => {
  if (filePreviewUrl.value) return filePreviewUrl.value
  return seatMapForm.apiImageUrl
})

function onSeatMapFileChange(event) {
  const file = event?.target?.files?.[0] || null
  seatMapForm.file = file
}

watch(() => seatMapForm.file, (newFile, oldFile) => {
  if (filePreviewUrl.value) {
    URL.revokeObjectURL(filePreviewUrl.value)
    filePreviewUrl.value = ''
  }
  if (newFile) {
    filePreviewUrl.value = URL.createObjectURL(newFile)
  }
})

onUnmounted(() => {
  if (filePreviewUrl.value) {
    URL.revokeObjectURL(filePreviewUrl.value)
  }
})

const tiers = ref([{
  name:          'General',
  tierType:      'GENERAL',
  price:         0,
  quantityTotal: 100,
  colorCode:     '#6366f1',
  saleStart:     '',
  saleEnd:       '',
}])

const totalSeatCapacity = computed(() => {
  const rows = Number(seatMapForm.totalRows || 0)
  const cols = Number(seatMapForm.totalCols || 0)
  if (!Number.isFinite(rows) || !Number.isFinite(cols)) return 0
  return Math.max(0, rows) * Math.max(0, cols)
})

watch([() => seatMapForm.totalRows, () => seatMapForm.totalCols], () => {
  if (!canManageSeatMap.value || isEditing) return

  // Auto-fill quantity when using the default single-tier setup.
  if (tiers.value.length === 1) {
    tiers.value[0].quantityTotal = totalSeatCapacity.value
  }
}, { immediate: true })

// Pre-fill for editing
onMounted(async () => {
  if (isEditing) {
    await organizer.fetchMyEvents(auth.user?.id)
    const ev = organizer.myEvents.find((e) => String(e.id) === String(eventId))
    if (ev) {
      Object.assign(eventForm, {
        title:       ev.title,
        description: ev.description,
        venue:       ev.venue,
        city:        ev.city,
        country:     ev.country || 'Vietnam',
        locationCoords: ev.locationCoords || '',
        startTime:   ev.date || '',
        endTime:     '',
        bannerUrl:   ev.banner || ev.image || '',
        imageUrl:    ev.image || ev.banner || '',
        featured:    Boolean(ev.featured),
        tagsText:    Array.isArray(ev.tags) ? ev.tags.join(', ') : '',
        defaultPrice: null,
        defaultTierQuantity: 1,
      })
    }
  }
})

function ev(field) {
  return formErrors[field] || organizer.formFieldErrors?.[field] || ''
}

function nextStep(step) {
  Object.keys(formErrors).forEach((k) => delete formErrors[k])
  organizer.formFieldErrors = {}
  if (step === 0) {
    let ok = true
    if (!eventForm.title.trim())     { formErrors.title = 'Required'; ok = false }
    if (!eventForm.startTime)        { formErrors.startTime = 'Required'; ok = false }
    if (!eventForm.endTime)          { formErrors.endTime = 'Required'; ok = false }
    if (!eventForm.venue.trim())     { formErrors.venue = 'Required'; ok = false }
    if (!eventForm.city.trim())      { formErrors.city = 'Required'; ok = false }
    if (!ok) return
  }
  if (currentStep.value < steps.value.length - 1) {
    currentStep.value++
  }
}

function skipSeatMap() {
  includeSeatMap.value = false
  currentStep.value++
}

function addTier() {
  tiers.value.push({
    name: '', tierType: 'GENERAL', price: 0, quantityTotal: 50,
    colorCode: '#8b5cf6', saleStart: '', saleEnd: '',
  })
}

async function submit() {
  organizer.formFieldErrors = {}
  const validTiers = tiers.value.filter((t) => t.name.trim() && t.quantityTotal > 0)
  const finalDescription = eventForm.description
  const normalizedTags = String(eventForm.tagsText || '')
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
  const normalizedDefaultPrice = Number(eventForm.defaultPrice)
  const normalizedDefaultTierQuantity = Number(eventForm.defaultTierQuantity)
  const hasDefaultPrice = Number.isFinite(normalizedDefaultPrice) && normalizedDefaultPrice > 0
  const defaultTierQuantity = Number.isFinite(normalizedDefaultTierQuantity) && normalizedDefaultTierQuantity > 0
    ? normalizedDefaultTierQuantity
    : 1

  if (isEditing) {
    const result = await organizer.updateMyEvent(eventId, {
      title: eventForm.title,
      description: finalDescription,
      venue: eventForm.venue,
      city: eventForm.city,
      country: eventForm.country || 'Vietnam',
      locationCoords: eventForm.locationCoords || null,
      startTime: eventForm.startTime,
      endTime: eventForm.endTime,
      bannerUrl: eventForm.bannerUrl,
      imageUrl: eventForm.imageUrl || eventForm.bannerUrl || null,
      featured: Boolean(eventForm.featured),
      tags: normalizedTags,
    })
    if (result) {
      alert('Tạo sự kiện thành công! Sự kiện sẽ ở trạng thái CHỜ DUYỆT (PENDING) cho đến khi admin phê duyệt. Nếu chưa có hạng vé, bạn cần tạo hạng vé để bán vé.')
      router.push('/dashboard')
    }
  } else {
    const eventData = {
      title:       eventForm.title,
      description: finalDescription,
      venue:       eventForm.venue,
      city:        eventForm.city,
      country:     eventForm.country || 'Vietnam',
      locationCoords: eventForm.locationCoords || null,
      startTime:   eventForm.startTime,
      endTime:     eventForm.endTime,
      bannerUrl:   eventForm.bannerUrl,
      imageUrl:    eventForm.imageUrl || eventForm.bannerUrl || null,
      featured:    Boolean(eventForm.featured),
      tags:        normalizedTags,
      defaultPrice: hasDefaultPrice ? normalizedDefaultPrice : undefined,
      defaultTierQuantity: hasDefaultPrice ? defaultTierQuantity : undefined,
    }

    const result = await organizer.createFullEvent({
      eventData,
      seatMapData: includeSeatMap.value && seatMapForm.name ? {
        name:       seatMapForm.name,
        file:       seatMapForm.file || null,
        imageUrl:   seatMapForm.apiImageUrl || null,
      } : null,
      tiers: validTiers.map((t) => ({
        name:          t.name,
        tierType:      t.tierType,
        price:         t.price,
        quantityTotal: t.quantityTotal,
        colorCode:     t.colorCode,
        saleStart:     t.saleStart || null,
        saleEnd:       t.saleEnd || null,
      })),
    })

    if (result) router.push('/dashboard')
  }
}
</script>

<style scoped>
.step-fade-enter-active, .step-fade-leave-active { transition: all 0.2s ease; }
.step-fade-enter-from, .step-fade-leave-to       { opacity: 0; transform: translateX(10px); }
</style>
