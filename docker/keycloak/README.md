# Keycloak Configuration for otel-motel

This directory contains the Keycloak realm configuration for the otel-motel API authentication and authorization.

## Files

- **otel-motel-realm.json** - Complete realm configuration with users, roles, and client settings

## Realm Configuration

### Basic Information
- **Realm Name:** otel-motel
- **Display Name:** otel-motel Hotel Booking API
- **Enabled:** Yes
- **SSL Required:** None (for development)

### Token Configuration
- **Access Token Lifespan:** 3600 seconds (1 hour)
- **SSO Session Idle:** 1800 seconds (30 minutes)
- **SSO Session Max:** 36000 seconds (10 hours)
- **Access Code Lifespan:** 60 seconds

## Client Configuration

### otel-motel-client
- **Client ID:** otel-motel-client
- **Client Secret:** secret (change in production!)
- **Protocol:** openid-connect
- **Access Type:** confidential
- **Direct Access Grants:** Enabled (password flow)
- **Service Accounts:** Enabled
- **Standard Flow:** Enabled
- **Valid Redirect URIs:** * (all URIs - restrict in production)
- **Web Origins:** * (all origins - restrict in production)

## Pre-configured Users

### User 1 (Regular User)
- **Username:** user1
- **Email:** user1@example.com
- **First Name:** Regular
- **Last Name:** User
- **Password:** password (not temporary)
- **Enabled:** Yes
- **Email Verified:** Yes
- **Realm Roles:** user
- **Client Roles:** user

**Use for:** Testing user-level operations (bookings, customer queries)

### Admin 1 (Administrator)
- **Username:** admin1
- **Email:** admin1@example.com
- **First Name:** Admin
- **Last Name:** User
- **Password:** admin (not temporary)
- **Enabled:** Yes
- **Email Verified:** Yes
- **Realm Roles:** admin, user
- **Client Roles:** admin, user

**Use for:** Testing admin operations (all bookings, administrative queries)

## Client Scopes

The realm includes standard OpenID Connect scopes that control what information is included in tokens:

### profile
Includes user profile information in tokens:
- `preferred_username` - Username
- `given_name` - First name
- `family_name` - Last name
- `name` - Full name

### email
Includes email information in tokens:
- `email` - Email address
- `email_verified` - Email verification status

### address (optional)
Includes address information in tokens when requested

### phone (optional)
Includes phone number information in tokens when requested

### roles (default)
Includes role information in tokens:
- `realm_access.roles` - Realm-level roles
- `resource_access.${client_id}.roles` - Client-specific roles

### offline_access (optional)
Allows requesting refresh tokens for offline access

## Roles

### Realm Roles

**user**
- Description: User role - can view and create own bookings
- Permissions: 
  - Create bookings
  - View own bookings
  - Cancel own bookings
  - View customer profiles

**admin**
- Description: Admin role - full access to all operations
- Permissions:
  - All user permissions
  - View all bookings (across all customers)
  - Administrative reports

### Role Inheritance
- Admin role inherits user role permissions
- Users with admin role can perform all user operations

## Protocol Mappers

### Realm Roles Mapper
- **Name:** realm roles
- **Protocol:** openid-connect
- **Mapper Type:** oidc-usermodel-realm-role-mapper
- **Token Claim Name:** realm_access.roles
- **Claim JSON Type:** String
- **Add to ID token:** Yes
- **Add to access token:** Yes
- **Add to userinfo:** Yes
- **Multivalued:** Yes

### Client Roles Mapper
- **Name:** client roles
- **Protocol:** openid-connect
- **Mapper Type:** oidc-usermodel-client-role-mapper
- **Token Claim Name:** resource_access.${client_id}.roles
- **Claim JSON Type:** String
- **Add to ID token:** Yes
- **Add to access token:** Yes
- **Add to userinfo:** Yes
- **Multivalued:** Yes

## Token Structure

When a user authenticates, they receive a JWT token with this structure:

```json
{
  "exp": 1234567890,
  "iat": 1234567890,
  "iss": "http://localhost:8180/realms/otel-motel",
  "sub": "user-id",
  "typ": "Bearer",
  "azp": "otel-motel-client",
  "preferred_username": "user1",
  "email": "user1@example.com",
  "email_verified": true,
  "realm_access": {
    "roles": ["user"]
  },
  "resource_access": {
    "otel-motel-client": {
      "roles": ["user"]
    }
  }
}
```

## Importing the Realm

### Automatic Import (Docker Compose)
The realm is automatically imported when Keycloak starts via docker-compose:
```yaml
volumes:
  - ./docker/keycloak:/opt/keycloak/data/import
command: start-dev --import-realm
```

### Manual Import
1. Access Keycloak Admin Console: http://localhost:8180
2. Login with admin/admin
3. Click "Add realm" button
4. Click "Select file" and choose `otel-motel-realm.json`
5. Click "Create"

