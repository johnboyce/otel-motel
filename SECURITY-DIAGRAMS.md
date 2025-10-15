# Security Flow Diagrams

Visual representations of the authentication and authorization flow in otel-motel.

## 1. Complete Authentication Flow

```
┌──────────────┐
│   Client     │
│   (Bruno)    │
└──────┬───────┘
       │
       │ Step 1: User selects environment
       │ (User or Admin)
       │
       ▼
┌──────────────────────────────────────┐
│   Bruno Pre-Request Script           │
│   - Checks auth_enabled               │
│   - Reads username & password         │
└──────┬───────────────────────────────┘
       │
       │ Step 2: POST /token
       │ grant_type=password
       │ username=user1
       │ password=password
       ▼
┌──────────────────────────────────────┐
│   Keycloak                           │
│   - Validates credentials             │
│   - Checks user exists                │
│   - Retrieves user roles              │
│   - Generates JWT token               │
└──────┬───────────────────────────────┘
       │
       │ Step 3: Returns JWT
       │ { "access_token": "eyJ..." }
       │
       ▼
┌──────────────────────────────────────┐
│   Bruno Pre-Request Script           │
│   - Stores token in environment       │
│   - Sets {{access_token}} variable    │
└──────┬───────────────────────────────┘
       │
       │ Step 4: GraphQL Request
       │ Authorization: Bearer eyJ...
       │ { query: "{ hotels { id name } }" }
       ▼
┌──────────────────────────────────────┐
│   Quarkus Application                │
│   Step 5: Security Filter            │
│   - Extracts JWT from header          │
│   - Validates signature               │
│   - Checks expiry                     │
│   - Extracts claims (roles)           │
└──────┬───────────────────────────────┘
       │
       │ Step 6: Authorization Check
       │
       ▼
┌──────────────────────────────────────┐
│   GraphQL Endpoint                   │
│   @RolesAllowed({"user", "admin"})   │
│   - Checks user has required role     │
│   - Allows if role matches            │
│   - Returns 403 if not authorized     │
└──────┬───────────────────────────────┘
       │
       │ Step 7: Execute Query
       │
       ▼
┌──────────────────────────────────────┐
│   Service Layer                      │
│   - Executes business logic           │
│   - Queries database                  │
└──────┬───────────────────────────────┘
       │
       │ Step 8: Return Response
       │ { data: { hotels: [...] } }
       │
       ▼
┌──────────────┐
│   Client     │
│   (Bruno)    │
└──────────────┘
```

## 2. Token Structure

```
JWT Token: eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSJ9.signature
           ─────────────────────────────────────────────────────────────────
                   │                    │                │
                   ▼                    ▼                ▼
              ┌────────┐         ┌──────────┐      ┌────────────┐
              │ Header │         │ Payload  │      │ Signature  │
              └────────┘         └──────────┘      └────────────┘
                  │                    │                  │
                  │                    │                  │
                  ▼                    ▼                  ▼
          {                    {                    RSASHA256(
            "alg": "RS256",      "sub": "user1",      base64(header) +
            "typ": "JWT"         "iss": "keycloak",   "." +
          }                      "exp": 1234567890,   base64(payload),
                                 "realm_access": {    privateKey
                                   "roles": ["user"]  )
                                 }
                               }
```

## 3. Role-Based Access Decision Flow

