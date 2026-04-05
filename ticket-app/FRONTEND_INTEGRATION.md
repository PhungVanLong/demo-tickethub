# Frontend Integration Contract

Updated on: 2026-04-05

## 1. Runtime Basics

- Backend base URL: `http://localhost:8081`
- API base URL: `http://localhost:8081/api`
- Auth: JWT Bearer
- CORS allowed origins: `http://localhost:5173`, `http://localhost:3000`

Auth header for protected APIs:

```http
Authorization: Bearer <accessToken>
```

## 2. Global Conventions

- `Content-Type`: `application/json`
- Time format: ISO-8601 UTC, example `2026-06-15T19:30:00Z`

### Google Sign-In (new)

- Endpoint: `POST /api/auth/google`
- Request body:

```json
{
  "idToken": "<google_id_token_from_frontend_sdk>"
}
```

- Behavior:
- Verify Google ID token against backend `google.oauth.client-id`.
- If email exists: update profile (`fullName`, `avatarUrl`, verified flag) and return JWT.
- If email does not exist: create new `CUSTOMER` account and return JWT.
- Claims mapping used by backend:
- `email`: from Google token email claim (required).
- `fullName` mapping order: `name` -> (`given_name` + `family_name`) -> `email` fallback.
- `avatarUrl`: from Google `picture` claim (can be null if claim is not present).
- `isVerified`: resolved from `email_verified` claim.

- Response shape: same as `/api/auth/login` (`AuthResponse`).

### Error response (standard)

```json
{
  "timestamp": "2026-04-02T10:20:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "errors": [
    { "field": "email", "message": "Email is invalid" }
  ],
  "path": "/api/auth/login"
}
```

### Page response (standard)

```json
{
  "content": [],
  "number": 0,
  "size": 20,
  "totalElements": 120,
  "totalPages": 6
}
```

## 3. Security and Access Rules

Public:

- `POST /api/auth/**`
- `GET /api/events/published`
- `GET /api/events/{id}` (public detail)
- `GET /api/events/categories`
- `GET /api/events/search`
- `GET /api/events/city/**`
- `GET /uploads/**`

Authenticated:

- `GET /api/dashboard`
- Checkout order/payment intent
- `GET /api/orders/me`
- `POST /api/events` (CUSTOMER/ORGANIZER/ADMIN)

Admin only:

- `/api/admin/**`
- `/api/users/**`
- `GET /api/events/pending`
- `POST /api/events/{eventId}/approve`
- `POST /api/events/{eventId}/reject`
- `GET /api/stats/platform`

Organizer owner or Admin:

- `GET /api/stats/organizer/{organizerId}`
- Organizer event management endpoints

### 3.1 FE Security Matrix (quick use)

Legend:

- `Y` = allowed by role
- `Y*` = allowed by role + ownership/business checks in service
- `-` = not allowed

| Endpoint | CUSTOMER | ORGANIZER | STAFF | ADMIN | Notes |
|---|---|---|---|---|---|
| `POST /api/events` | Y | Y | - | Y | Create event idea |
| `POST /api/events/{eventId}/staff` | - | Y* | - | - | Event must be `PUBLISHED`, organizer must own event |
| `POST /api/events/{eventId}/seat-maps` | Y* | Y* | - | Y* | Must own event (or admin) |
| `POST /api/events/{eventId}/seat-maps/{seatMapId}/tiers` | Y* | Y* | - | Y* | Must own event (or admin), seat map must belong to event |
| `GET /api/events` | - | - | - | Y | Admin list all events with pricing/capacity metrics |
| `GET /api/events/pending` | - | - | - | Y | Admin list pending events with pricing/capacity metrics |
| `POST /api/vouchers/events/{eventId}` | - | Y* | - | Y | Organizer must own event; event must be `PUBLISHED` |
| `POST /api/admin/vouchers/platform` | - | - | - | Y | Create global platform voucher (`applyOn = ALL`) |
| `POST /api/vouchers/validate` | Y | Y | Y | Y | Auth required |
| `POST /api/tickets/{ticketId}/use` | - | - | Y* | Y | Staff can only scan tickets of linked organizer |

FE implementation notes:

