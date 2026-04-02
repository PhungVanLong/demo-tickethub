# Voucher & Platform Sale System - Update for Frontend

**Release Date:** April 2, 2026  
**Version:** 1.0.0  
**Status:** Ready for Integration

---

## Overview

Hệ thống voucher mới bao gồm 3 tính năng chính:

1. **Monthly Voucher Allocation** - Hàng tháng mỗi user tự động được N vouchers (default = 2)
2. **Platform-Wide Sales** - Admin cấu hình sale % chung cho toàn sàn
3. **Organizer Event Vouchers** - Organizer tạo vouchers cho các event đã duyệt

---

## 1. Monthly Voucher Allocation

### Cơ chế
- Mỗi user tự động nhận **2 vouchers/tháng** (cấu hình: `voucher.monthly.quantity`)
- Voucher có **discount 10%** (cấu hình: `voucher.monthly.discount-percentage`)
- Tự **hết hạn vào 23:59:59 ngày cuối tháng**
- **Scheduled job** chạy mỗi ngày 1/tháng lúc 00:00 (cập nhật vouchers cho tất cả users)

### API

#### Get My Vouchers
```
GET /api/vouchers/me
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": "uuid-1",
    "code": "MONTHLY-202604-user-id-0",
    "voucherType": "USER_MONTHLY",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "validFrom": "2026-04-01T00:00:00",
    "validUntil": "2026-04-30T23:59:59",
    "isActive": true,
    "applyOn": "ALL",
    "usageLimit": 1,
    "usedCount": 0
  },
  {
    "id": "uuid-2",
    "code": "MONTHLY-202604-user-id-1",
    "voucherType": "USER_MONTHLY",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "validFrom": "2026-04-01T00:00:00",
    "validUntil": "2026-04-30T23:59:59",
    "isActive": true,
    "applyOn": "ALL",
    "usageLimit": 1,
    "usedCount": 0
  }
]
```

### UI Implementation
- Hiển thị các monthly vouchers trong **section "My Vouchers"** hoặc **"Special Offers"**
- Cho phép user xem voucher code (có thể copy to clipboard)
- Hiển thị thời gian hết hạn đỏ nếu còn < 3 ngày
- Nút "Apply Voucher" khi checkout

---

## 2. Platform-Wide Sales

### Cơ chế
- Admin tạo **sale chung cho toàn sàn** (% discount)
- Có thể có **nhiều sales cùng lúc** (hệ thống tự chọn % cao nhất khi thanh toán)
- Có **thời gian hiệu lực** (validFrom - validUntil)
- State: **Active (đang áp dụng) / Inactive (hết hạn)**

### Admin APIs

#### Create Platform Sale
```
POST /api/admin/platform-sales
Authorization: Bearer {admin-token}
Content-Type: application/json

{
  "name": "Summer Sale 2026",
  "description": "50% off all events",
  "discountPercentage": 50,
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-08-31T23:59:59"
}
```

**Response:**
```json
{
  "id": "uuid",
  "name": "Summer Sale 2026",
  "description": "50% off all events",
  "discountPercentage": 50,
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-08-31T23:59:59",
  "isActive": true,
  "createdAt": "2026-04-02T10:30:00",
  "updatedAt": "2026-04-02T10:30:00"
}
```

#### Get Active Sales
```
GET /api/admin/platform-sales/active
Authorization: Bearer {admin-token}
```

**Response:** Array of active sales

#### Get All Sales
```
GET /api/admin/platform-sales?isActive=true
Authorization: Bearer {admin-token}
```

#### Deactivate Sale
```
DELETE /api/admin/platform-sales/{saleId}
Authorization: Bearer {admin-token}
```

### Public API
```
GET /api/admin/platform-sales/active
(No auth required)
```

### UI Implementation (Admin)
- Dashboard có mục **"Platform Sales Management"**
- Form tạo sale mới: name, description, %, ngày bắt đầu, ngày kết thúc
- List sales: hiển thị trạng thái (Active/Inactive/Expired), timeline
- Search & filter by status, date range
- Nút deactivate/edit

### UI Implementation (Customer)
- Hiển thị **"Platform sale banner"** ở homepage khi có active sales
- Trong cart/checkout: hiển thị platform sale % (tự động apply)
- Nếu có nhiều sales: chọn cái có % cao nhất

---

## 3. Organizer Event Vouchers

### Cơ chế
- Organizer tạo vouchers cho **từng event đã PUBLISHED**
- Hỗ trợ 2 discount types:
  - **PERCENTAGE**: % discount
  - **FIXED_AMOUNT**: số tiền cố định
- Có thể set **min order value**, **usage limit**
- Voucher tự **hết hạn** khi validUntil pass

### API

