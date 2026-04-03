# Payment & Ticket Creation Flow Analysis

## Current Flow

### 1. Checkout Page (`CheckoutPage.vue`)

**Step 1: Contact Information**
- Collects: firstName, lastName, email, phone
- Validates contact info

**Step 2: Payment Method**
- Select method: card, momo, zalopay, bank
- Card validation (if card selected)

**Step 3: Confirm Order** → `placeOrder()` function:
```javascript
async function placeOrder() {
  // 1. Create order
  const order = await cart.createOrder()
  
  // 2. Create payment intent
  const orderId = order.orderId ?? order.id
  const pi = await cart.createPaymentIntent(orderId, form.paymentMethod)
  
  // 3. Redirect to payment page
  router.push({ 
    path: `/payment/${paymentCode}`, 
    query: { orderId } 
  })
}
```

---

### 2. API Calls in Order

#### Call 1: `POST /api/checkout/orders`
**From**: `booking.service.js` → `createOrder(data)`

**Payload**:
```json
{
  "eventId": "event-123",
  "items": [
    {
      "ticketTierId": "tier-1",
      "quantity": 2
    }
  ]
}
```

**Response** (expected):
```json
{
  "orderId": "order-abc-123",
  "id": "order-abc-123",
  "status": "PENDING",
  "eventId": "event-123",
  "items": [...],
  "total": 500000
}
```

**Backend Task**: 
- ✅ Create order with status = PENDING
- ✅ Hold/reserve seats (temporary)
- ❓ Should NOT create tickets yet (tickets created on payment success)

---

#### Call 2: `POST /api/checkout/orders/{orderId}/payments`
**From**: `booking.service.js` → `createPaymentIntent(orderId, data)`

**Payload**:
```json
{
  "method": "CARD|MOMO|ZALOPAY|BANK",
  "returnUrl": "http://localhost:5173/payment-result",
  "cancelUrl": "http://localhost:5173/checkout"
}
```

**Response** (expected):
```json
{
  "paymentCode": "PAY-123-abc",
  "paymentId": "pay-123",
  "orderId": "order-abc-123",
  "status": "PENDING",
  "amount": 500000,
  "method": "CARD"
}
```

**Backend Task**:
- ✅ Create payment intent with status = PENDING
- ✅ Return paymentCode for webhook reference

---

### 3. Payment Page (`PaymentPage.vue`)

**Simulation UI**:
- User clicks "Simulate Successful Payment" or "Simulate Failed Payment"
- Calls: `bookingStore.fakeWebhook(paymentCode, succeed)`

```javascript
async function simulate(succeed) {
  const res = await bookingStore.fakeWebhook(paymentCode, succeed)
  result.value = succeed ? 'success' : 'failed'
}
```

---

#### Call 3: `POST /api/checkout/payments/webhook/fake`
**From**: `booking.service.js` → `fakeWebhook(data)`

**Payload**:
```json
{
  "paymentCode": "PAY-123-abc",
  "success": true,
  "transactionId": "sim-1709894561234",
  "message": "Payment simulated successfully"
}
```

**Response** (expected):
```json
{
  "orderId": "order-abc-123",
  "status": "CONFIRMED",
  "message": "Payment successful"
}
```

**Backend Task (CRITICAL)**:
1. ✅ Find payment by paymentCode
2. ✅ Update payment status = SUCCESS/FAILED
3. ✅ Update order status = CONFIRMED (if payment success)
4. 🔴 **CREATE TICKETS** ← THIS IS CRITICAL
   - For each item in order:
     - Create ticket record with unique code
     - Link ticket to order
     - Set ticket status = ACTIVE
     - Generate QR code or ticket reference

---

### 4. Order Detail Page (`OrderDetailPage.vue`)

**Display tickets from order**:
```javascript
// Expected order structure:
{
  id: "order-abc-123",
  tickets: [
    {
      type: "VIP",
      qty: 2,
      price: 250000
    }
  ],
  status: "confirmed"
}
```

---

## ❌ Issues & Gaps

### Issue 1: Ticket Creation Unknown
**Status**: 🟡 UNCLEAR

Location of ticket creation:
- Frontend: No ticket creation code in `booking.store.js` or services
- Backend: Likely in webhook handler for fake payment

**What needs verification**:
1. Are tickets created in backend when webhook succeeds?
2. Does `/api/checkout/orders/{orderId}` return ticket data?
3. Are tickets created immediately or asynchronously?