- Hide action buttons if role is not eligible by table above.
- Even when button is shown (`Y*`), backend can still return `403`/`400` due to ownership or state checks.
- Keep a standard error toast mapping: `401` (login again), `403` (no permission), `400` (invalid state/input), `404` (resource not found).

## 4. Event Contracts

### 4.1 Published list

`GET /api/events/published`

Query params:

- `page`, `size`
- `category`, `city`, `featured`
- `sort`: `date_asc`, `date_desc`, `price_asc`, `price_desc`, `rating_desc`

Response content item:

```json
{
  "id": 1,
  "title": "Coldplay...",
  "slug": "coldplay-2026",
  "category": "Concert",
  "startTime": "2026-06-15T19:30:00Z",
  "endTime": "2026-06-15T22:00:00Z",
  "venue": "My Dinh National Stadium",
  "city": "Hanoi",
  "country": "Vietnam",
  "imageUrl": "https://.../thumb.jpg",
  "bannerUrl": "https://.../banner.jpg",
  "minPrice": 1200000,
  "originalPrice": 1500000,
  "status": "PUBLISHED",
  "featured": true,
  "tags": ["Pop", "Rock", "Live"],
  "rating": 4.9,
  "reviewCount": 2430,
  "soldCount": 42500,
  "totalCapacity": 50000,
  "organizer": {
    "id": "uuid",
    "name": "Live Nation Vietnam",
    "verified": true
  }
}
```

### 4.2 Event detail

`GET /api/events/{id}`

Access behavior:

- Public users can only view events with status `PUBLISHED`.
- Event owner can view their own event detail even when status is not `PUBLISHED`.

Response shape (actual DTO):

```json
{
  "id": 1,
  "title": "Coldplay Concert 2026",
  "slug": "coldplay-concert-2026-1775123456789",
  "category": "Concert",
  "description": "Long event description...",
  "startTime": "2026-06-15T19:30:00Z",
  "endTime": "2026-06-15T22:00:00Z",
  "venue": "My Dinh National Stadium",
  "city": "Hanoi",
  "country": "Vietnam",
  "imageUrl": "https://.../thumb.jpg",
  "bannerUrl": "https://.../banner.jpg",
  "minPrice": 1200000,
  "originalPrice": 1500000,
  "status": "PUBLISHED",
  "featured": true,
  "tags": ["Pop", "Live"],
  "rating": 4.9,
  "reviewCount": 2430,
  "soldCount": 42500,
  "totalCapacity": 50000,
  "organizer": {
    "id": "uuid",
    "name": "Live Nation Vietnam",
    "verified": true
  }
}
```

FE notes:

- `minPrice`, `originalPrice`, `soldCount`, `totalCapacity` are aggregated from ticket tiers of the event.
- `tags` is always returned as array (empty array if no tags).
- If `bannerUrl` is null in DB, backend may fallback to image value in list/detail mapping.

### 4.3 Categories

`GET /api/events/categories`

```json
{
  "categories": ["Concert", "Festival", "Conference", "Comedy", "Sports", "Expo"]
}
```

### 4.4 Admin event list (with pricing/capacity)

`GET /api/events` (admin only)

`GET /api/events/pending` (admin only)

Response item shape is aligned with `EventListItemResponse` and includes:

- `minPrice`
- `originalPrice`
- `soldCount`
- `totalCapacity`

This lets admin dashboard/event moderation screens display price and quantity metrics directly without extra per-event calls.

## 5. Seat Map and Ticket Options

### 5.1 Seat maps by event

`GET /api/events/{eventId}/seat-maps`

```json
[
  {
    "id": 501,
    "name": "Main Hall",
    "totalRows": 20,
    "totalCols": 30,
    "imageUrl": "https://...",
    "layoutJson": "{...}"
  }
]
```

### 5.2 Tiers for checkout block

`GET /api/checkout/events/{eventId}/tiers`

```json
[
  {
    "ticketTierId": 1001,
    "name": "VIP",
    "price": 4500000,
    "quantityAvailable": 120
  }
]
```

Note:

- Current DTO returns only: `ticketTierId`, `name`, `price`, `quantityAvailable`.
- `tierType`, `colorCode`, `maxPerOrder`, and duplicated `id` are not included in this endpoint response.

