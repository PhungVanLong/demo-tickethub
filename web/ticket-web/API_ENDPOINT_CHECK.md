# API Endpoint Verification Report

## ❌ Issues Found

### 1. Voucher Service — Missing `/api` prefix
**File**: `src/services/voucher.service.js`

| Endpoint | Current | Should Be | Status |
|----------|---------|-----------|--------|
| Get my vouchers | `/vouchers/me` | `/api/vouchers/me` | ❌ WRONG |
| Get organizer vouchers | `/vouchers/organizer` | `/api/vouchers/organizer` | ❌ WRONG |
| Create event voucher | `/vouchers/events/{id}` | `/api/vouchers/events/{id}` | ❌ WRONG |
| Get event vouchers | `/vouchers/events/{id}` | `/api/vouchers/events/{id}` | ❌ WRONG |
| Delete voucher | `/vouchers/{id}` | `/api/vouchers/{id}` | ❌ WRONG |
| Update voucher | `/vouchers/{id}` | `/api/vouchers/{id}` | ❌ WRONG |
| Validate voucher | `/vouchers/validate` | `/api/vouchers/validate` | ❌ WRONG |

**Issue**: Missing `/api` prefix causes requests to go to `http://localhost:8081/vouchers/...` instead of `http://localhost:8081/api/vouchers/...`

**Impact**: All voucher endpoints will fail with 404

---

### 2. Platform Sales Service — Missing `/api` prefix
**File**: `src/services/platformSales.service.js`

| Endpoint | Current | Should Be | Status |
|----------|---------|-----------|--------|
| Get active sales | `/admin/platform-sales/active` | `/api/admin/platform-sales/active` | ❌ WRONG |
| Get all sales | `/admin/platform-sales` | `/api/admin/platform-sales` | ❌ WRONG |
| Create sale | `/admin/platform-sales` | `/api/admin/platform-sales` | ❌ WRONG |
| Update sale | `/admin/platform-sales/{id}` | `/api/admin/platform-sales/{id}` | ❌ WRONG |
| Deactivate sale | `/admin/platform-sales/{id}` | `/api/admin/platform-sales/{id}` | ❌ WRONG |

**Issue**: Missing `/api` prefix causes requests to go to `http://localhost:8081/admin/...` instead of `http://localhost:8081/api/admin/...`

**Impact**: All platform sales endpoints will fail with 404

---

### 3. Voucher Service — Response Handling
**File**: `src/services/voucher.service.js`

**Current Pattern**:
```javascript
getMyVouchers() {
  return api.get('/vouchers/me')  // Returns Promise<AxiosResponse>
}
```

**Should Be**:
```javascript
getMyVouchers() {
  const res = await api.get('/api/vouchers/me')
  return res.data  // Returns Promise<data>
}
```

**Issue**: Inconsistent with other services (event.service.js, booking.service.js, admin.service.js)

**Impact**: Consumers get entire AxiosResponse object instead of just data

---

## ✅ Correct Patterns (Reference)

### event.service.js (✓ Correct)
```javascript
async getPublished(params = {}) {
    const res = await api.get('/api/events/published', { params })
    return res.data
}
```

### admin.service.js (✓ Correct)
```javascript
async getRevenueSummary(params = {}) {
    const res = await api.get('/api/admin/revenue/summary', { params })
    return res.data
}
```

### booking.service.js (✓ Correct)
```javascript
async getCheckoutTiers(eventId) {
    const res = await api.get(`/api/checkout/events/${eventId}/tiers`)
    return res.data
}
```

---

## Fix Required

### Fix 1: Update voucher.service.js
- Add `/api` prefix to all endpoints
- Use `async/await` pattern with `res.data` extraction
- Match style of other services

### Fix 2: Update platformSales.service.js  
- Add `/api` prefix to all endpoints
- Use `async/await` pattern with `res.data` extraction

---

## Backend API Contract (from EVENT_CREATION_INFO.md)

- Base URL: `http://localhost:8081`
- All endpoints should be at: `/api/{resource}`
- Auth: Bearer token required (already handled in api.js interceptor)
- Standard paths:
  - Events: `/api/events`
  - Checkout: `/api/checkout`
  - Orders: `/api/orders`
  - Admin: `/api/admin`
  - Users: `/api/users`
  - **Vouchers: `/api/vouchers`** (missing in current code)
  - **Platform Sales: `/api/admin/platform-sales`** (missing in current code)
