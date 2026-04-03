# Frontend Integration Contract

Updated on: 2026-04-02

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

Returns all fields from list item plus long `description`.

### 4.3 Categories

`GET /api/events/categories`

```json
{
  "categories": ["Concert", "Festival", "Conference", "Comedy", "Sports", "Expo"]
}
```

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
    "id": 1001,
    "ticketTierId": 1001,
    "name": "VIP",
    "tierType": "VIP",
    "price": 4500000,
    "quantityAvailable": 120,
    "maxPerOrder": 4,
    "colorCode": "#8b5cf6"
  }
]
```

Note:

- Frontend should accept both `id` and `ticketTierId` (same value).
- `maxPerOrder` defaults to `4` if event/tier specific rule is not configured yet.

## 6. Checkout Contracts

### 6.1 Quote

`POST /api/checkout/quote` (auth required)

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

Response:

```json
{
  "id": "a3d1...",
  "orderCode": "TH-20260402-001",
  "status": "PENDING",
  "totalAmount": 2520000,
  "createdAt": "2026-04-02T10:30:00Z"
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
  "paymentId": "pmt_...",
  "paymentCode": "PAY-20260402-001",
  "status": "PENDING",
  "payUrl": "https://gateway/...",
  "expiresAt": "2026-04-02T10:45:00Z"
}
```

## 7. Orders and Profile

### 7.1 Current user orders

`GET /api/orders/me` (auth required)

### 7.2 Order detail

`GET /api/orders/{id}`

### 7.3 Cancel/refund

- `POST /api/orders/{id}/cancel`
- `POST /api/orders/{id}/refund`

Should return latest order status object.

### 7.4 Ticket download (optional contract)

`GET /api/orders/{id}/ticket-download`

```json
{
  "downloadUrl": "https://.../ticket.pdf",
  "expiresAt": "2026-04-03T00:00:00Z"
}
```

## 8. Dashboard (Role-Aware)

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

## 9. Enums Frontend Should Mirror

- `Role`: `CUSTOMER | ORGANIZER | ADMIN`
- `EventStatus`: `PUBLISHED | PENDING | APPROVED | REJECTED | DRAFT | CANCELLED`
- `OrderStatus`: `PENDING | CONFIRMED | CANCELLED | REFUNDED`
- `PaymentMethod`: `CARD | MOMO | ZALOPAY | BANK`
- `TicketTierType`: `GENERAL | VIP | VVIP | STANDING`

## 10. Frontend Notes

- Event hero on detail page should use `bannerUrl`.
- Progress/sold-out should use `soldCount / totalCapacity`.
- Save badge should use `minPrice` vs `originalPrice`.
- Organizer header should use `organizer.name` and `organizer.verified`.
- Ticket options block should use `/api/checkout/events/{eventId}/tiers`.
