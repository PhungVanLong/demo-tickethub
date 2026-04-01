// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
    // Mock logged-in user
    const user = ref({
        id: 'usr_001',
        name: 'Alex Nguyen',
        email: 'alex@tickethub.vn',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex',
        role: 'user', // 'user' | 'admin'
        joinedAt: '2025-01-15',
    })

    const isLoggedIn = computed(() => !!user.value)
    const isAdmin = computed(() => user.value?.role === 'admin')

    function login(credentials) {
        // Mock login — in production replace with API call
        user.value = {
            id: 'usr_001',
            name: 'Alex Nguyen',
            email: credentials.email,
            avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Alex',
            role: credentials.email.includes('admin') ? 'admin' : 'user',
            joinedAt: '2025-01-15',
        }
    }

    function logout() {
        user.value = null
    }

    function switchRole() {
        if (user.value) {
            user.value.role = user.value.role === 'admin' ? 'user' : 'admin'
        }
    }

    return { user, isLoggedIn, isAdmin, login, logout, switchRole }
})