#### Create Event Voucher
```
POST /api/vouchers/events/{eventId}
Authorization: Bearer {organizer-token}
Content-Type: application/json

{
  "name": "Summer Special",
  "discountType": "PERCENTAGE",
  "discountValue": 20,
  "minOrderValue": 500000,
  "usageLimit": 100,
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-06-30T23:59:59"
}
```

**Response:**
```json
{
  "voucherId": "uuid",
  "code": "VOC-1712141000",
  "validFrom": "2026-06-01T00:00:00",
  "validUntil": "2026-06-30T23:59:59"
}
```

#### Get Organizer's Vouchers
```
GET /api/vouchers/organizer
Authorization: Bearer {organizer-token}
```

**Response:**
```json
[
  {
    "id": "uuid",
    "code": "VOC-1712141000",
    "voucherType": "ORGANIZER_EVENT",
    "discountType": "PERCENTAGE",
    "discountValue": 20,
    "minOrderValue": 500000,
    "usageLimit": 100,
    "usedCount": 5,
    "validFrom": "2026-06-01T00:00:00",
    "validUntil": "2026-06-30T23:59:59",
    "eventId": 15,
    "isActive": true
  }
]
```

### UI Implementation (Organizer)
- Trong **Event Management** → **Promotions/Vouchers** section
- Event list có mục **"Manage Vouchers"**
- Form tạo voucher: 
  - Discount type (% hoặc fixed amount)
  - Discount value
  - Min order value (optional)
  - Usage limit
  - Valid period
- List vouchers của event: code, discount, validity, usage stats (5/100 used)
- Nút copy code, edit, delete

---

## Database Schema Changes

### New Tables
- `platform_sales` - Cấu hình sale toàn sàn
- `monthly_voucher_allocations` - Tracking monthly allocation

### Updated Columns (vouchers table)
```sql
ALTER TABLE vouchers ADD COLUMN voucher_type VARCHAR(50);
ALTER TABLE vouchers ADD COLUMN event_id BIGINT;
ALTER TABLE vouchers ADD COLUMN assigned_to_user UUID;
ALTER TABLE vouchers ADD COLUMN created_by UUID NOT NULL;
```

---

## Configuration (application.properties)

```properties
# Voucher configuration
voucher.monthly.quantity=2
voucher.monthly.discount-percentage=10

# Scheduled tasks
spring.task.scheduling.pool.size=2
spring.task.scheduling.thread-name-prefix=scheduled-
```

### Scheduled Jobs
- **Monthly Allocation**: Ngày 1/tháng 00:00 (tạo N vouchers cho mỗi user)
- **Voucher Expiration**: Mỗi ngày 01:00 (disable các vouchers hết hạn)

---

## Validation Rules

### Monthly Vouchers
- Auto-created monthly (no user action needed)
- Usage limit: **1 per voucher** (1-time use)
- Discount type: **PERCENTAGE** only (10% fixed)
- Apply on: **ALL** (toàn sàn)
- Can't be edited/deleted by user

### Platform Sales
- `discountPercentage`: 0.01 - 100.00 (decimal)
- `validFrom` < `validUntil`
- Only **1 platform sale** applied per order (highest %)

### Organizer Vouchers
- Only for **PUBLISHED** events
- Support both **PERCENTAGE** & **FIXED_AMOUNT**
- `discountValue` must be positive
- `validUntil` > `validFrom`
- `usageLimit` >= actual usages

---

## Integration Checklist

### Frontend Tasks
- [ ] Add "My Vouchers" page (list monthly vouchers + organizer vouchers)
- [ ] Voucher copy-to-clipboard in checkout
- [ ] Show platform sale banner on homepage
- [ ] Auto-apply best platform sale in cart
- [ ] Organizer: Add voucher management in event dashboard
- [ ] Organizer: Form to create new event vouchers
- [ ] Admin: Platform sales management dashboard
- [ ] Admin: Create/deactivate sales form
- [ ] Update checkout flow to include voucher selection
- [ ] Show voucher savings in order summary

### Backend Status
- ✅ Entities created
- ✅ Repositories implemented
- ✅ Services + scheduled jobs ready
- ✅ APIs (CRUD) ready
- ✅ Security rules updated
- ✅ Build validated

---

## Notes

- Voucher codes auto-generated in format: `VOC-{timestamp}` or `MONTHLY-{YYYYMM}-{userId}-{index}`
- Monthly vouchers can be customized via `application.properties`
- Platform sales prioritize **highest discount %** when multiple active
- All vouchers auto-disable when `validUntil` passes (via scheduled job)
- User can have multiple active vouchers (from monthly + organizer)

---

## Support

For questions, contact backend team or check API docs: `/api/docs`
