# Frontend Integration Contract

Updated on: 2026-04-05

## 1. Runtime Basics

- Backend base URL: `http://localhost:8081`

- API base URL: `http://localhost:8081/api`

- Auth: JWT Bearer

- CORS allowed origins: `http://localhost:5173`, `http://localhost:3000`

- **Performance Update (2026-04-13):** Backend now uses Batch Fetching for all list endpoints. N+1 queries are eliminated.

- **Data Scale:** System is benchmarked and optimized for 5000+ events.

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

### Forgot Password (QuÃªn máº­t kháº©u)

- Endpoint: `POST /api/auth/forgot-password` (gá»­i OTP vá» email)

- Endpoint: `POST /api/auth/reset-password` (xÃ¡c thá»±c OTP vÃ  Ä‘áº·t láº¡i máº­t kháº©u má»›i)

**LÆ°u Ã½ UI:**

- Náº¿u tÃ i khoáº£n Ä‘Æ°á»£c táº¡o báº±ng Google, ngÆ°á»i dÃ¹ng váº«n cÃ³ thá»ƒ Ä‘áº·t láº¡i máº­t kháº©u má»›i qua email nÃ y.

- Sau khi Ä‘áº·t láº¡i máº­t kháº©u, ngÆ°á»i dÃ¹ng cÃ³ thá»ƒ Ä‘Äƒng nháº­p báº±ng email vÃ  máº­t kháº©u má»›i, hoáº·c tiáº¿p tá»¥c Ä‘Äƒng nháº­p báº±ng Google nhÆ° bÃ¬nh thÆ°á»ng.

- Náº¿u email khÃ´ng tá»“n táº¡i, backend tráº£ vá» lá»—i 404.

**Khuyáº¿n nghá»‹:**

- Hiá»ƒn thá»‹ note nÃ y á»Ÿ mÃ n hÃ¬nh "QuÃªn máº­t kháº©u" Ä‘á»ƒ ngÆ°á»i dÃ¹ng hiá»ƒu rÃµ vá» chá»©c nÄƒng.

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

| `DELETE /api/events/{eventId}` | - | Y* | - | Y | Organizer owner or admin can delete event |

| `DELETE /api/admin/events/{eventId}` | - | - | - | Y | Admin-only API to delete any event |

FE implementation notes:

- Hide action xÃ³a sá»± kiá»‡n náº¿u khÃ´ng pháº£i admin hoáº·c khÃ´ng pháº£i organizer owner.

- Äá»ƒ xÃ³a sá»± kiá»‡n vá»›i quyá»n admin, gá»i API: `DELETE /api/admin/events/{eventId}` (chá»‰ role ADMIN, khÃ´ng cáº§n ownership).

- Náº¿u lÃ  organizer owner, dÃ¹ng API: `DELETE /api/events/{eventId}`.

- Ngay cáº£ khi hiá»ƒn thá»‹ nÃºt xÃ³a (`Y*`), backend váº«n cÃ³ thá»ƒ tráº£ vá» `403`/`400` náº¿u khÃ´ng Ä‘á»§ quyá»n hoáº·c tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡.

- Mapping lá»—i toast: `401` (login láº¡i), `403` (khÃ´ng Ä‘á»§ quyá»n), `400` (dá»¯ liá»‡u/tráº¡ng thÃ¡i khÃ´ng há»£p lá»‡), `404` (khÃ´ng tÃ¬m tháº¥y).

## 4. Event Contracts

### 4.1 Published list

`GET /api/events/published`

Query params:

- `page`, `size`

- `category`, `city`, `featured`

- `q`: search query string (minimum 1 char)

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

Query params:

- `page`, `size` (defaults: 0, 20)

**Ghi chÃº (2026-04-13):** Danh sÃ¡ch nÃ y hiá»‡n Ä‘Æ°á»£c láº¥y Ä‘á»™ng tá»« Database vÃ  há»— trá»£ phÃ¢n trang chuáº©n `PageResponse`.

```json

{

  "content": ["Concert", "Festival", "Conference", "Comedy", "Sports", "Expo"],

  "number": 0,

  "size": 20,

  "totalElements": 6,

  "totalPages": 1

}

```

### 4.4 Search Events

`GET /api/events/search?q={keyword}&page={page}&size={size}`

- `q`: Tá»« khÃ³a tÃ¬m kiáº¿m (Ã­t nháº¥t 1 kÃ½ tá»±, tÃ¬m trong tiÃªu Ä‘á» hoáº·c mÃ´ táº£).