### 5.3 Data linkage for FE (important)

Backend does not store `event_id` directly in `ticket_tiers`.

Relationship used by backend queries:

- `events.id` -> `seat_maps.event_id`
- `seat_maps.id` -> `ticket_tiers.seat_map_id`

So when FE needs event pricing/capacity data:

- Event detail (`GET /api/events/{id}`) already returns aggregated pricing/capacity (`minPrice`, `originalPrice`, `soldCount`, `totalCapacity`).
- Tier list (`GET /api/checkout/events/{eventId}/tiers`) is resolved through seat map relation internally.

## 6. Checkout Contracts

### 6.1 Quote

`POST /api/checkout/quote` (auth required)

Request:

```json
{
  "userId": "uuid",
  "eventId": 1,
  "items": [
    { "ticketTierId": 1001, "quantity": 2 }
  ],
  "voucherCode": "SPRING10"
}
```

Quote behavior:

- Backend reads user from JWT and overrides `userId` internally.
- Due current DTO contract, `userId` is still required in request body format.
- Voucher validation uses the same rule set as order creation (`/api/vouchers/validate` equivalent checks).

Response:

```json
{
  "subtotal": 2400000,
  "serviceFee": 120000,
  "discount": 100000,
  "total": 2420000,
  "currency": "VND",
  "expiresAt": "2026-04-02T11:00:00Z"
}
```

Note:

- `expiresAt` in quote response is currently event end time from backend (`event.endTime`), not a separate quote hold expiration.

### 6.2 Create order

`POST /api/checkout/orders` (auth required)

Request (no `userId`, backend reads from JWT):

```json
{
  "eventId": 1,
  "items": [
    { "ticketTierId": 1001, "quantity": 2 }
  ]
}
```

Order creation behavior:

- Checkout currently works by ticket tier + quantity.
- Backend checks `quantityAvailable` of each selected tier and decreases stock after order creation.
- No seat lock/hold API is applied during quote/order.

Response:

```json
{
  "id": "uuid",
  "orderCode": "ORD1775123456789",
  "status": "PENDING",
  "totalAmount": 2420000,
  "createdAt": "2026-04-02T10:30:00"
}
```

### 6.3 Create payment intent

`POST /api/checkout/orders/{orderId}/payments`

Request:

```json
{
  "method": "CARD",
  "returnUrl": "http://localhost:5173/payment-result",
  "cancelUrl": "http://localhost:5173/checkout"
}
```

Response:

```json
{
  "paymentId": "uuid",
  "paymentCode": "PAY1775123456789",
  "status": "PENDING",
  "amount": 2420000,
  "platformFeeAmount": 242000,
  "gatewayFeeAmount": 48400,
  "organizerNetAmount": 2129600,
  "payUrl": "http://localhost:8081/api/checkout/payments/fake-gateway?paymentCode=...",
  "expiresAt": "2026-04-05T23:59:59"
}
```

### 6.4 Seat selection + payment status (important)

Seat selection APIs are now available (temporary hold flow):

- `GET /api/events/{eventId}/seat-maps/{seatMapId}/seats`
- `POST /api/events/{eventId}/seat-maps/{seatMapId}/seats/hold`
- `POST /api/events/{eventId}/seat-maps/{seatMapId}/seats/release`
- `POST /api/events/{eventId}/seat-maps/{seatMapId}/seats/confirm`

Hold request:

```json
{
  "seatIds": [101, 102, 103]
}
```

Hold response:

```json
{
  "holdToken": "uuid",
  "expiresAt": "2026-04-06T12:00:00",
  "seats": [
    {
      "seatId": 101,
      "seatCode": "A-01",
      "rowLabel": "A",
      "colNumber": 1,
      "ticketTierId": 1001,
      "status": "HELD",
      "holdExpiresAt": "2026-04-06T12:00:00"
    }
  ]
}
```

Release request:

```json
{
  "holdToken": "uuid"
}
```

Confirm request:

```json
{
  "holdToken": "uuid",
  "orderId": "uuid"
}
```

Important behavior:

- Hold TTL is 10 minutes.
- Expired holds are auto-released by scheduler.
- Seats move through statuses: `AVAILABLE -> HELD -> BOOKED`.
- Checkout (`/api/checkout/quote`, `/api/checkout/orders`) is still tier + quantity based.
- For now, FE should treat hold/confirm flow as seat-lock workflow and checkout as pricing/order workflow.

