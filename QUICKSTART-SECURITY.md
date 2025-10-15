# Quick Start - API Security

This guide gets you up and running with the secured otel-motel API in 5 minutes.

## Step 1: Start All Services (2 min)

```bash
# Start DynamoDB, ELK, OTEL Collector, and Keycloak
docker compose up -d

# Wait for Keycloak to initialize (check logs)
docker compose logs -f keycloak
# Look for: "Keycloak <version> started"
# Press Ctrl+C when ready
```

## Step 2: Choose Your Mode

### Option A: Development (No Authentication)
Best for rapid development and debugging.

```bash
# Start application without security
make dev
# or
./mvnw quarkus:dev
```

**In Bruno:**
- Select environment: "Development (No Auth)"
- All endpoints work without authentication

### Option B: With Authentication
Test with real OAuth2/OIDC authentication.

```bash
# Start application with security enabled
./mvnw quarkus:dev -Dquarkus.profile=prod
```

**In Bruno:**
- Select environment: "User" or "Admin"
- Tokens automatically fetched and attached

## Step 3: Test in Bruno

1. **Open Bruno**
   ```bash
   # macOS
   open /Applications/Bruno.app
   ```

2. **Open Collection**
   - File → Open Collection
   - Navigate to: `./bruno/ote-motel`

3. **Select Environment** (top-right dropdown)
   - "Development (No Auth)" - for dev mode
   - "User" - for user role testing
   - "Admin" - for admin role testing

4. **Run a Request**
   - Hotels → "Get All Hotels"
   - Click "Send"
   - ✅ Should succeed

## Test Different Scenarios

### Test Public Access
```
Environment: Any
Request: Get All Hotels
Expected: ✅ Success
```

### Test User Access
```
Environment: User or Admin
Request: Create Booking
Expected: ✅ Success
```

### Test Admin Access
```
Environment: Admin
Request: Get Upcoming Bookings
Expected: ✅ Success

Environment: User
Request: Get Upcoming Bookings
Expected: ❌ 403 Forbidden (user role insufficient)
```

## Pre-configured Users

| Username | Password | Roles | Description |
|----------|----------|-------|-------------|
| user1 | password | user | Regular user |
| admin1 | admin | admin, user | Administrator |

## Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| GraphQL API | http://localhost:8080/graphql | - |
| GraphQL UI | http://localhost:8080/q/graphql-ui | - |
| Keycloak Admin | http://localhost:8180 | admin/admin |
| Kibana | http://localhost:5601 | - |

## Troubleshooting

### "401 Unauthorized" Error
**Solution:** Check you're using the correct Bruno environment with authentication enabled.

### "403 Forbidden" Error
**Solution:** Switch to an environment with sufficient privileges (Admin for admin endpoints).

### "OIDC server is not available"
**Solution:** 
```bash
# Ensure Keycloak is running
docker compose ps keycloak

# Check logs
docker compose logs keycloak

# Restart if needed
docker compose restart keycloak
```

### Bruno Token Fetch Fails
**Solution:**
```bash
# Verify Keycloak is accessible
curl http://localhost:8180/realms/otel-motel/.well-known/openid-configuration

# Should return JSON configuration
```

## What's Next?

- 📖 Read [Full Security Guide](docs/SECURITY.md)
- 🔧 Review [Security Implementation Details](SECURITY-IMPLEMENTATION.md)
- 🧪 Explore [Bruno Collection Guide](bruno/README.md)

## Quick Commands

```bash
# Start everything
docker compose up -d && ./mvnw quarkus:dev

# Stop everything
./mvnw quarkus:dev (Ctrl+C)
docker compose down

# View logs
docker compose logs -f

# Get a token manually (for testing)
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=user1" \
  -d "password=password"
```

## That's It! 🎉

You're now ready to use the secured otel-motel API!

For detailed documentation, see:
- [docs/SECURITY.md](docs/SECURITY.md) - Complete security guide
- [SECURITY-IMPLEMENTATION.md](SECURITY-IMPLEMENTATION.md) - Implementation details
- [bruno/README.md](bruno/README.md) - Bruno collection guide