- `page`: Sá»‘ trang (báº¯t Ä‘áº§u tá»« 0). LuÃ´n truyá»n khi gá»i tá»« FE.

- `size`: Sá»‘ lÆ°á»£ng item má»—i trang (khuyáº¿n nghá»‹ 20, tá»‘i Ä‘a 50).

**LÆ°u Ã½ FE:**

- LuÃ´n truyá»n Ä‘á»§ cáº£ 3 tham sá»‘ `q`, `page`, `size` khi gá»i API search.

- Káº¿t quáº£ tráº£ vá» dáº¡ng chuáº©n phÃ¢n trang (xem má»¥c 2.2 Page response).

- CÃ¡c trÆ°á»ng trong tá»«ng item giá»‘ng nhÆ° API `/api/events/published` (xem EventListItemResponse).

- Táº¥t cáº£ cÃ¡c sá»‘ liá»‡u nhÆ° giÃ¡, sá»‘ lÆ°á»£ng vÃ©, organizer Ä‘á»u Ä‘Ã£ Ä‘Æ°á»£c batch fetching tá»‘i Æ°u, khÃ´ng cáº§n gá»i thÃªm API phá»¥.

**VÃ­ dá»¥:**

```http

GET /api/events/search?q=marathon&page=0&size=20

```

**FE nÃªn:**

- Hiá»ƒn thá»‹ tá»•ng sá»‘ káº¿t quáº£ (`totalElements`), sá»‘ trang (`totalPages`).

- Hiá»ƒn thá»‹ loading khi search, debounce khi nháº­p tá»« khÃ³a.

- KhÃ´ng gá»i láº¡i API náº¿u tá»« khÃ³a < 1 kÃ½ tá»±.

### 4.5 Admin event list (with pricing/capacity)

**QUAN TRá»ŒNG:** CÃ¡c API Admin hiá»‡n Ä‘Ã£ há»— trá»£ vÃ  **báº¯t buá»™c** phÃ¢n trang Ä‘á»ƒ trÃ¡nh treo há»‡ thá»‘ng vá»›i dá»¯ liá»‡u lá»›n (5000+ báº£n ghi).

- `GET /api/events?page={page}&size={size}` (admin only)

- `GET /api/events/pending?page={page}&size={size}` (admin only)

**Cáº¥u trÃºc tráº£ vá» (PageResponse):**

```json

{

  "content": [

    {

      "id": 1,

      "title": "...",

      "minPrice": 500000,

      "soldCount": 100,

      "totalCapacity": 1000,

      "status": "PUBLISHED"

    }

  ],

  "number": 0,

  "size": 10,

  "totalElements": 5010,

  "totalPages": 501

}

```

Dá»¯ liá»‡u item (`content`) tÆ°Æ¡ng Ä‘Æ°Æ¡ng vá»›i `EventListItemResponse`. Admin dashboard nÃªn sá»­ dá»¥ng cÃ¡c trÆ°á»ng nÃ y Ä‘á»ƒ hiá»ƒn thá»‹ metric trá»±c tiáº¿p mÃ  khÃ´ng cáº§n gá»i thÃªm API phá»¥.

### 4.6 Performance & Pagination Rules (FE Note)

**Báº¯t buá»™c phÃ¢n trang (Má»šI):**

- **ToÃ n bá»™** cÃ¡c API tráº£ vá» danh sÃ¡ch (Public Search, Admin Catalog, Pending List) Ä‘á»u yÃªu cáº§u truyá»n `page` (báº¯t Ä‘áº§u tá»« 0) vÃ  `size` tá»« phÃ­a FE. 

- Náº¿u khÃ´ng truyá»n, backend sáº½ máº·c Ä‘á»‹nh tráº£ vá» `page=0`, `size=20` (hoáº·c `size=10` cho Admin).

- Viá»‡c phÃ¢n trang cho Admin Catalog lÃ  cá»±c ká»³ quan trá»ng Ä‘á»ƒ Ä‘áº¡t Ä‘Æ°á»£c LCP < 2.5s khi há»‡ thá»‘ng cÃ³ hÃ ng chá»¥c ngÃ n sá»± kiá»‡n.

**Batch fetching:**

- Backend Ä‘Ã£ tá»‘i Æ°u, gom toÃ n bá»™ dá»¯ liá»‡u liÃªn quan (organizer, ticket tiers, metrics) trong 1 láº§n truy váº¥n, khÃ´ng cÃ³ N+1 query.

