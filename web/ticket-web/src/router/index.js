// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('@/pages/HomePage.vue'),
    },
    {
        path: '/event/:id',
        name: 'EventDetail',
        component: () => import('@/pages/EventDetailPage.vue'),
        props: true,
    },
    {
        path: '/booking/:id',
        name: 'Booking',
        component: () => import('@/pages/BookingPage.vue'),
        props: true,
        meta: { requiresAuth: true },
    },
    {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('@/pages/CheckoutPage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/pages/ProfilePage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/pages/AdminPage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/pages/NotFoundPage.vue'),
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) return savedPosition
        return { top: 0, behavior: 'smooth' }
    },
})

router.beforeEach((to) => {
    const auth = useAuthStore()
    if (to.meta.requiresAuth && !auth.isLoggedIn) {
        return { name: 'Home' }
    }
    if (to.meta.requiresAdmin && !auth.isAdmin) {
        return { name: 'Home' }
    }
})

export default router