```
                    ┌───────────────┐
                    │ Request Arrives│
                    └───────┬────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Extract JWT   │
                    │ from Header   │
                    └───────┬────────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ Validate JWT  │
                    │ Signature &   │
                    │ Expiry        │
                    └───────┬────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
                ▼                       ▼
        ┌───────────┐           ┌───────────┐
        │  Invalid  │           │   Valid   │
        └─────┬─────┘           └─────┬─────┘
              │                       │
              ▼                       ▼
        ┌───────────┐         ┌───────────────┐
        │ Return    │         │ Extract Roles │
        │ 401       │         │ from Token    │
        │ Unauthorized        └───────┬────────┘
        └───────────┘                 │
                                      ▼
                            ┌───────────────────┐
                            │ Check Endpoint    │
                            │ @RolesAllowed     │
                            │ Annotation        │
                            └───────┬───────────┘
                                    │
                        ┌───────────┴───────────┐
                        │                       │
                        ▼                       ▼
                ┌──────────────┐       ┌──────────────┐
                │ @PermitAll   │       │ @RolesAllowed│
                └──────┬───────┘       └──────┬───────┘
                       │                      │
                       ▼                      ▼
                ┌──────────────┐     ┌────────────────┐
                │ Allow Access │     │ Check User Has │
                │              │     │ Required Role  │
                └──────┬───────┘     └────────┬───────┘
                       │                      │
                       │          ┌───────────┴───────────┐
                       │          │                       │
                       │          ▼                       ▼
                       │    ┌──────────┐           ┌──────────┐
                       │    │ Has Role │           │ No Role  │
                       │    └────┬─────┘           └────┬─────┘
                       │         │                      │
                       │         ▼                      ▼
                       │   ┌──────────┐          ┌──────────┐
                       └──>│ Execute  │          │ Return   │
                           │ Query    │          │ 403      │
                           └──────────┘          │ Forbidden│
                                                 └──────────┘
```

## 4. Environment Selection Flow

```
Developer Opens Bruno
         │
         ▼
┌────────────────────┐
│ Select Environment │
└────────┬───────────┘
         │
         ├──────────────────────┬──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ Development    │   │ User           │   │ Admin          │
│ (No Auth)      │   │ (user role)    │   │ (admin role)   │
└────────┬───────┘   └────────┬───────┘   └────────┬───────┘
         │                    │                    │
         │                    │                    │
         ▼                    ▼                    ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ auth_enabled:  │   │ auth_enabled:  │   │ auth_enabled:  │
│ false          │   │ true           │   │ true           │
│                │   │ username:user1 │   │ username:admin1│
│                │   │ password:pwd   │   │ password:admin │
└────────┬───────┘   └────────┬───────┘   └────────┬───────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ No Auth        │   │ Fetch Token    │   │ Fetch Token    │
│ Requests sent  │   │ from Keycloak  │   │ from Keycloak  │
│ without token  │   └────────┬───────┘   └────────┬───────┘
└────────────────┘            │                    │
                              ▼                    ▼
                     ┌────────────────┐   ┌────────────────┐
                     │ Store in       │   │ Store in       │
                     │ {{access_token}}   │ {{access_token}}
                     └────────┬───────┘   └────────┬───────┘
                              │                    │
                              ▼                    ▼
                     ┌────────────────┐   ┌────────────────┐
                     │ Attach Bearer  │   │ Attach Bearer  │
                     │ token to       │   │ token to       │
                     │ requests       │   │ requests       │
                     └────────────────┘   └────────────────┘
```

## 5. Endpoint Access Matrix Visual

```
Endpoint                  │ Public │ User │ Admin
─────────────────────────┼────────┼──────┼──────
hotels                   │   ✅   │  ✅  │  ✅
hotel(id)                │   ✅   │  ✅  │  ✅
hotelsByCity             │   ✅   │  ✅  │  ✅
hotelsByCountry          │   ✅   │  ✅  │  ✅
room(id)                 │   ✅   │  ✅  │  ✅
roomsByHotel             │   ✅   │  ✅  │  ✅
availableRooms           │   ✅   │  ✅  │  ✅
─────────────────────────┼────────┼──────┼──────
booking(id)              │   ❌   │  ✅  │  ✅
bookingsByCustomer       │   ❌   │  ✅  │  ✅
customer(id)             │   ❌   │  ✅  │  ✅
customerByEmail          │   ❌   │  ✅  │  ✅
createBooking            │   ❌   │  ✅  │  ✅
cancelBooking            │   ❌   │  ✅  │  ✅
─────────────────────────┼────────┼──────┼──────
upcomingBookings         │   ❌   │  ❌  │  ✅

Legend:
✅ = Access Granted
❌ = Access Denied (401/403)

Annotations:
Public    = @PermitAll
User      = @RolesAllowed({"user", "admin"})
Admin     = @RolesAllowed("admin")
```