- FE chá»‰ cáº§n render theo Ä‘Ãºng dá»¯ liá»‡u tráº£ vá», khÃ´ng cáº§n gá»i thÃªm API phá»¥ Ä‘á»ƒ láº¥y organizer, giÃ¡, sá»‘ lÆ°á»£ng vÃ©...

**Hiá»‡u nÄƒng:**

- Thá»i gian pháº£n há»“i trung bÃ¬nh cho 1 trang (20 sá»± kiá»‡n) lÃ  < 50ms á»Ÿ táº£i bÃ¬nh thÆ°á»ng.

- FE nÃªn tá»‘i Æ°u UI loading, chá»‰ gá»i láº¡i khi thá»±c sá»± cáº§n (thay Ä‘á»•i filter, page, search...).

**FE cáº§n lÆ°u Ã½:**

- LuÃ´n kiá»ƒm tra vÃ  hiá»ƒn thá»‹ phÃ¢n trang (sá»‘ trang, tá»•ng sá»‘ item).

- KhÃ´ng gá»i láº¡i API khi khÃ´ng thay Ä‘á»•i filter/search.

- Xá»­ lÃ½ tá»‘t tráº¡ng thÃ¡i loading, empty, error.

### 4.5 Táº¡o sá»± kiá»‡n vÃ  háº¡ng vÃ© (chuáº©n hÃ³a UI/logic)

#### Táº¡o sá»± kiá»‡n (POST /api/events)

- **TrÆ°á»ng báº¯t buá»™c:** `title`, `venue`, `city`, `startTime`, `endTime`.

- **TrÆ°á»ng tÃ¹y chá»n:** `category`, `country`, `bannerUrl`, `imageUrl`, `featured`, `tags`.

- **Táº¡o auto tier:**

  - Náº¿u truyá»n `defaultPrice > 0` vÃ  `defaultTierQuantity >= 1`, backend sáº½ tá»± táº¡o seat map + tier máº·c Ä‘á»‹nh (General Admission).

  - Náº¿u khÃ´ng truyá»n hoáº·c `defaultPrice <= 0`, chá»‰ táº¡o event, khÃ´ng cÃ³ tier/seat map máº·c Ä‘á»‹nh.

- **Sau khi táº¡o:** Event luÃ´n á»Ÿ tráº¡ng thÃ¡i `PENDING`, chá» admin duyá»‡t má»›i public.

**VÃ­ dá»¥ request táº¡o event cÃ³ auto tier:**

```json

{

  "title": "Live Show A",

  "venue": "My Dinh National Stadium",

  "city": "Hanoi",

  "startTime": "2026-06-20T19:00:00",

  "endTime": "2026-06-20T22:00:00",

  "defaultPrice": 5000000,

  "defaultTierQuantity": 200

}

```

#### Táº¡o háº¡ng vÃ© (ticket tier) cho sá»± kiá»‡n

- Sau khi event Ä‘Ã£ táº¡o, organizer cÃ³ thá»ƒ táº¡o thÃªm tier qua API `/api/ticket-tiers`.

- **TrÆ°á»ng báº¯t buá»™c:** `seatMapId`, `name`, `tierType`, `price > 0`, `quantityTotal > 0`.

- Náº¿u táº¡o tier má»›i, auto tier sáº½ bá»‹ áº©n khá»i cÃ¡c API tráº£ vá» (FE chá»‰ hiá»ƒn thá»‹ tier tháº­t).

**VÃ­ dá»¥ request táº¡o tier:**

```json

{

  "seatMapId": 123,

  "name": "VIP",

  "tierType": "VIP",

  "price": 1200000,

  "quantityTotal": 50,

  "colorCode": "#FFD700",

  "saleStart": "2026-06-20T19:00:00",

  "saleEnd": "2026-06-20T22:00:00"

}

```

#### LÆ°u Ã½ UI/FE:

- LuÃ´n validate cÃ¡c trÆ°á»ng báº¯t buá»™c trÆ°á»›c khi gá»­i request.

- Náº¿u event chÆ°a cÃ³ tier, UI nÃªn nháº¯c organizer táº¡o tier Ä‘á»ƒ cÃ³ thá»ƒ bÃ¡n vÃ©.

- áº¨n/disable cÃ¡c action khÃ´ng há»£p lá»‡ theo role vÃ  tráº¡ng thÃ¡i event.

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