### 6.5 Voucher contracts (Organizer + Admin)

Validate voucher before checkout:

- `POST /api/vouchers/validate` (auth required)
- Request:

```json
{
  "code": "SPRING10",
  "eventId": 1,
  "orderAmount": 2400000
}
```

- Response includes `valid`, `message`, `calculatedDiscount`, `voucherType`, `applyOn`, `eventId`.

Create event voucher (Organizer or Admin for a specific event):

- `POST /api/vouchers/events/{eventId}`
- Access: `ORGANIZER` or `ADMIN`
- Voucher behavior: `voucherType = ORGANIZER_EVENT`, `applyOn = SPECIFIC_EVENT`.
- Scope: only applies to the given `{eventId}`.

Create platform-wide voucher (Admin):

- `POST /api/admin/vouchers/platform`
- Access: `ADMIN` only
- Voucher behavior: `voucherType = PLATFORM`, `applyOn = ALL`, `assignedToUser = null`.
- Scope: can be used on all events (subject to validity window, usage limit, min order value).

Checkout behavior (important):

- `POST /api/checkout/quote` and `POST /api/checkout/orders` now use the same voucher validation rules as `POST /api/vouchers/validate`.
- If voucher is invalid for user/event/order context, backend returns `400` with validation message.

## 7. Orders (Frontend Note - Fully Updated)

This section reflects the current backend behavior on branch develop.

### 7.1 FE order flow (recommended)

1. `POST /api/checkout/quote`
2. `POST /api/checkout/orders`
3. `POST /api/checkout/orders/{orderId}/payments`
4. Poll/order refresh with `GET /api/orders/{orderId}` and/or `GET /api/orders/me`

### 7.2 Create order from checkout

`POST /api/checkout/orders` (auth required)

Request:

```json
{
  "eventId": 1,
  "items": [
    { "ticketTierId": 1001, "quantity": 2 }
  ],
  "voucherCode": "SPRING10"
}
```

Response shape (actual DTO):

```json
{
  "id": "uuid",
  "orderCode": "ORD-1775123456789-123",
  "status": "PENDING",
  "totalAmount": 2520000,
  "createdAt": "2026-04-05T10:30:00"
}
```

Notes:

- `status` here is from checkout DTO (`PENDING`, `CONFIRMED`, `CANCELLED`, `REFUNDED`).
- Backend reads `userId` from JWT, frontend must not send `userId`.
- Header `Idempotency-Key` exists but is not enforced yet by backend logic.

### 7.3 Order detail

`GET /api/orders/{orderId}` (auth required)

Authorization rule (important):

- Admin can view any order.
- Non-admin can view only own order.

Response shape (actual DTO):

```json
{
  "id": "uuid",
  "userId": "uuid",
  "orderCode": "ORD-1775123456789-123",
  "orderStatus": "PENDING",
  "totalAmount": 2520000,
  "discountAmount": 100000,
  "finalAmount": 2420000,
  "notes": "Checkout order",
  "createdAt": "2026-04-05T10:30:00",
  "updatedAt": "2026-04-05T10:30:00"
}
```

Status handling for FE:

- `401`: missing/invalid token
- `403`: authenticated but not owner/admin
- `404`: order id not found

### 7.4 Current user orders

`GET /api/orders/me` (auth required)

Current response type is `List<Order>` (entity response, not flattened DTO yet).
FE should safely read these stable fields only:

- `id`
- `orderCode`
- `orderStatus`
- `totalAmount`
- `discountAmount`
- `finalAmount`
- `createdAt`

Avoid coupling UI to nested relation fields from this endpoint.

### 7.5 Update/cancel/refund order

Available endpoints:

- `PUT /api/orders/{orderId}`
- `POST /api/orders/{orderId}/cancel`
- `POST /api/orders/{orderId}/refund`

Current response type is `Order` entity (not OrderResponse DTO).
For UI update, trust `orderStatus`, `updatedAt`, and `id` only.

### 7.6 Payment intent for order

`POST /api/checkout/orders/{orderId}/payments` (auth required)

