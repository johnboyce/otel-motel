# Security Cheat Sheet

Quick reference for otel-motel API security.

## 🔐 Roles & Permissions

| Role | Permissions |
|------|-------------|
| **Public** | Browse hotels and rooms |
| **user** | Create/view own bookings, view customers |
| **admin** | All user permissions + view all bookings |

## 🎭 Pre-configured Users

```
User:  user1  / password  (role: user)
Admin: admin1 / admin     (roles: admin, user)
```

## 🌍 Environments

| Environment | Auth | Use For |
|-------------|------|---------|
| Development (No Auth) | ❌ Off | Local development |
| User | ✅ On | User role testing |
| Admin | ✅ On | Admin role testing |

## 📋 Endpoint Access Matrix

| Endpoint | Public | User | Admin |
|----------|--------|------|-------|
| hotels | ✅ | ✅ | ✅ |
| hotel | ✅ | ✅ | ✅ |
| hotelsByCity | ✅ | ✅ | ✅ |
| hotelsByCountry | ✅ | ✅ | ✅ |
| room | ✅ | ✅ | ✅ |
| roomsByHotel | ✅ | ✅ | ✅ |
| availableRooms | ✅ | ✅ | ✅ |
| booking | ❌ | ✅ | ✅ |
| bookingsByCustomer | ❌ | ✅ | ✅ |
| upcomingBookings | ❌ | ❌ | ✅ |
| customer | ❌ | ✅ | ✅ |
| customerByEmail | ❌ | ✅ | ✅ |
| createBooking | ❌ | ✅ | ✅ |
| cancelBooking | ❌ | ✅ | ✅ |

## 🚀 Start Commands

```bash
# Dev mode (no auth)
make dev
./mvnw quarkus:dev

# Production mode (auth enabled)
./mvnw quarkus:dev -Dquarkus.profile=prod
```

## 🔑 Get Token Manually

```bash
# User token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=user1" \
  -d "password=password"

# Admin token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=admin1" \
  -d "password=admin"
```

## 📝 Use Token in Request

```bash
# Save token
TOKEN="your-access-token-here"

# Make authenticated request
curl -X POST http://localhost:8080/graphql \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ bookingsByCustomer(customerId: \"1\") { id status } }"}'
```

## 🔍 Verify Token

```bash
# Introspect token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token/introspect \
  -d "token=$TOKEN" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret"
```

## 🌐 Service URLs

```
GraphQL:  http://localhost:8080/graphql
Keycloak: http://localhost:8180
GraphQL UI: http://localhost:8080/q/graphql-ui
```

## ⚙️ Keycloak Admin

```
URL: http://localhost:8180
Username: admin
Password: admin
Realm: otel-motel
```

## 🔧 Configuration Profiles

| Profile | Security | Where |
|---------|----------|-------|
| dev | ❌ Disabled | `%dev.quarkus.oidc.enabled=false` |
| test | ❌ Disabled | `%test.quarkus.oidc.enabled=false` |
| prod | ✅ Enabled | `quarkus.oidc.enabled=true` |

## 🎯 Bruno Environment Variables

```
graphql_url: http://localhost:8080/graphql
keycloak_url: http://localhost:8180
keycloak_realm: otel-motel
keycloak_client_id: otel-motel-client
keycloak_client_secret: secret
auth_enabled: true/false
username: user1 or admin1
password: password or admin
```

## 🐛 Common Errors

| Error | HTTP | Cause | Solution |
|-------|------|-------|----------|
| Unauthorized | 401 | No/invalid token | Get fresh token |
| Forbidden | 403 | Insufficient role | Use admin environment |
| OIDC unavailable | - | Keycloak down | Start Keycloak |
| Connection refused | - | App not running | Start application |

## 📚 Documentation Links

- [Full Security Guide](docs/SECURITY.md)
- [Implementation Details](SECURITY-IMPLEMENTATION.md)
- [Quick Start](QUICKSTART-SECURITY.md)
- [Bruno Guide](bruno/README.md)

## 💡 Pro Tips

1. **Use dev mode** for local development (no auth hassle)
2. **Test with Bruno** environments before manual curl
3. **Check Keycloak logs** if auth fails
4. **Tokens expire** after 1 hour (re-fetch)
5. **Pre-request script** in Bruno handles tokens automatically

## 🎓 Security Concepts

**OAuth2**: Industry-standard authorization framework
**OIDC**: Authentication layer on top of OAuth2
**JWT**: JSON Web Token (stateless token format)
**Bearer Token**: Authorization header format
**RBAC**: Role-Based Access Control

## ⚡ One-Liners

```bash
# Start everything
docker compose up -d && ./mvnw quarkus:dev

# Stop everything
docker compose down

# View all logs
docker compose logs -f

# Check Keycloak health
curl http://localhost:8180/health/ready

# Test public endpoint
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotels { id name } }"}'
```

---

**Need help?** See [docs/SECURITY.md](docs/SECURITY.md) for detailed guides.
