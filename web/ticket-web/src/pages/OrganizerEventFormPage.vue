<template>
  <div class="min-h-screen py-24 px-4">
    <div class="max-w-3xl mx-auto">

      <!-- Header -->
      <div class="flex items-center gap-4 mb-8">
        <RouterLink to="/dashboard" class="btn-ghost p-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="m15 18-6-6 6-6"/></svg>
        </RouterLink>
        <div>
          <h1 class="text-2xl font-black text-white">{{ isEditing ? 'Edit Event' : (canManageSeatMap ? 'Create New Event' : 'Organizer Request Form') }}</h1>
          <p class="text-zinc-500 text-sm mt-0.5">{{ isEditing ? 'Update event details' : (canManageSeatMap ? 'Fill in details, then add seat map info for review/publishing' : 'Submit request sheet. Admin approval will upgrade your role to Organizer.') }}</p>
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
        {{ organizer.formError }}
      </div>

      <div v-if="organizer.formWarning" class="bg-amber-500/10 border border-amber-500/30 text-amber-300 rounded-xl px-4 py-3 text-sm mb-6">
        {{ organizer.formWarning }}
      </div>

      <div v-if="!canManageSeatMap && !isEditing" class="bg-blue-500/10 border border-blue-500/30 text-blue-300 rounded-xl px-4 py-3 text-sm mb-6">
        Customer account can only submit organizer request sheet. After admin approves your request, your role will become Organizer and you can create full event with seat map/ticket tiers.
      </div>

      <!-- STEP 0: Event Details -->
      <Transition name="step-fade" mode="out-in">
        <div v-if="currentStep === 0" key="step0" class="card p-6 space-y-5">
          <h2 class="font-bold text-white text-lg">Event Details</h2>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Event Title *</label>
            <input v-model="eventForm.title" type="text" placeholder="e.g. Coldplay Music of the Spheres" class="input-field" />
            <p v-if="ev('title')" class="text-red-400 text-xs mt-1">{{ ev('title') }}</p>
          </div>

          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Description</label>
            <textarea v-model="eventForm.description" rows="4" placeholder="Describe your event…" class="input-field resize-none" />
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Start Date & Time *</label>
              <input v-model="eventForm.startTime" type="datetime-local" class="input-field" />
              <p v-if="ev('startTime')" class="text-red-400 text-xs mt-1">{{ ev('startTime') }}</p>
            </div>
            <div>
              <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">End Date & Time *</label>
              <input v-model="eventForm.endTime" type="datetime-local" class="input-field" />
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

          <div class="flex justify-end pt-2">
            <button v-if="!isEditing" class="btn-primary" @click="nextStep(0)">
              {{ canManageSeatMap ? 'Next: Seat Map' : 'Next: Seat Map Image' }}
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
            <h2 class="font-bold text-white text-lg">{{ canManageSeatMap ? 'Seat Map' : 'Seat Map Image (Organizer Request)' }}</h2>
            <span class="text-xs text-zinc-500">{{ canManageSeatMap ? 'Optional — skip if not needed' : 'Optional but recommended for event review' }}</span>
          </div>

          <p v-if="!canManageSeatMap" class="text-xs text-zinc-500">
            This image will be attached to your event request and shown in event detail until full organizer seat map is configured.
          </p>

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

          <div v-if="canManageSeatMap">
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seat Map Image File</label>
            <input type="file" accept="image/*" class="input-field" @change="onSeatMapFileChange" />
            <p class="text-xs text-zinc-500 mt-1">Upload image directly to backend seatmap API.</p>
          </div>

          <div v-if="canManageSeatMap">
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seat Map API Image URL (optional)</label>
            <input v-model="seatMapForm.apiImageUrl" type="url" placeholder="https://…" class="input-field" />
          </div>

          <div v-else>
            <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Seat Map Reference URL (for request)</label>
            <input v-model="seatMapForm.requestImageUrl" type="url" placeholder="https://…" class="input-field" />
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
              <button v-if="canManageSeatMap" class="btn-ghost text-sm" @click="skipSeatMap">Skip</button>
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
                Submit Request Sheet
              </button>
              <button v-if="canManageSeatMap" class="btn-primary" @click="nextStep(1)">
                Next: Ticket Tiers
                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path d="M5 12h14m-7-7 7 7-7 7"/></svg>
              </button>
            </div>
          </div>
        </div>

        <!-- STEP 2: Ticket Tiers -->
        <div v-else-if="!isEditing && canManageSeatMap && currentStep === 2" key="step2" class="space-y-4">
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
                <input v-model.number="tier.price" type="number" min="0" placeholder="0" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Total Quantity *</label>
                <input v-model.number="tier.quantityTotal" type="number" min="1" placeholder="100" class="input-field" />
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
              :disabled="organizer.formLoading"
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
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore }        from '@/stores/auth.store'
import { useOrganizerStore }   from '@/stores/organizer.store'
import { embedSeatMapImageInDescription } from '@/utils/seatMapImage'