Request:

```json
{
  "method": "CARD",
  "returnUrl": "http://localhost:5173/payment-result",
  "cancelUrl": "http://localhost:5173/checkout"
}
```

Response shape (actual DTO):

```json
{
  "paymentId": "uuid",
  "paymentCode": "PAY1775123456789",
  "status": "PENDING",
  "amount": 2420000,
  "platformFeeAmount": 242000,
  "gatewayFeeAmount": 48400,
  "organizerNetAmount": 2129600,
  "payUrl": "http://localhost:8081/api/checkout/payments/fake-gateway?paymentCode=...",
  "expiresAt": "2026-04-05T23:59:59"
}
```

### 7.7 FE troubleshooting for Order APIs

Common mistakes:

- Sending Bearer token on `/api/auth/login` request (set login request to No Auth).
- Using `token` field from login response. Correct field is `accessToken`.
- Using wrong HTTP method (`POST` instead of `GET`) for `/api/orders/{orderId}`.

Quick checklist when seeing `403`:

1. Confirm token is fresh (`accessToken` from latest login response).
2. Confirm request is owner order or admin user.
3. Confirm endpoint/method pair is correct.

## 8. Tickets (Complete Response with Buyer and Event Context)

All ticket endpoints now return enriched TicketResponse including buyer info, event details, and pricing.

### 8.1 Get ticket by ID

`GET /api/tickets/{ticketId}` (auth required)

Authorization rule:

- Admin can view any ticket.
- Non-admin can view only own ticket (must own the order).

Response shape (enriched DTO):

```json
{
  "ticketId": "uuid",
  "ticketCode": "TCK_550e8400-e29b-41d4-a716-446655440000",
  "qrCodeData": "TICKET|TCK_550e8400-e29b-41d4-a716-446655440000|550e8400-e29b-41d4-a716-446655440000|87654321-e29b-41d4-a716-446655440001|ACTIVE",
  "seatNumber": "A-12",
  "ticketStatus": "ACTIVE",
  "orderItemId": "uuid",
  "createdAt": "2026-04-05T10:30:00",
  "usedAt": null,
  
  "buyerId": "uuid",
  "buyerFullName": "Nguyen Van A",
  "buyerEmail": "user@example.com",
  "buyerPhone": "+84901234567",
  
  "eventId": 1,
  "eventTitle": "Coldplay Concert 2026",
  "eventVenue": "My Dinh National Stadium",
  "eventCity": "Hanoi",
  "eventStartTime": "2026-06-15T19:30:00Z",
  "eventBannerUrl": "https://example.com/coldplay-banner.jpg",
  
  "tierId": 1001,
  "tierName": "VIP",
  "tierType": "VIP",
  "unitPrice": 4500000
}
```

Field descriptions:

- `ticketId`: Unique ticket UUID
- `ticketCode`: Machine-readable ticket code (TCK_ prefix)
- `qrCodeData`: String data for FE to generate QR code (format: `TICKET|code|id|orderItemId|status`)
- `seatNumber`: Alpha-numeric seat (e.g., "A-12", "VIP-Row5-Seat10")
- `ticketStatus`: `ACTIVE` (can be used) | `USED` (already consumed) | `CANCELLED` (refunded)
- `orderItemId`: Link to order item (internal, use for analytics)
- `createdAt`: ISO-8601 ticket creation time
- `usedAt`: ISO-8601 time ticket was scanned/used (null if not used yet)
- `buyerId`, `buyerFullName`, `buyerEmail`, `buyerPhone`: Buyer info from order
- `eventId`, `eventTitle`, `eventVenue`, `eventCity`, `eventStartTime`, `eventBannerUrl`: Event context
- `tierId`, `tierName`, `tierType`, `unitPrice`: Price tier info

Status codes:

- `200`: Success
- `401`: Missing/invalid token
- `403`: Authenticated but not ticket owner/admin
- `404`: Ticket ID not found

### 8.2 Get my tickets

`GET /api/tickets/me` (auth required)

Returns list of all tickets for current user.

How backend matches with purchased orders:

- Read `userId` from JWT token (not from request params/body).
- Query chain: `Ticket -> OrderItem -> Order`.
- Only tickets whose `Order.userId == currentUserId` are returned.
- Tickets appear after payment success flow creates tickets for confirmed order.

