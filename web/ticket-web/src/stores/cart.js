// src/stores/cart.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
    const event = ref(null)   // full event object
    const selections = ref([])     // [{ ticketType, qty }]

    const totalTickets = computed(() =>
        selections.value.reduce((sum, s) => sum + s.qty, 0)
    )

    const subtotal = computed(() =>
        selections.value.reduce((sum, s) => sum + s.ticketType.price * s.qty, 0)
    )

    const serviceFee = computed(() => Math.round(subtotal.value * 0.05))

    const grandTotal = computed(() => subtotal.value + serviceFee.value)

    function setBooking(eventObj, selected) {
        event.value = eventObj
        selections.value = selected.filter((s) => s.qty > 0)
    }

    function clear() {
        event.value = null
        selections.value = []
    }

    return {
        event,
        selections,
        totalTickets,
        subtotal,
        serviceFee,
        grandTotal,
        setBooking,
        clear,
    }
})