## Exporting the Realm

To export changes made in the Keycloak UI:

```bash
# Enter the Keycloak container
docker exec -it otel-motel-keycloak /bin/bash

# Export the realm
/opt/keycloak/bin/kc.sh export --dir /tmp --realm otel-motel

# Exit the container
exit

# Copy the exported file
docker cp otel-motel-keycloak:/tmp/otel-motel-realm.json ./docker/keycloak/
```

## Customization

### Adding New Users

1. **Via Keycloak UI:**
   - Login to admin console
   - Select otel-motel realm
   - Go to Users → Add user
   - Set username, email, etc.
   - Go to Credentials tab
   - Set password
   - Go to Role Mapping tab
   - Assign roles

2. **Via Realm JSON:**
   - Edit `otel-motel-realm.json`
   - Add user to `users` array:
   ```json
   {
     "username": "newuser",
     "enabled": true,
     "email": "newuser@example.com",
     "credentials": [
       {
         "type": "password",
         "value": "password",
         "temporary": false
       }
     ],
     "realmRoles": ["user"],
     "clientRoles": {
       "otel-motel-client": ["user"]
     }
   }
   ```
   - Restart Keycloak

### Adding New Roles

1. **Via Keycloak UI:**
   - Login to admin console
   - Select otel-motel realm
   - Go to Realm roles → Add role
   - Set name and description
   - Assign to users/clients

2. **Via Realm JSON:**
   - Edit `otel-motel-realm.json`
   - Add role to `roles.realm` array:
   ```json
   {
     "name": "manager",
     "description": "Manager role with specific permissions"
   }
   ```
   - Restart Keycloak

## Testing Authentication

### Get Token (Password Flow)

```bash
# User token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=user1" \
  -d "password=password"

# Admin token
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret" \
  -d "username=admin1" \
  -d "password=admin"
```

### Introspect Token

```bash
curl -X POST http://localhost:8180/realms/otel-motel/protocol/openid-connect/token/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_ACCESS_TOKEN" \
  -d "client_id=otel-motel-client" \
  -d "client_secret=secret"
```

### Get User Info

```bash
curl -X GET http://localhost:8180/realms/otel-motel/protocol/openid-connect/userinfo \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Production Considerations

### Database

Keycloak uses PostgreSQL for data persistence in this setup. The database configuration is:
- **Host**: postgres (Docker service name)
- **Port**: 5432
- **Database**: keycloak
- **User**: keycloak
- **Password**: keycloak (⚠️ change in production!)

For more details on the PostgreSQL setup, see [PostgreSQL Configuration](../postgres/README.md).

### ⚠️ Security Warnings

**DO NOT use these settings in production:**
- Client secret "secret" - use a strong, random secret
- Password "password" - use strong passwords
- redirect_uris "*" - restrict to your application URLs
- webOrigins "*" - restrict to your frontend domains
- SSL required: none - enable SSL in production

### Production Checklist

- [ ] Change all default passwords
- [ ] Generate strong client secret
- [ ] Enable SSL/HTTPS
- [ ] Restrict redirect URIs
- [ ] Restrict web origins
- [ ] Use external database (PostgreSQL with proper backups)
- [ ] Use strong database passwords
- [ ] Enable realm caching
- [ ] Configure backup strategy
- [ ] Set up monitoring
- [ ] Enable audit logging
- [ ] Configure SMTP for email verification
- [ ] Set appropriate token lifespans
- [ ] Enable brute force protection
- [ ] Configure account lockout policies

## Troubleshooting

### Keycloak Not Starting
```bash
# Check logs
docker compose logs keycloak

# Common issues:
# - Port 8180 already in use
# - Realm file has JSON errors
# - Insufficient memory
```

### Realm Not Imported
```bash
# Verify file exists
ls -la docker/keycloak/otel-motel-realm.json

# Check container mount
docker exec otel-motel-keycloak ls -la /opt/keycloak/data/import/

# Reimport manually via UI
```

### Token Validation Fails
```bash
# Check issuer URL
curl http://localhost:8180/realms/otel-motel/.well-known/openid-configuration

# Verify public keys
curl http://localhost:8180/realms/otel-motel/protocol/openid-connect/certs

# Check application.properties issuer configuration
```

## References

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [OAuth2 Specification](https://oauth.net/2/)
- [OpenID Connect Specification](https://openid.net/connect/)
- [JWT.io Token Debugger](https://jwt.io/)

## Related Documentation

- [Main Security Guide](../../docs/SECURITY.md)
- [Quick Start](../../QUICKSTART-SECURITY.md)
- [Security Cheat Sheet](../../SECURITY-CHEATSHEET.md)
- [Security Implementation](../../SECURITY-IMPLEMENTATION.md)