### [Bá»” SUNG] Logic voucher platform liÃªn káº¿t PlatformSale

### [Bá»” SUNG] API kiá»ƒm tra tráº¡ng thÃ¡i PlatformSale theo voucher platform

### [Bá»” SUNG] API láº¥y danh sÃ¡ch voucher thuá»™c cÃ¡c platform sale Ä‘ang active

- Endpoint má»›i: `GET /api/admin/platform-sales/active-vouchers` (public, khÃ´ng cáº§n auth)

- Tráº£ vá» list voucher (VoucherResponse) thuá»™c cÃ¡c platform sale Ä‘ang active, FE cÃ³ thá»ƒ dÃ¹ng Ä‘á»ƒ hiá»ƒn thá»‹ trá»±c tiáº¿p trong "My Voucher" hoáº·c cÃ¡c nÆ¡i cáº§n show voucher platform.

- Endpoint má»›i: `GET /api/platform-sales/voucher/{voucherCode}` (public, khÃ´ng cáº§n auth)

- Tráº£ vá» thÃ´ng tin PlatformSale liÃªn káº¿t vá»›i voucherCode (náº¿u cÃ³):

```json

{

  "id": "uuid",

  "name": "Platform Sale ThÃ¡ng 4",

  "description": "Giáº£m giÃ¡ toÃ n há»‡ thá»‘ng thÃ¡ng 4",

  "discountPercentage": 10.0,

  "validFrom": "2026-04-01T00:00:00",

  "validUntil": "2026-04-30T23:59:59",

  "isActive": true,

  "voucherId": "uuid",

  "voucherCode": "PLAT-123456789"

}

```

- FE cÃ³ thá»ƒ gá»i endpoint nÃ y Ä‘á»ƒ kiá»ƒm tra tráº¡ng thÃ¡i PlatformSale cá»§a voucher platform trÆ°á»›c khi cho phÃ©p user sá»­ dá»¥ng.

- Voucher platform (táº¡o qua `/api/admin/vouchers/platform`) chá»‰ usable/hiá»ƒn thá»‹ khi cÃ³ báº£n ghi PlatformSale active liÃªn káº¿t voucher Ä‘Ã³ (liÃªn káº¿t qua khÃ³a ngoáº¡i `platform_sale.voucher_id`).

- Khi gá»i `GET /api/vouchers/me`, backend chá»‰ tráº£ vá» cÃ¡c voucher platform cÃ³ liÃªn káº¿t PlatformSale Ä‘ang active (tráº¡ng thÃ¡i active, cÃ²n háº¡n, chÆ°a bá»‹ disable).

- Khi validate voucher platform (`POST /api/vouchers/validate`), backend kiá»ƒm tra tráº¡ng thÃ¡i PlatformSale liÃªn káº¿t. Náº¿u PlatformSale háº¿t háº¡n hoáº·c bá»‹ disable, voucher sáº½ khÃ´ng usable, tráº£ vá» lá»—i.

- FE khÃ´ng cáº§n gá»i API riÃªng Ä‘á»ƒ láº¥y PlatformSale, chá»‰ cáº§n láº¥y voucher nhÆ° bÃ¬nh thÆ°á»ng qua `/api/vouchers/me`.

- Náº¿u voucher platform khÃ´ng cÃ²n active (PlatformSale háº¿t háº¡n hoáº·c bá»‹ disable), sáº½ khÃ´ng usable/khÃ´ng hiá»ƒn thá»‹ á»Ÿ "My Voucher".

#### Validate voucher before checkout

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

**LÆ°u Ã½:**

- Khi validate voucher platform, backend kiá»ƒm tra tráº¡ng thÃ¡i PlatformSale liÃªn káº¿t. Náº¿u PlatformSale háº¿t háº¡n hoáº·c bá»‹ disable, voucher sáº½ khÃ´ng usable, tráº£ vá» lá»—i cho FE.

#### Get my available vouchers

`GET /api/vouchers/me` (auth required)

Returns all vouchers the current user can use:

  - Personal vouchers assigned to the user (e.g. monthly vouchers).

  - Platform-wide vouchers created by Admin (`applyOn = ALL`, `assignedToUser = null`).

  - Platform-wide vouchers (voucher platform) **chá»‰ tráº£ vá» náº¿u cÃ³ báº£n ghi PlatformSale active liÃªn káº¿t voucher Ä‘Ã³** (`applyOn = ALL`, `assignedToUser = null`, liÃªn káº¿t PlatformSale).

  - Expired, inactive hoáº·c khÃ´ng cÃ³ PlatformSale active sáº½ bá»‹ loáº¡i khá»i danh sÃ¡ch usable voucher.

