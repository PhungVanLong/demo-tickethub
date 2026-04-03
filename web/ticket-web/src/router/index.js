// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { logAction } from '@/services/actionLogger'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

const routes = [
    // ── Public & authenticated pages (with Navbar + Footer) ─────────────────
    {
        path: '/',
        component: DefaultLayout,
        children: [
            {
                path: '',
                name: 'Home',
                component: () => import('@/pages/HomePage.vue'),
            },
            {
                path: 'event/:id',
                name: 'EventDetail',
                component: () => import('@/pages/EventDetailPage.vue'),
                props: true,
            },
            {
                path: 'booking/:id',
                name: 'Booking',
                component: () => import('@/pages/BookingPage.vue'),
                props: true,
                meta: { requiresAuth: true },
            },
            {
                path: 'checkout',
                name: 'Checkout',
                component: () => import('@/pages/CheckoutPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/pages/DashboardPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/pages/ProfilePage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'vouchers',
                name: 'Vouchers',
                component: () => import('@/pages/VouchersPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'admin',
                name: 'Admin',
                redirect: '/dashboard',
                meta: { requiresAuth: true, requiresAdmin: true },
            },
            {
                path: 'payment/:paymentCode',
                name: 'Payment',
                component: () => import('@/pages/PaymentPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'order/:orderId',
                name: 'OrderDetail',
                component: () => import('@/pages/OrderDetailPage.vue'),
                props: true,
                meta: { requiresAuth: true },
            },
            {
                path: 'organizer',
                name: 'OrganizerDashboard',
                component: () => import('@/pages/OrganizerDashboardPage.vue'),
                meta: { requiresAuth: true, requiresOrganizer: true },
            },
            {
                path: 'organizer/events/create',
                name: 'OrganizerEventCreate',
                component: () => import('@/pages/OrganizerEventFormPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'organizer/events/:id/edit',
                name: 'OrganizerEventEdit',
                component: () => import('@/pages/OrganizerEventFormPage.vue'),
                props: true,
                meta: { requiresAuth: true },
            },
        ],
    },
    // ── Auth pages (no Navbar, centered layout) ──────────────────────────────
    {
        path: '/',
        component: AuthLayout,
        children: [
            {
                path: 'login',
                name: 'Login',
                component: () => import('@/pages/LoginPage.vue'),
                meta: { guestOnly: true },
            },
            {
                path: 'register',
                name: 'Register',
                component: () => import('@/pages/RegisterPage.vue'),
                meta: { guestOnly: true },
            },
        ],
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

router.beforeEach(async (to, from) => {
    const auth = useAuthStore()

    logAction('ROUTE_NAVIGATE', {
        from: from.fullPath || '-',
        to: to.fullPath,
    })

    // Restore session from localStorage token on first navigation
    if (auth.token && !auth.user && !auth.tokenValidated) {
        await auth.fetchMe()
    }

    // Validate JWT token for authenticated routes
    if (auth.token && !auth.tokenValidated && (to.meta.requiresAuth || to.meta.requiresAdmin || to.meta.requiresOrganizer)) {
        const isValid = await auth.validateToken()
        if (!isValid) {
            return { name: 'Login', query: { redirect: to.fullPath } }
        }
    }

    // Redirect unauthenticated users to login
    if (to.meta.requiresAuth && !auth.isLoggedIn) {
        return { name: 'Login', query: { redirect: to.fullPath } }
    }

    // Redirect non-admins away from admin page (with JWT verification)
    if (to.meta.requiresAdmin) {
        if (!auth.isAdmin) {
            logAction('ROUTE_ADMIN_DENIED', { userId: auth.user?.id, role: auth.user?.role })
            return { name: 'Home' }
        }
    }

    // Redirect non-organizers away from organizer pages
    if (to.meta.requiresOrganizer && !auth.isOrganizer) {
        return { name: 'Home' }
    }

    // Redirect already-logged-in users away from guest-only pages
    if (to.meta.guestOnly && auth.isLoggedIn) {
        return { name: 'Home' }
    }
})

export default router