## 6. Development vs Production Mode

```
                    ┌────────────────┐
                    │ Application    │
                    │ Startup        │
                    └────────┬───────┘
                             │
                ┌────────────┴────────────┐
                │                         │
                ▼                         ▼
        ┌───────────────┐         ┌───────────────┐
        │ Dev Mode      │         │ Prod Mode     │
        │ (default)     │         │ -Dprofile=prod│
        └───────┬───────┘         └───────┬───────┘
                │                         │
                ▼                         ▼
        ┌───────────────┐         ┌───────────────┐
        │ Load %dev     │         │ Load prod     │
        │ properties    │         │ properties    │
        └───────┬───────┘         └───────┬───────┘
                │                         │
                ▼                         ▼
        ┌───────────────┐         ┌───────────────┐
        │ Security:     │         │ Security:     │
        │ DISABLED      │         │ ENABLED       │
        └───────┬───────┘         └───────┬───────┘
                │                         │
                ▼                         ▼
        ┌───────────────┐         ┌───────────────┐
        │ All endpoints │         │ Check JWT     │
        │ accessible    │         │ tokens        │
        └───────────────┘         └───────┬───────┘
                                          │
                                          ▼
                                  ┌───────────────┐
                                  │ Enforce roles │
                                  │ via           │
                                  │ @RolesAllowed │
                                  └───────────────┘
```

## 7. Error Response Flow

```
Request with Invalid/No Token
         │
         ▼
┌────────────────────┐
│ Security Filter    │
└────────┬───────────┘
         │
         ├──────────────────────┬──────────────────────┐
         │                      │                      │
         ▼                      ▼                      ▼
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ No Token       │   │ Invalid Token  │   │ Expired Token  │
└────────┬───────┘   └────────┬───────┘   └────────┬───────┘
         │                    │                    │
         └────────────────────┴────────────────────┘
                              │
                              ▼
                     ┌────────────────┐
                     │ Return 401     │
                     │ Unauthorized   │
                     └────────────────┘

Request with Valid Token, Wrong Role
         │
         ▼
┌────────────────────┐
│ Security Filter    │
│ Token Valid        │
└────────┬───────────┘
         │
         ▼
┌────────────────────┐
│ Check @RolesAllowed│
│ annotation         │
└────────┬───────────┘
         │
         ▼
┌────────────────────┐
│ User Role: "user"  │
│ Required: "admin"  │
└────────┬───────────┘
         │
         ▼
┌────────────────────┐
│ Return 403         │
│ Forbidden          │
└────────────────────┘
```

## 8. Token Lifecycle

```
Time: 0 min                    ┌─────────────────┐
                               │ User Logs In    │
                               └────────┬────────┘
                                        │
Time: 0 min                             ▼
                               ┌─────────────────┐
                               │ Token Issued    │
                               │ Exp: +60 min    │
                               └────────┬────────┘
                                        │
Time: 0-60 min                          ▼
                               ┌─────────────────┐
                               │ Token Valid     │
                               │ Requests Succeed│
                               └────────┬────────┘
                                        │
Time: 60 min                            ▼
                               ┌─────────────────┐
                               │ Token Expires   │
                               └────────┬────────┘
                                        │
                                        ▼
                               ┌─────────────────┐
                               │ 401 Unauthorized│
                               │ Get New Token   │
                               └─────────────────┘
```

## Legend

```
Symbols Used:
├── Branch point
│   Vertical line
─   Horizontal line
▼   Flow direction
┌─┐ Box corners
└─┘ Box corners
✅  Success/Allowed
❌  Denied/Blocked
```

---

These diagrams provide a visual representation of the security implementation.
For detailed explanations, see:
- [docs/SECURITY.md](docs/SECURITY.md)
- [SECURITY-IMPLEMENTATION.md](SECURITY-IMPLEMENTATION.md)
