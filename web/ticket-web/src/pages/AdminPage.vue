<template>
  <div class="min-h-screen py-24 animate-fade-in">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <p class="text-xs font-semibold text-violet-400 uppercase tracking-widest mb-1">Admin Panel</p>
          <h1 class="text-3xl font-black text-white">Dashboard</h1>
        </div>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div
          v-for="kpi in kpis"
          :key="kpi.label"
          class="card p-5 hover:border-zinc-700 transition-colors"
        >
          <div class="flex items-start justify-between mb-3">
            <div class="w-10 h-10 rounded-xl flex items-center justify-center" :class="kpi.iconBg">
              <span v-html="kpi.icon" class="w-5 h-5" :class="kpi.iconColor" />
            </div>
            <span class="text-xs font-semibold" :class="kpi.trendUp ? 'text-emerald-400' : 'text-red-400'">
              {{ kpi.trend }}
            </span>
          </div>
          <p class="text-2xl font-black text-white">{{ kpi.value }}</p>
          <p class="text-xs text-zinc-500 mt-1">{{ kpi.label }}</p>
        </div>
      </div>

      <!-- Tabs -->
      <div class="flex items-center gap-1 mb-6 bg-zinc-900 border border-zinc-800 p-1 rounded-2xl w-fit">
        <button
          v-for="tab in adminTabs"
          :key="tab.id"
          class="px-5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200"
          :class="activeTab === tab.id ? 'bg-violet-600 text-white' : 'text-zinc-400 hover:text-white'"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- TAB: Events -->
      <div v-if="activeTab === 'events'" class="space-y-4 animate-fade-in">
        <!-- Filter row -->
        <div class="flex flex-col sm:flex-row gap-3">
          <input v-model="eventSearch" type="text" placeholder="Search events…" class="input-field text-sm py-2.5 max-w-xs" />
          <select v-model="eventStatusFilter" class="input-field text-sm py-2.5 w-44">
            <option value="all">All Status</option>
            <option value="published">Published</option>
            <option value="pending">Pending Approval</option>
            <option value="rejected">Rejected</option>
          </select>
        </div>

        <AdminTable
          :columns="eventColumns"
          :rows="filteredAdminEvents"
          :total-rows="filteredAdminEvents.length"
          :current-page="1"
          :page-size="10"
          empty-text="No events found"
        >
          <template #cell-title="{ row }">
            <div class="flex items-center gap-3">
              <img :src="row.image" :alt="row.title" class="w-10 h-10 rounded-lg object-cover shrink-0" />
              <div class="min-w-0">
                <p class="font-semibold text-white text-sm truncate max-w-[200px]">{{ row.title }}</p>
                <p class="text-xs text-zinc-500">{{ row.category }}</p>
              </div>
            </div>
          </template>
          <template #cell-date="{ value }">
            <span class="text-zinc-300 text-sm">{{ formatDate(value) }}</span>
          </template>
          <template #cell-price="{ value }">
            <span class="text-violet-400 font-semibold text-sm">{{ formatPrice(value) }}</span>
          </template>
          <template #cell-sold="{ row }">
            <div class="flex items-center gap-2">
              <div class="flex-1 h-1.5 bg-zinc-800 rounded-full w-16 overflow-hidden">
                <div
                  class="h-full rounded-full bg-violet-600"
                  :style="{ width: `${Math.round((row.sold / row.capacity) * 100)}%` }"
                />
              </div>
              <span class="text-xs text-zinc-400 tabular-nums">{{ Math.round((row.sold / row.capacity) * 100) }}%</span>
            </div>
          </template>
          <template #cell-status="{ row }">
            <span :class="eventBadge(row.status)" class="capitalize">
              {{ eventStatusLabel(row.status) }}
            </span>
          </template>
          <template #cell-actions="{ row }">
            <div class="flex items-center gap-1">
              <template v-if="String(row.status || '').toLowerCase() === 'pending'">
                <button 
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-emerald-300 bg-emerald-500/10 border border-emerald-500/30 hover:bg-emerald-500/20 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="approvePendingEvent(row)"
                >
                  Approve
                </button>
                <button 
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-red-300 bg-red-500/10 border border-red-500/30 hover:bg-red-500/20 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="rejectPendingEvent(row)"
                >
                  Reject
                </button>
              </template>
              <button 
                :disabled="!hasAdminRole()"
                class="btn-ghost py-1 px-2 text-xs disabled:opacity-50 disabled:cursor-not-allowed"
                @click="editEvent(row)"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                Edit
              </button>
              <button 
                :disabled="!hasAdminRole()"
                class="py-1 px-2 rounded-lg text-xs text-red-400 hover:text-red-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                @click="confirmDelete(row)"
              >
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
              </button>
            </div>
          </template>
        </AdminTable>
      </div>

      <!-- TAB: Orders -->
      <div v-else-if="activeTab === 'orders'" class="space-y-4 animate-fade-in">
        <div class="flex flex-col sm:flex-row gap-3">
          <input v-model="orderSearch" type="text" placeholder="Search by order ID or event…" class="input-field text-sm py-2.5 max-w-xs" />
          <select v-model="orderStatusFilter" class="input-field text-sm py-2.5 w-44">
            <option value="all">All Status</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="PENDING">Pending</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="REFUNDED">Refunded</option>
          </select>
        </div>
        <div v-if="adminStore.ordersLoading" class="flex justify-center py-12">
          <svg class="w-8 h-8 animate-spin text-violet-500" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
        </div>
        <AdminTable
          v-else
          :columns="orderColumns"
          :rows="filteredAdminOrders"
          :total-rows="filteredAdminOrders.length"
          :current-page="1"
          :page-size="10"
          empty-text="No orders found"
        >
          <template #cell-id="{ value }">
            <span class="font-mono text-xs text-violet-400">{{ typeof value === 'string' ? value.slice(0, 8) + '…' : value }}</span>
          </template>
          <template #cell-eventTitle="{ value }">
            <span class="text-white text-sm font-medium line-clamp-1 max-w-[200px]">{{ value }}</span>
          </template>
          <template #cell-total="{ value }">
            <span class="text-white font-semibold">{{ formatPrice(value) }}</span>
          </template>
          <template #cell-status="{ row }">
            <span :class="orderBadge(row.status)" class="capitalize">{{ row.status }}</span>
          </template>
          <template #cell-bookedAt="{ value }">
            <span class="text-zinc-400 text-xs">{{ formatDateTime(value) }}</span>
          </template>
        </AdminTable>
      </div>

      <!-- TAB: Users -->
      <div v-else-if="activeTab === 'users'" class="space-y-4 animate-fade-in">
        <div class="bg-blue-500/10 border border-blue-500/30 text-blue-300 rounded-xl px-4 py-3 text-sm">
          Role approval list only shows accounts that submitted event creation requests and are waiting admin review.
        </div>

        <div class="flex flex-col sm:flex-row gap-3">
          <input
            v-model="userSearch"
            type="text"
            placeholder="Search users by name or email…"
            class="input-field text-sm py-2.5 max-w-xs"
            @input="debouncedFetchUsers"
          />
          <select v-model="userRoleFilter" class="input-field text-sm py-2.5 w-44" @change="onUserRoleFilter">
            <option value="all">All Roles</option>
            <option value="CUSTOMER">Customers</option>
            <option value="ORGANIZER">Organizers</option>
            <option value="ADMIN">Admins</option>
          </select>
        </div>
        <div v-if="adminStore.usersLoading" class="flex justify-center py-12">
          <svg class="w-8 h-8 animate-spin text-violet-500" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <div v-for="user in filteredUsers" :key="user.id" class="card p-5 hover:border-zinc-700 transition-colors">
            <div class="flex items-center gap-3 mb-3">
              <img :src="user.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.id}`" :alt="user.displayName || user.username" class="w-10 h-10 rounded-full bg-zinc-800" />
              <div class="min-w-0">
                <p class="font-semibold text-white text-sm truncate">{{ user.displayName || user.username }}</p>
                <p class="text-xs text-zinc-500 truncate">{{ user.email }}</p>
              </div>
              <span class="ml-auto shrink-0" :class="user.role === 'ADMIN' ? 'badge-violet' : user.role === 'ORGANIZER' ? 'badge-yellow' : 'badge-blue'">{{ user.role }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span :class="user.active ? 'badge-green' : 'badge-red'">{{ user.active ? 'Active' : 'Disabled' }}</span>
              <span class="text-xs text-zinc-500">{{ requestCountByUser[user.id] || 0 }} request(s)</span>
              <div class="flex gap-1">
                <button
                  v-if="user.active"
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-amber-400 hover:text-amber-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Deactivate"
                  @click="deactivateUser(user.id)"
                >Deactivate</button>
                <button
                  v-else
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-emerald-400 hover:text-emerald-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  title="Activate"
                  @click="activateUser(user.id)"
                >Activate</button>
                <button
                  v-if="user.role === 'CUSTOMER'"
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-violet-400 hover:text-violet-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="promoteOrganizer(user.id)"
                >→ Organizer</button>
                <button
                  v-if="user.role === 'ORGANIZER'"
                  :disabled="!hasAdminRole()"
                  class="py-1 px-2 rounded-lg text-xs text-zinc-400 hover:text-zinc-300 hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  @click="demoteCustomer(user.id)"
                >→ Customer</button>
              </div>
            </div>
          </div>
          <div v-if="filteredUsers.length === 0" class="col-span-full text-center text-zinc-500 py-12">No role-approval accounts found</div>
        </div>
      </div>
      <!-- TAB: Revenue -->
      <div v-else-if="activeTab === 'revenue'" class="space-y-6 animate-fade-in">
        <!-- Date filters -->
        <div class="flex flex-col sm:flex-row gap-3 items-end">
          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1 uppercase tracking-wide">From</label>
            <input v-model="revenueFrom" type="date" class="input-field text-sm py-2" />
          </div>
          <div>
            <label class="block text-xs font-semibold text-zinc-400 mb-1 uppercase tracking-wide">To</label>
            <input v-model="revenueTo" type="date" class="input-field text-sm py-2" />
          </div>
          <button class="btn-primary py-2" @click="fetchRevenue">Apply</button>
        </div>

        <!-- Revenue KPI cards -->
        <div v-if="adminStore.revenueLoading" class="flex justify-center py-12">
          <svg class="w-8 h-8 animate-spin text-violet-500" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 0 1 8-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
        </div>
        <div v-else class="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <div class="card p-5">
            <p class="text-xs text-zinc-500 mb-1">Gross Revenue (GMV)</p>
            <p class="text-xl font-black text-white">{{ formatPrice(adminStore.revenueSummary?.totalGrossRevenue ?? adminStore.revenueSummary?.gmv) }}</p>
          </div>
          <div class="card p-5">
            <p class="text-xs text-zinc-500 mb-1">Platform Fee Collected</p>
            <p class="text-xl font-black text-emerald-400">{{ formatPrice(adminStore.revenueSummary?.totalPlatformFee ?? adminStore.revenueSummary?.platformFeeTotal) }}</p>
          </div>
          <div class="card p-5">
            <p class="text-xs text-zinc-500 mb-1">Gateway Fees</p>
            <p class="text-xl font-black text-amber-400">{{ formatPrice(adminStore.revenueSummary?.totalGatewayFee ?? adminStore.revenueSummary?.gatewayFeeTotal) }}</p>
          </div>
          <div class="card p-5">
            <p class="text-xs text-zinc-500 mb-1">Organizer Net Payout</p>
            <p class="text-xl font-black text-violet-400">{{ formatPrice(adminStore.revenueSummary?.totalOrganizerNet ?? adminStore.revenueSummary?.organizerNetTotal) }}</p>
          </div>
        </div>

        <!-- Payments table -->
        <div class="card overflow-hidden">
          <div class="px-5 py-4 border-b border-zinc-800 flex items-center justify-between">
            <h3 class="font-bold text-white">Payment Transactions</h3>
            <span class="text-xs text-zinc-500">{{ adminStore.payments.length }} records</span>
          </div>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-zinc-800 text-left">
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Payment Code</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Order</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Method</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Status</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Amount</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Platform Fee</th>
                  <th class="px-4 py-3 text-xs font-semibold text-zinc-500 uppercase tracking-wide">Paid At</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="adminStore.payments.length === 0">
                  <td colspan="7" class="px-4 py-10 text-center text-zinc-500">No payment records</td>
                </tr>
                <tr
                  v-for="p in adminStore.payments"
                  :key="p.paymentId || p.paymentCode"
                  class="border-b border-zinc-800/50 hover:bg-zinc-800/30 transition-colors"
                >
                  <td class="px-4 py-3 font-mono text-xs text-violet-400">{{ p.paymentCode }}</td>
                  <td class="px-4 py-3 font-mono text-xs text-zinc-400">{{ p.orderCode || p.orderId?.slice(0,8) }}</td>
                  <td class="px-4 py-3 text-zinc-300">{{ p.method }}</td>
                  <td class="px-4 py-3">
                    <span :class="p.status === 'PAID' || p.status === 'SUCCESS' ? 'badge-green' : p.status === 'FAILED' ? 'badge-red' : 'badge-yellow'">{{ p.status }}</span>
                  </td>
                  <td class="px-4 py-3 font-semibold text-white">{{ formatPrice(p.amount) }}</td>
                  <td class="px-4 py-3 text-emerald-400">{{ formatPrice(p.platformFee) }}</td>
                  <td class="px-4 py-3 text-zinc-400 text-xs">{{ formatDateTime(p.paidAt || p.createdAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card p-5 space-y-4">
          <div class="flex items-center justify-between gap-3">
            <h3 class="font-bold text-white">Organizer Money Management</h3>
            <span class="text-xs text-zinc-500">All ticket sales deduct 10% platform fee before organizer payout</span>
          </div>

          <p v-if="adminStore.payoutError" class="text-red-400 text-sm bg-red-500/10 border border-red-500/20 rounded-xl px-3 py-2">
            {{ adminStore.payoutError }}
          </p>

          <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div v-for="org in organizerUsers" :key="org.id" class="rounded-xl border border-zinc-800 bg-zinc-900/50 p-4 space-y-3">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-white">{{ org.name }}</p>
                  <p class="text-xs text-zinc-500">{{ org.email }}</p>
                </div>
                <span class="badge-yellow">ORGANIZER</span>
              </div>

              <div class="flex gap-2">
                <button class="btn-secondary text-xs py-2 px-3" :disabled="adminStore.payoutLoading" @click="previewPayout(org.id)">
                  Preview
                </button>
                <button class="btn-primary text-xs py-2 px-3" :disabled="adminStore.payoutLoading" @click="executePayout(org.id)">
                  Execute Payout
                </button>
              </div>

              <div v-if="adminStore.payoutPreviews[org.id]" class="text-xs text-zinc-400 border border-zinc-800 rounded-lg p-3">
                <p>Pending payments: <span class="text-zinc-200">{{ adminStore.payoutPreviews[org.id].pendingPayments ?? 0 }}</span></p>
                <p>Total net payout: <span class="text-emerald-400">{{ formatPrice(adminStore.payoutPreviews[org.id].totalNetPayout ?? 0) }}</span></p>
              </div>

              <div v-if="adminStore.payoutResults[org.id]" class="text-xs text-zinc-400 border border-zinc-800 rounded-lg p-3">
                <p>Paid payments: <span class="text-zinc-200">{{ adminStore.payoutResults[org.id].paidPayments ?? 0 }}</span></p>
                <p>Total paid: <span class="text-emerald-400">{{ formatPrice(adminStore.payoutResults[org.id].totalPaid ?? 0) }}</span></p>
                <p class="text-zinc-500 mt-1">Ref: {{ adminStore.payoutResults[org.id].payoutReference || 'N/A' }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB: Sales -->
      <div v-else-if="activeTab === 'sales'" class="space-y-6 animate-fade-in">
        <PlatformSalesManagement />
      </div>
    </div>
    <Transition name="fade">
      <div
        v-if="showCreateModal"
        class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-start justify-center p-4 pt-20 overflow-y-auto"
        @click.self="showCreateModal = false"
      >
        <div class="bg-zinc-900 border border-zinc-700 rounded-3xl max-w-2xl w-full p-6 shadow-2xl animate-slide-up mb-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-xl font-black text-white">{{ editingEvent ? 'Edit Event' : 'Create New Event' }}</h2>
            <button class="btn-ghost p-2" @click="showCreateModal = false">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M18 6 6 18M6 6l12 12"/></svg>
            </button>
          </div>

          <div class="space-y-4">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Event Title</label>
                <input v-model="eventForm.title" type="text" placeholder="Enter event title" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Category</label>
                <select v-model="eventForm.category" class="input-field">
                  <option value="">Select category</option>
                  <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
                </select>
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Date</label>
                <input v-model="eventForm.date" type="date" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Time</label>
                <input v-model="eventForm.time" type="time" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Ticket Price (VND)</label>
                <input v-model.number="eventForm.price" type="number" placeholder="0" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Venue</label>
                <input v-model="eventForm.venue" type="text" placeholder="Venue name" class="input-field" />
              </div>
              <div>
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">City</label>
                <input v-model="eventForm.city" type="text" placeholder="City" class="input-field" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Capacity</label>
                <input v-model.number="eventForm.capacity" type="number" placeholder="Total capacity" class="input-field" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Description</label>
                <textarea v-model="eventForm.description" rows="4" placeholder="Describe the event…" class="input-field resize-none" />
              </div>
              <div class="sm:col-span-2">
                <label class="block text-xs font-semibold text-zinc-400 mb-1.5 uppercase tracking-wide">Image URL</label>
                <input v-model="eventForm.image" type="url" placeholder="https://…" class="input-field" />
              </div>
            </div>

            <div class="flex gap-3 pt-2">
              <button class="btn-secondary flex-1 justify-center" @click="showCreateModal = false">Cancel</button>
              <button class="btn-primary flex-1 justify-center" @click="saveEvent">
                {{ editingEvent ? 'Save Changes' : 'Publish Event' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Delete Confirm Modal -->
    <Transition name="fade">
      <div
        v-if="deleteTarget"
        class="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4"
        @click.self="deleteTarget = null"
      >
        <div class="bg-zinc-900 border border-zinc-700 rounded-2xl max-w-sm w-full p-6 shadow-2xl animate-slide-up text-center">
          <div class="w-14 h-14 rounded-full bg-red-500/20 border border-red-500/30 flex items-center justify-center mx-auto mb-4">
            <svg class="w-7 h-7 text-red-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </div>
          <h3 class="text-lg font-bold text-white mb-2">Delete Event?</h3>
          <p class="text-zinc-400 text-sm mb-6">
            Are you sure you want to delete <span class="text-white font-semibold">{{ deleteTarget?.title }}</span>? This action cannot be undone.
          </p>
          <div class="flex gap-3">
            <button class="btn-secondary flex-1 justify-center" @click="deleteTarget = null">Cancel</button>
            <button
              class="flex-1 justify-center bg-red-600 hover:bg-red-500 text-white font-semibold px-6 py-3 rounded-xl transition-all active:scale-95 inline-flex items-center gap-2"
              @click="deleteEvent"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import AdminTable from '@/components/AdminTable.vue'
import PlatformSalesManagement from '@/components/PlatformSalesManagement.vue'
import { useAdminStore } from '@/stores/admin.store'
import { useAuthStore } from '@/stores/auth.store'
import { categories }    from '@/data/events'

const adminStore = useAdminStore()
const auth = useAuthStore()

// Helper to check if current user has ADMIN role from JWT
function hasAdminRole() {
  return auth.isAdmin && auth.tokenValidated
}

// Helper to warn and prevent action if not authorized
function requireAdminRole(actionName = 'action') {
  if (!hasAdminRole()) {
    console.warn(`[Security] Unauthorized ${actionName}: User does not have ADMIN role in JWT`)
    return false
  }
  return true
}

const activeTab          = ref('events')
const showCreateModal    = ref(false)
const editingEvent       = ref(null)
const deleteTarget       = ref(null)
const eventSearch        = ref('')
const eventStatusFilter  = ref('all')
const orderSearch        = ref('')
const orderStatusFilter  = ref('all')
const userSearch         = ref('')
const userRoleFilter     = ref('all')
const revenueFrom        = ref('')
const revenueTo          = ref('')

function toDateInput(date) {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const today = new Date()
const thirtyDaysAgo = new Date(today)
thirtyDaysAgo.setDate(today.getDate() - 30)
revenueFrom.value = toDateInput(thirtyDaysAgo)
revenueTo.value = toDateInput(today)

const eventForm = reactive({
  title: '', category: '', date: '', time: '', price: 0,
  venue: '', city: '', capacity: 0, description: '', image: '',
})

// Debounce helper for user search
let userSearchTimer = null
function debouncedFetchUsers() {
  clearTimeout(userSearchTimer)
  userSearchTimer = setTimeout(() => adminStore.fetchUsers(userSearch.value || null), 350)
}

function onUserRoleFilter() {
  adminStore.fetchUsers(userSearch.value || null)
}

function dateRangeParams() {
  const fromDate = revenueFrom.value || toDateInput(thirtyDaysAgo)
  const toDate = revenueTo.value || toDateInput(today)
  return {
    from: `${fromDate}T00:00:00`,
    to: `${toDate}T23:59:59`,
  }
}

// Fetch data on mount
onMounted(async () => {
  await adminStore.fetchPendingEvents()
  const params = dateRangeParams()
  await Promise.all([
    adminStore.fetchAllEvents(),
    adminStore.fetchPlatformStats(),
    adminStore.fetchUsers(),
    adminStore.fetchAllOrders(),
    adminStore.fetchRevenueSummary(params),
    adminStore.fetchPayments(params),
  ])
})

async function fetchRevenue() {
  const params = dateRangeParams()
  await Promise.all([
    adminStore.fetchRevenueSummary(params),
    adminStore.fetchPayments(params),
  ])
}

async function previewPayout(organizerId) {
  if (!requireAdminRole('payout preview')) return
  await adminStore.previewOrganizerPayout(organizerId, dateRangeParams())
}

async function executePayout(organizerId) {
  if (!requireAdminRole('payout execution')) return
  const params = dateRangeParams()
  const payoutRef = `PAYOUT-${new Date().toISOString()
    .replaceAll('-', '')
    .replaceAll(':', '')
    .replaceAll('.', '')
    .replaceAll('T', '')
    .replaceAll('Z', '')
    .slice(0, 14)}`
  const payload = {
    from: params.from,
    to: params.to,
    payoutReference: payoutRef,
    note: 'Admin payout execution',
  }
  const executed = await adminStore.executeOrganizerPayout(organizerId, payload)
  if (executed) {
    await fetchRevenue()
  }
}

const adminTabs = [
  { id: 'events',  label: 'Events'  },
  { id: 'orders',  label: 'Orders'  },
  { id: 'users',   label: 'Users'   },
  { id: 'revenue', label: 'Revenue' },
  { id: 'sales',   label: 'Sales'   },
]

const eventColumns = [
  { key: 'title',   label: 'Event'     },
  { key: 'date',    label: 'Date'      },
  { key: 'price',   label: 'Price'     },
  { key: 'sold',    label: 'Capacity'  },
  { key: 'status',  label: 'Status'    },
  { key: 'actions', label: 'Actions', align: 'right' },
]

const orderColumns = [
  { key: 'id',         label: 'Order ID'   },
  { key: 'eventTitle', label: 'Event'      },
  { key: 'total',      label: 'Total'      },
  { key: 'status',     label: 'Status'     },
  { key: 'bookedAt',   label: 'Placed At'  },
]

const filteredAdminEvents = computed(() => {
  let list = adminStore.allEvents
  if (eventStatusFilter.value !== 'all') list = list.filter((e) => e.status === eventStatusFilter.value)
  if (eventSearch.value.trim()) {
    const q = eventSearch.value.toLowerCase()
    list = list.filter((e) =>
      (e.title    ?? '').toLowerCase().includes(q) ||
      (e.category ?? '').toLowerCase().includes(q)
    )
  }
  return list
})

const filteredAdminOrders = computed(() => {
  let list = adminStore.allOrders
  if (orderStatusFilter.value !== 'all') list = list.filter((o) => o.status === orderStatusFilter.value)
  if (orderSearch.value.trim()) {
    const q = orderSearch.value.toLowerCase()
    list = list.filter((o) =>
      String(o.id).toLowerCase().includes(q) ||
      String(o.orderCode ?? '').toLowerCase().includes(q) ||
      String(o.eventTitle ?? '').toLowerCase().includes(q)
    )
  }
  return list
})

const filteredUsers = computed(() => {
  const requestedUserIds = new Set(
    adminStore.pendingEvents
      .map((e) => String(e.organizerId || e.organizer?.id || ''))
      .filter(Boolean)
  )

  let list = adminStore.users.filter((u) => requestedUserIds.has(String(u.id)))
  if (userRoleFilter.value !== 'all') list = list.filter((u) => u.role === userRoleFilter.value)
  return list
})

const requestCountByUser = computed(() => {
  const map = {}
  for (const ev of adminStore.pendingEvents) {
    const key = String(ev.organizerId || ev.organizer?.id || '')
    if (!key) continue
    map[key] = (map[key] || 0) + 1
  }
  return map
})

const organizerUsers = computed(() =>
  adminStore.users.filter((u) => u.role === 'ORGANIZER')
)

const kpis = computed(() => [
  {
    label: 'Total Revenue',
    value: adminStore.kpiRevenue
      ? new Intl.NumberFormat('vi-VN', { notation: 'compact', style: 'currency', currency: 'VND' }).format(adminStore.kpiRevenue)
      : '₫ —',
    trend: '↑ 12.4%', trendUp: true,
    iconBg: 'bg-emerald-500/20', iconColor: 'text-emerald-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>',
  },
  {
    label: 'Total Events',
    value: adminStore.allEvents.length || adminStore.kpiOrders || '—',
    trend: `↑ ${adminStore.pendingEvents.length} pending`, trendUp: true,
    iconBg: 'bg-violet-500/20', iconColor: 'text-violet-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>',
  },
  {
    label: 'Total Orders',
    value: adminStore.kpiOrders ?? adminStore.allOrders.length,
    trend: '↑ 8.1%', trendUp: true,
    iconBg: 'bg-blue-500/20', iconColor: 'text-blue-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>',
  },
  {
    label: 'Active Users',
    value: adminStore.kpiUsers ?? (adminStore.users.length || '—'),
    trend: '↑ 5.3%', trendUp: true,
    iconBg: 'bg-amber-500/20', iconColor: 'text-amber-400',
    icon: '<svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>',
  },
])

const mockUsers = []  // removed — using adminStore.users

async function approvePendingEvent(row) {
  if (!requireAdminRole('event approval')) return
  const ok = await adminStore.approveEvent(row.id)
  if (ok) row.status = 'published'
}

async function rejectPendingEvent(row) {
  if (!requireAdminRole('event rejection')) return
  const ok = await adminStore.rejectEvent(row.id)
  if (ok) row.status = 'rejected'
}

function editEvent(row) {
  if (!requireAdminRole('event edit')) return
  editingEvent.value  = row
  Object.assign(eventForm, { ...row })
  showCreateModal.value = true
}

function confirmDelete(row) {
  if (!requireAdminRole('event deletion')) return
  deleteTarget.value = row
}

async function deleteEvent() {
  if (!requireAdminRole('event deletion')) return
  adminStore.deleteLocalEvent(deleteTarget.value.id)
  deleteTarget.value = null
}

async function saveEvent() {
  if (!requireAdminRole('event save')) return
  if (editingEvent.value) {
    await adminStore.updateEvent(editingEvent.value.id, { ...eventForm })
  } else {
    await adminStore.createEvent({ ...eventForm, status: 'pending' })
  }
  showCreateModal.value = false
  editingEvent.value    = null
  Object.assign(eventForm, { title: '', category: '', date: '', time: '', price: 0, venue: '', city: '', capacity: 0, description: '', image: '' })
}

// ─── User Management (JWT-protected) ─────────────────────────────────────
async function deactivateUser(userId) {
  if (!requireAdminRole('user deactivation')) return
  await adminStore.deactivateUser(userId)
}

async function activateUser(userId) {
  if (!requireAdminRole('user activation')) return
  await adminStore.activateUser(userId)
}

async function promoteOrganizer(userId) {
  if (!requireAdminRole('user role promotion')) return
  await adminStore.promoteOrganizer(userId)
}

async function demoteCustomer(userId) {
  if (!requireAdminRole('user role demotion')) return
  await adminStore.demoteCustomer(userId)
}

function formatDate(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC' })
}
function formatDateTime(d) {
  if (!d) return ''
  return new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
function formatPrice(val) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val ?? 0)
}
function orderBadge(status) {
  const key = String(status || '').toLowerCase()
  return { confirmed: 'badge-green', pending: 'badge-yellow', cancelled: 'badge-red', refunded: 'badge-blue' }[key] ?? 'badge-blue'
}

function eventBadge(status) {
  const key = String(status || '').toLowerCase()
  return {
    pending: 'badge-yellow',
    published: 'badge-green',
    on_sale: 'badge-green',
    approved: 'badge-green',
    rejected: 'badge-red',
    draft: 'badge-blue',
  }[key] ?? 'badge-blue'
}

function eventStatusLabel(status) {
  const key = String(status || '').toLowerCase()
  const mapped = {
    pending: 'Pending',
    published: 'Published',
    on_sale: 'Published',
    approved: 'Approved',
    rejected: 'Rejected',
    draft: 'Draft',
  }[key]
  return mapped || key || 'Unknown'
}
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to       { opacity: 0; }
</style>