---

### Issue 2: Order Status Lifecycle Unclear
**Status**: 🟡 UNCLEAR

Expected status flow:
```
PENDING (order created)
    ↓
CONFIRMED (payment successful via webhook)
    ↓
COMPLETED (tickets generated/delivered)
```

**What needs verification**:
1. What statuses does backend use?
2. When is order marked CONFIRMED - immediately or after ticket creation?
3. Is there a separate ticket status vs order status?

---

### Issue 3: User Contact Info Not Sent to Backend
**Status**: 🔴 ISSUE

**Current**: CheckoutPage collects firstName, lastName, email, phone but **NEVER SENDS** to backend

**createOrder() payload**:
```javascript
// Current - MISSING user contact info
const payload = {
  eventId: event.value.id,
  items: selections.value.map((s) => ({
    ticketTierId: s.ticketType.id,
    quantity: s.qty,
  })),
}

// Should include:
// {
//   eventId: "...",
//   userId: auth.user.id,
//   items: [...],
//   purchaserName: form.firstName + " " + form.lastName,
//   purchaserEmail: form.email,
//   purchaserPhone: form.phone
// }
```

**Impact**: Backend doesn't have purchaser information for:
- Ticket email delivery
- Customer support (who bought tickets)
- Invoice/receipt generation

---

### Issue 4: Voucher/Discount Code Not Used
**Status**: 🔴 ISSUE

**Current**: Quote API call doesn't include voucher code

**Expected payload**:
```javascript
const payload = {
  userId: auth.user?.id,
  eventId: event.value.id,
  items: [...],
  voucherCode: form.voucherCode,  // ← MISSING
}
```

**Impact**: Discount codes not applied to orders

---

### Issue 5: Contact Form Fields Not Normalized
**Status**: 🟡 CAUTION

**Current code** uses dangerous field access:
```javascript
const form = reactive({
  firstName: auth.user?.name?.split(' ')[0] ?? '',
  lastName: auth.user?.name?.split(' ').slice(1).join(' ') ?? '',
  // ...
})
```

**Issue**: If `auth.user.name` is null/undefined, split will fail

**Should be**:
```javascript
function splitName(fullName = '') {
  const parts = String(fullName).trim().split(' ')
  return {
    firstName: parts[0] ?? '',
    lastName: parts.slice(1).join(' ') ?? ''
  }
}
```

---

### Issue 6: No Seat Allocation Tracking
**Status**: 🟡 UNCLEAR

**Current**: Cart stores ticketTierId + quantity, but:
- No specific seat numbers selected
- No seat map used
- No seat availability shown

**Questions**:
1. Are specific seats allocated when order created?
2. Is capacity/availability checked per ticket tier?
3. How does backend handle overselling if multiple users checkout simultaneously?

---

## Required Backend Implementation

### When Webhook Succeeds:

```
1. Find Payment by paymentCode
2. Update Payment: status = SUCCESS, completedAt = now()
3. Find Order by orderId
4. Update Order: status = CONFIRMED, updatedAt = now()
5. For each item in order.items:
   a. Query TicketTier by tier ID
   b. Deduct quantity from tier availability
   c. Create Ticket records (qty times):
      - ticketCode: generate unique code
      - qrCode: generate QR code
      - orderId: link to order
      - ticketTierId: reference tier
      - status: ACTIVE
      - createdAt: now()
6. Return: { orderId, status: "CONFIRMED", tickets: [...] }
```

---

## Testing Checklist

- [ ] Create order with 2 VIP tickets
- [ ] Check order status = PENDING
- [ ] Create payment intent
- [ ] Simulate successful payment
- [ ] Verify:
  - Order status changed to CONFIRMED
  - 2 ticket records created in DB
  - Each ticket has unique code + QR code
  - Order detail page shows 2 tickets
  - Ticket status = ACTIVE
- [ ] Verify ticket availability decreased in TicketTier
- [ ] Test failed payment:
  - Order stays PENDING
  - No tickets created
  - Seats still reserved for timeout period

---

## Frontend Fixes Needed

1. **Add voucher code field to CheckoutPage**
2. **Include purchaser contact info in createOrder payload**
3. **Fix name splitting logic**
4. **Show seat map (if seat-level booking)**
5. **Validate ticket availability before checkout**