Current filtering behavior:

- Endpoint returns all tickets owned by the user.
- Not filtered by `OrderStatus` at this endpoint.
- Not filtered by `TicketStatus` at this endpoint.
- FE should group/filter locally by `ticketStatus` (`ACTIVE`, `USED`, `CANCELLED`) if needed.

Response shape:

```json
[
  {
    "ticketId": "uuid",
    "ticketCode": "TCK_...",
    "qrCodeData": "TICKET|...",
    "seatNumber": "A-12",
    "ticketStatus": "ACTIVE",
    "orderItemId": "uuid",
    "createdAt": "2026-04-05T10:30:00",
    "usedAt": null,
    "buyerId": "uuid",
    "buyerFullName": "...",
    "buyerEmail": "...",
    "buyerPhone": "...",
    "eventId": 1,
    "eventTitle": "Coldplay Concert 2026",
    "eventVenue": "My Dinh National Stadium",
    "eventCity": "Hanoi",
    "eventStartTime": "2026-06-15T19:30:00Z",
    "eventBannerUrl": "...",
    "tierId": 1001,
    "tierName": "VIP",
    "tierType": "VIP",
    "unitPrice": 4500000
  }
]
```

Important runtime notes (current backend):

- Event fields (`eventId`, `eventTitle`, `eventVenue`, `eventCity`, `eventStartTime`, `eventBannerUrl`) are now enriched from `TicketTier -> SeatMap -> Event` on backend.
- `seatNumber` may be null if seat assignment has not been implemented for the ticket tier/event.
- `buyer*` fields are expected to be present when related order-user data exists; FE should still handle null safely.

Status codes:

- `200`: Success
- `401`: Missing/invalid token

### 8.3 Get tickets by order

`GET /api/tickets/order/{orderId}` (auth required)

Authorization rule:

- Admin can view tickets of any order.
- Non-admin can view only if they own the order.

Response shape:

```json
[
  { ... ticket response item ... },
  { ... ticket response item ... }
]
```

Status codes:

- `200`: Success
- `401`: Missing/invalid token
- `403`: Not owner/admin of order
- `404`: Order ID not found

### 8.4 Download ticket

`GET /api/tickets/{ticketId}/download` (auth required)

Same response as GET detail endpoint.

Frontend use case: Export ticket as PDF/image with:

- Event banner (use `eventBannerUrl`)
- Event title, time, location (use `eventTitle`, `eventStartTime`, `eventVenue`)
- Ticket code and QR code (use `ticketCode` and generate QR from `qrCodeData`)
- Buyer name (use `buyerFullName`)
- Tier name and seat (use `tierName`, `seatNumber`)

### 8.5 Mark ticket as used

`POST /api/tickets/{ticketId}/use` (auth required)

No request body needed.

Response shape: Updated ticket response with `ticketStatus: "USED"` and `usedAt: "2026-04-05T15:45:00"`.

Authorization rule:

- `STAFF` can scan/mark tickets as used (dedicated scanning account).
- `ADMIN` can mark any ticket used.
- `CUSTOMER` and `ORGANIZER` are not allowed to call this endpoint.
- For demo: scan is not restricted by event start/end time.
- `STAFF` can only scan tickets that belong to events of their linked organizer.

Status codes:

- `200`: Success, ticket marked used
- `400`: Ticket already used or not in ACTIVE status
- `401`: Missing/invalid token
- `403`: Not allowed role or staff scans ticket from another organizer
- `404`: Ticket ID not found

### 8.7 Organizer creates staff account (scan account)

`POST /api/users/staff` (organizer auth required)

Purpose:

- Organizer creates a separate `STAFF` account used for ticket scanning operations.

Request:

```json
{
  "email": "staff01@tickethub.com",
  "password": "Staff@123456",
  "fullName": "Gate Staff 01",
  "phone": "+84901234567"
}
```

Response:

```json
{
  "userId": "uuid",
  "email": "staff01@tickethub.com",
  "fullName": "Gate Staff 01",
  "role": "STAFF",
  "active": true
}
```

Status codes:

- `200`: Staff account created
- `400`: Email already exists / invalid request
- `401`: Missing or invalid token
- `403`: Current user is not organizer

### 8.8 Organizer creates staff account inside an approved event (new)

`POST /api/events/{eventId}/staff` (organizer auth required)

Purpose:

- Create staff account directly from event management screen.
- Enforce event-level ownership and approval state before account creation.

Request:

```json
{
  "email": "staff02@tickethub.com",
  "password": "Staff@123456",
  "fullName": "Gate Staff 02",
  "phone": "+84909876543"
}
```

Response:

```json
{
  "userId": "uuid",
  "email": "staff02@tickethub.com",
  "fullName": "Gate Staff 02",
  "role": "STAFF",
  "active": true
}
```

Business rules:

- Organizer must be owner of `{eventId}`.
- Event status must be `PUBLISHED` (already approved by admin).
- If event is not approved yet, API returns `400`.
- If organizer is not owner of event, API returns `403`.

Status codes:

- `200`: Staff account created
- `400`: Event not approved / email already exists / invalid request
- `401`: Missing or invalid token
- `403`: Event does not belong to current organizer
- `404`: Event not found

Frontend recommendation:

- In event detail/management page, show "Create Staff" button only when event status is `PUBLISHED`.
- Call `POST /api/events/{eventId}/staff` as default flow for organizer event pages.
- Keep `POST /api/users/staff` for global organizer account management screen.

### 8.6 FE ticket display checklist

- Show ticket code prominently (use `ticketCode`)
- Generate QR code from `qrCodeData` string using frontend QR library
- Display event info in header (banner, title, time, venue)
- Show buyer name and email for reference
- Display tier type and seat number
- Show status badge: ACTIVE (blue), USED (gray), CANCELLED (red)
- "Mark Used" button → `POST /api/tickets/{id}/use`
- "Download" button → `GET /api/tickets/{id}/download` then generate PDF
- If status is USED, show `usedAt` timestamp

---

## 9. Dashboard (Role-Aware)

`GET /api/dashboard` (auth required)

```json
{
  "role": "ADMIN",
  "userId": "uuid",
  "email": "admin@mail.com",
  "fullName": "Admin User",
  "canCreateEvent": true,
  "stats": {
    "totalUsers": 2000,
    "totalEvents": 420,
    "publishedEvents": 380,
    "totalOrders": 56000,
    "totalGMV": 12500000000
  }
}
```

Role behavior:

- `ADMIN`: platform-wide stats
- `ORGANIZER`: organizer-only stats
- `STAFF`: operation/support user (scope depends on backend authorization rules)
- `CUSTOMER`: customer-only stats

## 9. Dashboard (Role-Aware)

`GET /api/dashboard` (auth required)

```json
{
  "role": "ADMIN",
  "userId": "uuid",
  "email": "admin@mail.com",
  "fullName": "Admin User",
  "canCreateEvent": true,
  "stats": {
    "totalUsers": 2000,
    "totalEvents": 420,
    "publishedEvents": 380,
    "totalOrders": 56000,
    "totalGMV": 12500000000
  }
}
```

Role behavior:

- `ADMIN`: platform-wide stats
- `ORGANIZER`: organizer-only stats
- `CUSTOMER`: customer-only stats

## 10. Enums Frontend Should Mirror

- `Role`: `CUSTOMER | STAFF | ORGANIZER | ADMIN`
- `EventStatus`: `PUBLISHED | PENDING | APPROVED | REJECTED | DRAFT | CANCELLED`
- `OrderStatus`: `PENDING | CONFIRMED | CANCELLED | REFUNDED`
- `PaymentMethod`: `CARD | MOMO | ZALOPAY | BANK`
- `TicketTierType`: `GENERAL | VIP | VVIP | STANDING`
- `TicketStatus`: `ACTIVE | USED | CANCELLED`

## 11. Frontend Notes

- Event hero on detail page should use `bannerUrl`.
- Progress/sold-out should use `soldCount / totalCapacity`.
- Save badge should use `minPrice` vs `originalPrice`.
- Organizer header should use `organizer.name` and `organizer.verified`.
- Ticket options block should use `/api/checkout/events/{eventId}/tiers`.
- Each ticket response now includes full buyer, event, and tier context for rich display.
