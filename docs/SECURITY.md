# Security Setup Guide for otel-motel

This guide explains how to set up and use OAuth2/OIDC authentication for the otel-motel API.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Security Scopes](#security-scopes)
- [Setup Instructions](#setup-instructions)
- [Testing with Bruno](#testing-with-bruno)
- [Development vs Production](#development-vs-production)
- [Troubleshooting](#troubleshooting)

## Overview

The otel-motel API is protected using OAuth2/OIDC (OpenID Connect) with JWT tokens. Authentication is provided by Keycloak, an open-source identity and access management solution.

### Key Features
- **JWT-based authentication** using OAuth2/OIDC standards
- **Role-based access control (RBAC)** with `user` and `admin` roles
- **Scope-based authorization** for different API operations
- **Development mode** with optional security for local testing
- **Bruno API client** with automatic token management

## Architecture

```
┌─────────────┐
│   Bruno     │
│  API Client │
└──────┬──────┘
       │
       │ 1. Request Token
       ▼
┌─────────────┐
│  Keycloak   │
│   (OIDC)    │
└──────┬──────┘
       │
       │ 2. Return JWT Token
       ▼
┌─────────────┐
│   Bruno     │
│  API Client │
└──────┬──────┘
       │
       │ 3. GraphQL Request + JWT
       ▼
┌─────────────────┐
│  Quarkus API    │
│  (GraphQL)      │
│  - Validates JWT│
│  - Checks Roles │
└─────────────────┘
```

## Security Scopes

### Public Endpoints (No Authentication Required)
These endpoints are accessible without authentication:
- `hotels` - Get all hotels
- `hotel` - Get hotel by ID
- `hotelsByCity` - Get hotels by city
- `hotelsByCountry` - Get hotels by country
- `room` - Get room by ID
- `roomsByHotel` - Get rooms by hotel
- `availableRooms` - Get available rooms for dates

### User Scope (`user` role)
These endpoints require authentication with the `user` role:
- `booking` - Get booking by ID
- `bookingsByCustomer` - Get bookings for a customer
- `customer` - Get customer by ID
- `customerByEmail` - Get customer by email
- `createBooking` - Create a new booking
- `cancelBooking` - Cancel a booking

### Admin Scope (`admin` role)
These endpoints require authentication with the `admin` role:
- `upcomingBookings` - Get all upcoming bookings (all customers)

**Note:** The `admin` role inherits all permissions from the `user` role.

## Setup Instructions

### 1. Start Keycloak

Start the entire stack including Keycloak:

```bash
docker compose up -d
```

Wait for Keycloak to be ready (usually 60-90 seconds):

```bash
docker compose logs -f keycloak
```

Look for: `Keycloak <version> started`

### 2. Verify Keycloak is Running

Access the Keycloak admin console:
- URL: http://localhost:8180
- Username: `admin`
- Password: `admin`

### 3. Verify Realm Configuration

The `otel-motel` realm is automatically imported with:

**Pre-configured Users:**
- **Regular User**
  - Username: `user1`
  - Password: `password`
  - Roles: `user`
  
- **Admin User**
  - Username: `admin1`
  - Password: `admin`
  - Roles: `admin`, `user`

**Client Configuration:**
- Client ID: `otel-motel-client`
- Client Secret: `secret`
- Direct Access Grants: Enabled (for password flow)

### 4. Start the Application

For development with security **disabled**:
```bash
make dev
# or
./mvnw quarkus:dev
```

For testing with security **enabled**:
```bash
./mvnw quarkus:dev -Dquarkus.profile=prod
```

## Testing with Bruno

### Environment Setup

Bruno includes three pre-configured environments:

#### 1. Development (No Auth)
Use this for local development without security:
- Authentication: Disabled
- Best for: Local testing, debugging

#### 2. User
Use this to test as a regular user:
- Username: `user1`
- Password: `password`
- Roles: `user`
- Can: Create bookings, view own bookings, view public data

#### 3. Admin
Use this to test as an admin:
- Username: `admin1`
- Password: `admin`
- Roles: `admin`, `user`
- Can: Everything a user can + view all bookings

### Using Bruno

1. **Open Bruno**
   ```bash
   open /Applications/Bruno.app
   # or launch from your Applications folder
   ```

2. **Open the Collection**
   - File → Open Collection
   - Navigate to: `./bruno/ote-motel`

3. **Select an Environment**
   - Click the environment dropdown (top-right)
   - Choose: "Development (No Auth)", "User", or "Admin"

4. **Run Requests**
   - The pre-request script automatically obtains JWT tokens
   - Tokens are automatically attached to all requests
   - Click "Send" on any request

### Pre-Request Script

The collection includes an automatic token management script:
- Automatically fetches JWT tokens from Keycloak
- Stores tokens in environment variables
- Attaches tokens to all requests
- Handles token refresh (gets fresh token per request for simplicity)

### Testing Different Scenarios

#### Test Public Access
1. Select "Development (No Auth)" environment
2. Run any hotel or room query
3. Should succeed without authentication

#### Test User Access
1. Select "User" environment
2. Run "Create Booking" - should succeed
3. Run "Get Upcoming Bookings" - should fail (admin only)

#### Test Admin Access
1. Select "Admin" environment
2. Run "Get Upcoming Bookings" - should succeed
3. All user operations should also succeed

## Development vs Production

### Development Mode (Default)
```properties
%dev.quarkus.oidc.enabled=false
%dev.quarkus.security.auth.enabled-in-dev-mode=false
```

In development mode:
- Security is **disabled** by default
- No authentication required
- Use "Development (No Auth)" Bruno environment

### Production Mode
```properties
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=http://localhost:8180/realms/otel-motel
```

In production mode:
- Security is **enabled**
- JWT tokens required for protected endpoints
- Use "User" or "Admin" Bruno environments

### Switching Modes

**Run in Dev Mode (No Auth):**
```bash
./mvnw quarkus:dev
```

**Run in Production Mode (With Auth):**
```bash
./mvnw quarkus:dev -Dquarkus.profile=prod
```

## Configuration Reference

### Application Properties

```properties
# Enable OIDC
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=http://localhost:8180/realms/otel-motel
quarkus.oidc.client-id=otel-motel-client
quarkus.oidc.credentials.secret=secret

# JWT Verification
mp.jwt.verify.publickey.location=http://localhost:8180/realms/otel-motel/protocol/openid-connect/certs
mp.jwt.verify.issuer=http://localhost:8180/realms/otel-motel

# Permissions
quarkus.http.auth.permission.authenticated.paths=/graphql
quarkus.http.auth.permission.authenticated.policy=authenticated
```

### Keycloak Configuration

The Keycloak realm configuration is located at:
```
docker/keycloak/otel-motel-realm.json
```

Key settings:
- Token lifespan: 3600 seconds (1 hour)
- SSO session timeout: 1800 seconds (30 minutes)
- Direct access grants enabled (for password flow)

## Troubleshooting

### Issue: "401 Unauthorized" Error

**Cause:** Token is missing or invalid

**Solution:**
1. Check that you're using the correct Bruno environment
2. Verify Keycloak is running: http://localhost:8180
3. Check the Bruno console for token fetch errors
4. Ensure the user credentials are correct in the environment

### Issue: "403 Forbidden" Error

**Cause:** User doesn't have required role

**Solution:**
1. Check the endpoint's required role in the docs
2. Switch to appropriate Bruno environment:
   - Admin endpoints: Use "Admin" environment
   - User endpoints: Use "User" or "Admin" environment
3. Verify role assignments in Keycloak admin console

### Issue: Token Fetch Fails in Bruno

**Cause:** Keycloak not accessible or configuration error

**Solution:**
1. Verify Keycloak is running:
   ```bash
   curl http://localhost:8180/realms/otel-motel/.well-known/openid-configuration
   ```
2. Check environment variables in Bruno:
   - `keycloak_url`: http://localhost:8180
   - `keycloak_realm`: otel-motel
   - `keycloak_client_id`: otel-motel-client
   - `keycloak_client_secret`: secret

### Issue: "OIDC server is not available"

**Cause:** Keycloak container not started or still initializing

**Solution:**
1. Start Keycloak:
   ```bash
   docker compose up -d keycloak
   ```
2. Wait for startup (60-90 seconds)
3. Check logs:
   ```bash
   docker compose logs keycloak
   ```

### Issue: Development Mode Has Authentication Enabled

**Cause:** Running in production profile

**Solution:**
Run in dev mode explicitly:
```bash
./mvnw quarkus:dev
# Don't use -Dquarkus.profile=prod
```

## Security Best Practices

### For Development
1. Use "Development (No Auth)" environment for rapid iteration
2. Test with real authentication before committing changes
3. Never commit tokens or credentials to git

### For Production
1. Use HTTPS/TLS for all connections
2. Rotate client secrets regularly
3. Implement token refresh logic (not just fetching new tokens)
4. Configure appropriate token lifespans
5. Use proper secret management (not hardcoded values)
6. Enable rate limiting
7. Implement proper logging and monitoring
8. Use Keycloak in production mode with database persistence

## Additional Resources

- [Quarkus OIDC Guide](https://quarkus.io/guides/security-oidc-bearer-token-authentication)
- [Quarkus Security Guide](https://quarkus.io/guides/security)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth2 RFC](https://oauth.net/2/)
- [OpenID Connect Specification](https://openid.net/connect/)

## Quick Reference

### Obtaining a Token Manually (curl)

```bash
# Get token for user
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=user1" \
  -d "password=password"

# Use token in GraphQL request
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotels { id name } }"}'
```

### Testing Token Validation

```bash
# Introspect token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_TOKEN_HERE" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret"
```