const router    = useRouter()
const route     = useRoute()
const auth      = useAuthStore()
const organizer = useOrganizerStore()

const eventId   = route.params.id
const isEditing = !!eventId

const canManageSeatMap = computed(() => auth.isOrganizer || auth.isAdmin)
const steps = computed(() => {
  if (isEditing) return ['Event Details']
  return canManageSeatMap.value
    ? ['Event Details', 'Seat Map', 'Ticket Tiers']
    : ['Event Details', 'Seat Map Image']
})
const currentStep = ref(0)
const formErrors  = reactive({})
const includeSeatMap = ref(true)

const eventForm = reactive({
  title:       '',
  description: '',
  venue:       '',
  city:        '',
  locationCoords: '',
  startTime:   '',
  endTime:     '',
  bannerUrl:   '',
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
  return canManageSeatMap.value ? seatMapForm.apiImageUrl : seatMapForm.requestImageUrl
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
        locationCoords: ev.locationCoords || '',
        startTime:   ev.date || '',
        endTime:     '',
        bannerUrl:   ev.banner || ev.image || '',
      })
    }
  }
})

function ev(field) { return formErrors[field] }

function nextStep(step) {
  Object.keys(formErrors).forEach((k) => delete formErrors[k])
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
  const validTiers = tiers.value.filter((t) => t.name.trim() && t.quantityTotal > 0)
  const finalDescription = (!isEditing && !canManageSeatMap.value)
    ? embedSeatMapImageInDescription(eventForm.description, seatMapForm.requestImageUrl)
    : eventForm.description

  if (isEditing) {
    const result = await organizer.updateMyEvent(eventId, {
      title: eventForm.title,
      description: finalDescription,
      venue: eventForm.venue,
      city: eventForm.city,
      locationCoords: eventForm.locationCoords || null,
      startTime: eventForm.startTime,
      endTime: eventForm.endTime,
      bannerUrl: eventForm.bannerUrl,
    })
    if (result) router.push('/dashboard')
  } else {
    const eventData = {
      title:       eventForm.title,
      description: finalDescription,
      venue:       eventForm.venue,
      city:        eventForm.city,
      locationCoords: eventForm.locationCoords || null,
      startTime:   eventForm.startTime,
      endTime:     eventForm.endTime,
      bannerUrl:   eventForm.bannerUrl,
    }

    const result = canManageSeatMap.value
      ? await organizer.createFullEvent({
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
      : await organizer.createOrganizerRequest(eventData)

    if (result) router.push('/dashboard')
  }
}
</script>

<style scoped>
.step-fade-enter-active, .step-fade-leave-active { transition: all 0.2s ease; }
.step-fade-enter-from, .step-fade-leave-to       { opacity: 0; transform: translateX(10px); }
</style>
