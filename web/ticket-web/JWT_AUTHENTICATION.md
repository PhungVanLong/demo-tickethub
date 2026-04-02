# JWT Authentication Implementation — Admin Login

## Overview

Comprehensive JWT authentication system implemented with proper token validation, expiration checking, and refresh token mechanism.

## Files Created

### 1. `src/utils/jwt.js` — JWT Token Utilities
**Purpose**: Centralized JWT token handling with validation

**Key Functions**:
- `decodeToken(token)` - Decode JWT payload
- `isTokenExpired(token)` - Check if token is expired (with 1min buffer)
- `getTokenExpiresIn(token)` - Get seconds until expiration
- `getTokenUser(token)` - Extract user info from JWT
- `verifyToken(token)` - Verify token structure and expiration
- `extractTokens(data)` - Extract access + refresh tokens from API response

**Token Expiration**: Considers token expired 1 minute before actual expiration for safety

## Files Modified

### 2. `src/services/auth.service.js`
**Changes**:
- Added `refreshToken(refreshToken)` - POST /api/auth/refresh
- Added `logout()` - POST /api/auth/logout to invalidate token on backend
- Updated JSDoc to specify JWT payload requirements

**Expected JWT Payload**:
```json
{
  "sub": "user-id",
  "email": "user@example.com",
  "role": "ADMIN|ORGANIZER|CUSTOMER",
  "exp": 1234567890,
  "iat": 1234567800
}
```

### 3. `src/services/api.js` — Auto Token Refresh
**Changes**:
- **Request Interceptor**: 
  - Auto-checks token expiration before each request
  - Auto-refreshes token if expired (with queue mechanism)
  - Handles multiple concurrent requests during refresh
- **Response Interceptor**: 
  - 401 → clear tokens + redirect to /login

**Key Feature**: Multiple requests won't trigger multiple refresh calls

### 4. `src/stores/auth.store.js` — Enhanced Authentication
**New State**:
- `refreshToken` - Store refresh token
- `tokenValidated` - Track if JWT was validated

**New Functions**:
- `validateToken()` - Check JWT expiration + structure
- `refreshAccessToken()` - Use refresh token to get new access token

**Enhanced Role Verification**:
```javascript
const isAdmin = computed(() => {
    // Verify BOTH from user object AND JWT payload
    const userRoleValid = user.value?.role === 'ADMIN'
    const tokenRoleValid = decodeToken(token.value)?.role === 'ADMIN'
    return userRoleValid && tokenRoleValid
})
```

**Changes**:
- `login()` - Verify token before storing + extract user from JWT
- `register()` - Same JWT validation
- `fetchMe()` - Validate token first, then fetch
- `logout()` - Call backend logout + clear refresh token

### 5. `src/router/index.js` — JWT-Protected Routes
**Changes**:
- Validate JWT before allowing access to protected routes
- Added `tokenValidated` check to prevent duplicate validation
- Admin route now verifies JWT role payload
- Added logging for admin access denials

**Protection Flow**:
```
requiresAuth/Admin/Organizer route
    ↓
Check token validity
    ↓
Verify JWT expiration + structure
    ↓
Verify role from JWT payload
    ↓
Allow or redirect to login/home
```

## Security Features Implemented

### ✅ Token Expiration Check
- Validates `exp` claim before each API request
- 1-minute buffer before actual expiration
- Auto-logout if token expired and no refresh token

### ✅ Refresh Token Mechanism
- Supports refresh token rotation
- Concurrent request queueing during refresh
- Automatic token refresh before expiration

### ✅ JWT Decode Validation
- Verifies token structure (not just string presence)
- Extracts user claims: `sub`, `email`, `role`, `exp`, `iat`
- Double-checks role from JWT payload (not just API response)

### ✅ Admin Role Verification
- Verifies admin role against both user object and JWT payload
- Prevents privilege escalation if API is compromised
- Router guard validates JWT before allowing /admin access

### ✅ Auto-Logout on Expiration
- Invalid tokens → clear localStorage + redirect to /login
- 401 response → same behavior
- Refresh failure → logout required

## Backend Requirements

### Login Endpoint: `POST /api/auth/login`

**Request**:
```json
{ "email": "admin@example.com", "password": "..." }
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "admin-123",
    "email": "admin@example.com",
    "name": "Admin User",
    "role": "ADMIN"
  }
}
```

### Refresh Endpoint: `POST /api/auth/refresh`

**Request**:
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiIs..." }
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..." (optional, if rotating)
}
```

### Logout Endpoint: `POST /api/auth/logout` (optional)

Frontend will call this when user logs out to invalidate backend session.

## JWT Token Structure Requirements

### Minimum Claims
```javascript
{
  "sub": "user-id",              // Subject (user ID)
  "email": "user@example.com",   // Email
  "role": "ADMIN",               // Role: ADMIN|ORGANIZER|CUSTOMER
  "exp": 1704067200,             // Expiration (Unix timestamp in seconds)
  "iat": 1704063600              // Issued At (Unix timestamp in seconds)
}
```

### Example JWT Payload (decoded)
```javascript
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "admin@tickethub.com",
  "role": "ADMIN",
  "exp": Math.floor(Date.now() / 1000) + 3600,  // 1 hour from now
  "iat": Math.floor(Date.now() / 1000),
  "iss": "tickethub",
  "aud": "tickethub-web"
}
```

## Testing Checklist

### Admin Login Flow
- [ ] Admin logs in with email + password
- [ ] Backend returns JWT token with `role: ADMIN`
- [ ] Token is stored in localStorage as `auth_token`
- [ ] User is redirected to /dashboard (admin page)
- [ ] Admin can access `/admin` route

### Token Expiration Handling
- [ ] Create JWT with short expiration (e.g., 5 minutes)
- [ ] Wait for token to expire
- [ ] Make API request after expiration
- [ ] Should auto-refresh using refreshToken
- [ ] Request succeeds with new token

### Refresh Token Flow
- [ ] Login stores both `auth_token` and `refresh_token`
- [ ] Token refresh endpoint returns new `auth_token`
- [ ] Stale refresh token should cause logout
- [ ] Multiple concurrent requests during refresh → resolved correctly

### Privilege Escalation Prevention
- [ ] Non-admin user can't access /admin route
- [ ] User role mismatch (API shows ADMIN but JWT shows CUSTOMER) → denied
- [ ] JWT `exp` claim is validated before allowing access

### Auto-Logout Scenarios
- [ ] Invalid token format → logout
- [ ] Expired token without refresh token → logout
- [ ] 401 response from backend → logout
- [ ] localStorage cleared → redirect to /login

## Usage in Components

### Check Admin Status
```javascript
import { useAuthStore } from '@/stores/auth'

export default {
  setup() {
    const auth = useAuthStore()
    
    // isAdmin now checks both user.role AND JWT payload
    console.log(auth.isAdmin)  // boolean
  }
}
```

### Manually Validate Token
```javascript
const auth = useAuthStore()

// Before accessing admin features
if (await auth.validateToken()) {
  // Token is valid, refresh if needed
} else {
  // Token invalid, user logged out
}
```

### Check Token Expiration
```javascript
import { isTokenExpired, getTokenExpiresIn } from '@/utils/jwt'

const token = localStorage.getItem('auth_token')
console.log(isTokenExpired(token))  // boolean
console.log(getTokenExpiresIn(token))  // seconds remaining
```

## Build Result

```
✓ 137 modules transformed
dist/index-fSopBky_.js: 183.07 kB (gzip: 68.20 kB)
✓ built in 2.86s
```

All changes compiled successfully!
