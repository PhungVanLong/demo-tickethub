# Event Creation Info

This document summarizes what is needed to create an event from frontend to backend.

## 1. Endpoint and Auth

- Endpoint: `POST /api/events`
- Base URL: `http://localhost:8081/api`
- Auth: Bearer token required
- Allowed roles (current backend): `CUSTOMER`, `ORGANIZER`, `ADMIN`

## 2. Required Fields (CreateEventRequest)

Minimum required fields from backend DTO:

- `title` (string, required, max 255)
- `venue` (string, required, max 255)
- `city` (string, required, max 100)
- `startTime` (LocalDateTime, required)
- `endTime` (LocalDateTime, required)

Recommended optional fields:

- `category` (string, max 100)
- `description` (string)
- `country` (string, max 100)
- `locationCoords` (string, max 100)
- `bannerUrl` (string, max 500)
- `imageUrl` (string, max 500)
- `featured` (boolean)
- `tags` (array of strings)

## 3. Datetime Format

Use ISO-like datetime accepted by Spring `LocalDateTime`.

Examples:

- `2026-06-15T19:30:00`
- `2026-06-15T22:00:00`

Note: `datetime-local` input from browser usually produces this format and is valid.

## 4. Payload Example

```json
{
  "title": "Live Concert 2026",
  "category": "Concert",
  "description": "Large scale live show",
  "venue": "My Dinh National Stadium",
  "city": "Hanoi",
  "country": "Vietnam",
  "locationCoords": "21.0285,105.8542",
  "startTime": "2026-06-15T19:30:00",
  "endTime": "2026-06-15T22:00:00",
  "bannerUrl": "https://your-cdn.com/banner.jpg",
  "imageUrl": "https://your-cdn.com/thumb.jpg",
  "featured": true,
  "tags": ["Pop", "Live", "Outdoor"]
}
```

## 5. Typical Full Flow After Event Created

If you use multi-step organizer flow:

1. Create event
   - `POST /api/events`
2. Create seat map
   - `POST /api/events/{eventId}/seat-maps`
3. Create ticket tiers
   - `POST /api/events/{eventId}/seat-maps/{seatMapId}/tiers`

## 6. Seat Map Request Example

```json
{
  "name": "Main Hall",
  "layoutJson": "{\"zones\":[{\"name\":\"A\",\"rows\":10}]}",
  "imageUrl": "https://your-cdn.com/seatmap.jpg",
  "totalRows": 20,
  "totalCols": 30
}
```

## 7. Ticket Tier Request Example

```json
{
  "name": "VIP",
  "tierType": "VIP",
  "price": 3500000,
  "quantityTotal": 500,
  "colorCode": "#f59e0b",
  "saleStart": "2026-04-01T00:00:00",
  "saleEnd": "2026-06-15T18:00:00"
}
```

## 8. Common Validation Notes

- `title`, `venue`, `city`, `startTime`, `endTime` must not be empty.
- `endTime` should be after `startTime`.
- URL fields should be valid and reachable (avoid placeholders like `https://example.com/banner.jpg`).
- `quantityTotal` must be >= 1.
- `price` must be >= 0.

## 9. Frontend Mapping Note

Current organizer form uses field `bannerUrl` only. Backend also supports `imageUrl`.
If needed, add a separate input for thumbnail (`imageUrl`) to improve event card images.
