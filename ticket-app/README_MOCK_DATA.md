# Backend Mock Data Seed Guide

Tai lieu nay huong dan seed du lieu mock vao PostgreSQL cho backend `ticket-app` bang API (khuyen nghi), khong can viet SQL tay.

## 1) Dieu kien can

- Java 17
- Maven Wrapper (`mvnw.cmd`)
- PostgreSQL dang chay local
- DB da tao: `ticket-app`

Cau hinh hien tai doc tu `src/main/resources/application.properties`:

- URL: `jdbc:postgresql://localhost:5432/ticket-app`
- User: `postgres`
- Password: `270504`
- Port backend: `8081`

## 2) Khoi dong backend

Tai thu muc backend:

```powershell
cd D:\ticket\ticket-app
.\mvnw.cmd spring-boot:run
```

Khi app start lan dau, `DataInitializer` tu tao tai khoan admin:

- Email: `admin@tickethub.com`
- Password: `Admin@123456`

## 3) Seed mock data bang PowerShell (API-first)

Mo terminal PowerShell moi, chay block sau:

```powershell
$base = "http://localhost:8081/api"

# ---------- Helpers ----------
function PostJson($url, $body, $token = $null) {
  $headers = @{ "Content-Type" = "application/json" }
  if ($token) { $headers["Authorization"] = "Bearer $token" }
  return Invoke-RestMethod -Method POST -Uri $url -Headers $headers -Body ($body | ConvertTo-Json -Depth 10)
}

function GetJson($url, $token = $null) {
  $headers = @{}
  if ($token) { $headers["Authorization"] = "Bearer $token" }
  return Invoke-RestMethod -Method GET -Uri $url -Headers $headers
}

# ---------- 1) Register users ----------
$organizer = PostJson "$base/auth/register" @{
  email    = "organizer1@tickethub.com"
  password = "Organizer@123"
  fullName = "Organizer One"
  phone    = "0900000001"
}

$customer = PostJson "$base/auth/register" @{
  email    = "customer1@tickethub.com"
  password = "Customer@123"
  fullName = "Customer One"
  phone    = "0900000002"
}

# ---------- 2) Login ----------
$orgLogin = PostJson "$base/auth/login" @{
  email    = "organizer1@tickethub.com"
  password = "Organizer@123"
}
$orgToken = $orgLogin.accessToken
$orgUserId = $orgLogin.userId

$custLogin = PostJson "$base/auth/login" @{
  email    = "customer1@tickethub.com"
  password = "Customer@123"
}
$custToken = $custLogin.accessToken
$custUserId = $custLogin.userId

# ---------- 3) Create event (authenticated) ----------
$event = PostJson "$base/events" @{
  title          = "Coldplay Mock Live 2026"
  category       = "Concert"
  description    = "Mock concert for frontend integration"
  venue          = "My Dinh National Stadium"
  city           = "Hanoi"
  country        = "Vietnam"
  locationCoords = "21.0285,105.8542"
  startTime      = "2026-06-15T19:30:00"
  endTime        = "2026-06-15T22:00:00"
  bannerUrl      = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=1600&q=80"
  imageUrl       = "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&q=80"
  featured       = $true
  tags           = @("Pop", "Rock", "Live")
} $orgToken
$eventId = $event.id

# ---------- 4) Create seat map ----------
$seatMap = PostJson "$base/events/$eventId/seat-maps" @{
  name      = "Main Hall"
  totalRows = 20
  totalCols = 30
  imageUrl  = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=1200&q=80"
  layoutJson = "{\"zones\":[{\"name\":\"A\",\"rows\":10}],\"version\":1}"
} $orgToken
$seatMapId = $seatMap.id

# ---------- 5) Create ticket tiers ----------
$tierGeneral = PostJson "$base/events/$eventId/seat-maps/$seatMapId/tiers" @{
  name          = "General Admission"
  tierType      = "GENERAL"
  price         = 1200000
  quantityTotal = 5000
  colorCode     = "#6366f1"
  saleStart     = "2026-04-01T00:00:00"
  saleEnd       = "2026-06-15T18:00:00"
} $orgToken

$tierVip = PostJson "$base/events/$eventId/seat-maps/$seatMapId/tiers" @{
  name          = "VIP"
  tierType      = "VIP"
  price         = 3500000
  quantityTotal = 500
  colorCode     = "#f59e0b"
  saleStart     = "2026-04-01T00:00:00"
  saleEnd       = "2026-06-15T18:00:00"
} $orgToken

# ---------- 6) Optional: customer creates order ----------
# Lay tier tu endpoint checkout
$tiers = GetJson "$base/checkout/events/$eventId/tiers"
$firstTierId = $tiers[0].ticketTierId
if (-not $firstTierId) { $firstTierId = $tiers[0].id }

# Luu y: branch hien tai co the yeu cau userId trong quote
try {
  $quote = PostJson "$base/checkout/quote" @{
    userId  = $custUserId
    eventId = $eventId
    items   = @(@{ ticketTierId = $firstTierId; quantity = 2 })
  } $custToken
  Write-Host "Quote OK:" ($quote | ConvertTo-Json -Depth 5)
} catch {
  Write-Host "Quote skip/fail:" $_.Exception.Message
}

$order = PostJson "$base/checkout/orders" @{
  eventId = $eventId
  items   = @(@{ ticketTierId = $firstTierId; quantity = 2 })
} $custToken

$orderId = $order.id
$paymentIntent = PostJson "$base/checkout/orders/$orderId/payments" @{
  method    = "CARD"
  returnUrl = "http://localhost:5173/payment-result"
  cancelUrl = "http://localhost:5173/checkout"
} $custToken

Write-Host "Seed done"
Write-Host "Event ID:" $eventId
Write-Host "SeatMap ID:" $seatMapId
Write-Host "Order ID:" $orderId
Write-Host "Payment code:" $paymentIntent.paymentCode
```

## 4) Verify nhanh

```powershell
# Public list
Invoke-RestMethod -Method GET -Uri "http://localhost:8081/api/events/published"

# Public detail
Invoke-RestMethod -Method GET -Uri "http://localhost:8081/api/events/<eventId>"

# Checkout tiers
Invoke-RestMethod -Method GET -Uri "http://localhost:8081/api/checkout/events/<eventId>/tiers"
```

## 5) Reset du lieu (dev only)

Cach nhanh de reset toan bo schema:

```sql
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

Sau do restart app va chay lai script seed o muc 3.

## 6) Luu y quan trong

- Backend dang dung `spring.jpa.hibernate.ddl-auto=update`, khong dung migration SQL mac dinh.
- Seeding qua API la cach an toan de tu dong ton trong validation va business rules.
- `POST /api/events` da mo cho `CUSTOMER | ORGANIZER | ADMIN` neu da login.
- `GET /api/dashboard` can token, tra stats theo role nguoi dang nhap.