#### Create event voucher (Organizer or Admin for a specific event)

- `POST /api/vouchers/events/{eventId}`

- Access: `ORGANIZER` or `ADMIN`

- Voucher behavior: `voucherType = ORGANIZER_EVENT`, `applyOn = SPECIFIC_EVENT`.

- Scope: only applies to the given `{eventId}`.

- Request:

```json

{

  "name": "Event Summer Sale",

  "discountType": "PERCENTAGE",

  "discountValue": 10,

  "minOrderValue": 500000,

  "usageLimit": 100,

  "validFrom": "2026-04-06T00:00:00",

  "validUntil": "2026-05-01T00:00:00"

}

```

- Required fields: `name`, `discountType`, `discountValue`, `validFrom`, `validUntil`.

- Optional fields: `minOrderValue`, `usageLimit` (null = unlimited).

- Validation rules:

  - `validUntil` must be after `validFrom`.

  - If `discountType = PERCENTAGE`, `discountValue` must be <= 100.

  - Event must be `PUBLISHED`.

#### Create platform-wide voucher (Admin)

- `POST /api/admin/vouchers/platform`

- Access: `ADMIN` only

- Voucher behavior: `voucherType = PLATFORM`, `applyOn = ALL`, `assignedToUser = null`.

- Scope: can be used on all events (subject to validity window, usage limit, min order value).

- Request:

```json

{

  "name": "Platform April",

  "discountType": "FIXED_AMOUNT",

  "discountValue": 50000,

  "minOrderValue": 200000,

  "usageLimit": 1000,

  "validFrom": "2026-04-06T00:00:00",

  "validUntil": "2026-05-01T00:00:00"

}

```

- Required fields: `name`, `discountType`, `discountValue`, `validFrom`, `validUntil`.

- Optional fields: `minOrderValue`, `usageLimit` (null = unlimited).

- Validation rules: same as event voucher.

- Response includes generated `code` (auto-generated by backend, not in request):

```json

{

  "id": "uuid",

  "code": "VOC-1775123456789",

  "validFrom": "2026-04-06T00:00:00",

  "validUntil": "2026-05-01T00:00:00"

}

```

- FE should display the returned `code` for admin to copy/share.

#### `discountType` values

- `PERCENTAGE`: discount as percent of order subtotal (0â€“100).

- `FIXED_AMOUNT`: discount as fixed VND amount (deducted from subtotal, capped at order amount).

#### Checkout voucher behavior (important)

- `POST /api/checkout/quote` and `POST /api/checkout/orders` use the same voucher validation rules as `POST /api/vouchers/validate`.

- If voucher is invalid for user/event/order context, backend returns `400` with validation message.

- When order is created successfully with a voucher:

  - Backend increments `usedCount` on the voucher (enforces `usageLimit`).

  - Backend records a `VoucherUsage` entry linking voucher, order, and user.

  - Concurrent checkout is protected by row-level lock to prevent exceeding usage limit.

- FE does not need to call any extra "consume" API â€” it happens automatically inside order creation.

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

- "Mark Used" button â†’ `POST /api/tickets/{id}/use`

- "Download" button â†’ `GET /api/tickets/{id}/download` then generate PDF

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

  

**Voucher platform:**

- Chá»‰ usable/hiá»ƒn thá»‹ khi cÃ³ báº£n ghi PlatformSale active liÃªn káº¿t voucher Ä‘Ã³. FE khÃ´ng cáº§n xá»­ lÃ½ gÃ¬ thÃªm, chá»‰ cáº§n láº¥y danh sÃ¡ch voucher nhÆ° bÃ¬nh thÆ°á»ng qua `/api/vouchers/me`.

- Náº¿u voucher platform khÃ´ng cÃ²n active (PlatformSale háº¿t háº¡n hoáº·c bá»‹ disable), sáº½ khÃ´ng usable/khÃ´ng hiá»ƒn thá»‹ á»Ÿ "My Voucher".

## 12. Frontend Optimization Tips (LCP/Performance)

Äá»ƒ Ä‘áº¡t Ä‘Æ°á»£c chá»‰ sá»‘ **LCP < 2.5s** vá»›i lÆ°á»£ng dá»¯ liá»‡u lá»›n (5000+ events), FE cáº§n tuÃ¢n thá»§ cÃ¡c quy táº¯c sau:

### 12.1 Image Optimization (Cá»°C Ká»² QUAN TRá»ŒNG)

- **Sá»­ dá»¥ng Thumbnail:** KhÃ´ng bao giá» load áº£nh gá»‘c trÃªn trang danh sÃ¡ch. Backend hiá»‡n tráº£ vá» áº£nh `400x250` cho `imageUrl` Ä‘á»ƒ tá»‘i Æ°u SEO vÃ  LCP.

- **Lazy Loading:** Ãp dá»¥ng `loading="lazy"` cho táº¥t cáº£ áº£nh sá»± kiá»‡n náº±m ngoÃ i mÃ n hÃ¬nh Ä‘áº§u tiÃªn (viewport).

- **Fetch Priority:** Vá»›i áº£nh cá»§a Sá»± kiá»‡n Ä‘áº§u tiÃªn (Event ID Ä‘áº§u tiÃªn trÃªn List), hÃ£y dÃ¹ng `fetchpriority="high"` Ä‘á»ƒ trÃ¬nh duyá»‡t Æ°u tiÃªn táº£i áº£nh nÃ y ngay láº­p tá»©c.

### 12.2 Network & Data

- **Gzip Compression:** Backend Ä‘Ã£ báº­t nÃ©n. FE hÃ£y Ä‘áº£m báº£o cÃ¡c request luÃ´n cÃ³ header `Accept-Encoding: gzip, deflate, br` (TrÃ¬nh duyá»‡t thÆ°á»ng tá»± Ä‘á»™ng lÃ m viá»‡c nÃ y).

- **Avoid Over-fetching:** Chá»‰ gá»i API Search khi user Ä‘Ã£ nháº­p Ã­t nháº¥t 2 kÃ½ tá»± vÃ  sá»­ dá»¥ng **Debounce (300-500ms)** Ä‘á»ƒ trÃ¡nh spam request lÃ m treo server.

- **Skeleton Screens:** Sá»­ dá»¥ng Skeleton loading thay cho Spinner Ä‘á»ƒ tÄƒng tráº£i nghiá»‡m ngÆ°á»i dÃ¹ng ("Perceived Performance").

### 12.3 Pagination Strategy

- **Size cá»‘ Ä‘á»‹nh:** Khuyáº¿n nghá»‹ dÃ¹ng `size=20`. KhÃ´ng nÃªn set `size > 50` vÃ¬ sáº½ lÃ m tÄƒng dung lÆ°á»£ng JSON vÃ  lÃ m cháº­m DOM rendering trÃªn trÃ¬nh duyá»‡t cÅ©.

- **Prefetching:** Khi user hover vÃ o nÃºt "Trang tiáº¿p theo", FE cÃ³ thá»ƒ thá»±c hiá»‡n pre-fetch dá»¯ liá»‡u trang Ä‘Ã³ Ä‘á»ƒ tráº£i nghiá»‡m chuyá»ƒn trang diá»…n ra tá»©c thÃ¬.

## 13. Non-Functional Requirements (NFR)

Để đảm bảo hệ thống vận hành ổn định với quy mô dữ liệu lớn (Big Data), Backend cam kết và yêu cầu Frontend tuân thủ các tiêu chuẩn sau:

- **Scalability (Khả năng mở rộng):** Hệ thống hỗ trợ quản lý đồng thời trên 5,000+ sự kiện với logic tính toán hạng vé và tồn kho phức tạp mà không làm tăng độ trễ API.
- **Latency (Độ trễ):** Thời gian phản hồi trung bình (TTFB) cho các yêu cầu lấy danh sách đã phân trang phải dưới **200ms** tại phía Server.
- **Consistency (Tính nhất quán):** Dữ liệu trả về qua API phải luôn tuân thủ cấu trúc PageResponse chuẩn để Frontend có thể sử dụng các thư viện UI Kit phân trang một cách đồng bộ.
- **Concurrency (Xử lý đồng thời):** Hệ thống có cơ chế hàng đợi và khóa (locking) để xử lý kịch bản tranh chấp vé (contention) khi có hàng ngàn yêu cầu mua vé cùng lúc.
- **Memory Efficiency:** Backend áp dụng cơ chế giải phóng bộ nhớ (flush/clear) và Batch Fetching giúp ứng dụng hoạt động ổn định trên các môi trường có RAM giới hạn (từ 2GB trở lên).
