# PostgreSQL Configuration for Keycloak

This directory contains PostgreSQL configuration and initialization scripts for Keycloak persistence.

## Overview

PostgreSQL is used as the persistence layer for Keycloak to maintain authentication and authorization data across container restarts. While the application itself uses DynamoDB for its data storage, Keycloak requires a relational database for its internal data structures.

## Files

### `init-keycloak-db.sql`
Initialization script that runs when the PostgreSQL container starts for the first time. It:
- Connects to the keycloak database
- Sets the timezone to UTC
- Provides documentation on database setup

## Database Configuration

### Connection Details
- **Host**: `postgres` (Docker service name) or `localhost:5432` (from host)
- **Database**: `keycloak`
- **User**: `keycloak`
- **Password**: `keycloak` (⚠️ Change in production!)
- **Port**: 5432

### Docker Compose Configuration
The PostgreSQL service is configured in `docker-compose.yml` with:
```yaml
postgres:
  image: postgres:16-alpine
  container_name: otel-motel-postgres
  environment:
    POSTGRES_DB: keycloak
    POSTGRES_USER: keycloak
    POSTGRES_PASSWORD: keycloak
  volumes:
    - postgres_data:/var/lib/postgresql/data
    - ./docker/postgres/init-keycloak-db.sql:/docker-entrypoint-initdb.d/init.sql
  ports:
    - "5432:5432"
```

## Keycloak Integration

Keycloak is configured to use PostgreSQL through environment variables:
```yaml
KC_DB: postgres
KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
KC_DB_USERNAME: keycloak
KC_DB_PASSWORD: keycloak
```

### How Keycloak Schema is Loaded

The Keycloak database schema is **automatically managed by Keycloak itself**. Here's how it works:

1. **PostgreSQL Initialization** (first startup):
   - PostgreSQL container starts with the `postgres:16-alpine` image
   - The `init-keycloak-db.sql` script runs automatically via Docker's `/docker-entrypoint-initdb.d/` mechanism
   - This creates the `keycloak` database and sets up the `keycloak` user with appropriate permissions
   - The database is now empty but ready for Keycloak to use

2. **Keycloak Schema Creation** (first startup):
   - Keycloak container starts and connects to PostgreSQL using the configured credentials
   - Keycloak detects that the database is empty (no schema tables exist)
   - Keycloak automatically runs its internal migration scripts to create all necessary tables
   - This includes creating tables for:
     - Realms (realm, realm_attribute, etc.)
     - Users (user_entity, user_attribute, credential, etc.)
     - Roles (keycloak_role, role_attribute, etc.)
     - Clients (client, client_attribute, etc.)
     - Sessions (user_session, authentication_session, etc.)
     - And many more (~100+ tables total)

3. **Realm Import** (first startup):
   - After schema creation, Keycloak imports the realm configuration from `/opt/keycloak/data/import/otel-motel-realm.json`
   - This is triggered by the `--import-realm` flag in the Keycloak startup command
   - The realm import creates the pre-configured users, roles, and client settings

4. **Subsequent Startups**:
   - Keycloak detects that the schema already exists
   - It checks the schema version and runs any necessary migrations for updates
   - The data persists in the `postgres_data` Docker volume

**Key Points:**
- ✅ You don't need to manually create Keycloak tables
- ✅ Keycloak handles all schema creation and migrations automatically
- ✅ The `init-keycloak-db.sql` script only initializes the database, not the schema
- ✅ Schema is version-controlled and managed by Keycloak's migration system
- ✅ Data persists across container restarts via Docker volumes

## Data Persistence

PostgreSQL data is persisted in a Docker volume named `postgres_data`. This ensures that:
- Keycloak realms, users, and configurations survive container restarts
- Data is maintained even when containers are stopped and restarted
- Database state is preserved across docker-compose down/up cycles

### Volume Management

```bash
# List volumes
docker volume ls | grep postgres

# Inspect volume
docker volume inspect otel-motel_postgres_data

# Remove volume (⚠️ Deletes all data!)
docker volume rm otel-motel_postgres_data
```

## Database Operations

### Connect to PostgreSQL

```bash
# Via psql in container
docker exec -it otel-motel-postgres psql -U keycloak -d keycloak

# Via Makefile
make postgres-console
```

### Common Commands

```sql
-- List all databases
\l

-- Connect to keycloak database
\c keycloak

-- List all tables
\dt

-- Show table schema
\d+ table_name

-- Check Keycloak tables (after first run)
\dt kc_*
```

### Backup Database

```bash
# Backup
docker exec otel-motel-postgres pg_dump -U keycloak keycloak > keycloak-backup.sql

# Restore
docker exec -i otel-motel-postgres psql -U keycloak keycloak < keycloak-backup.sql
```

### Reset Database

To completely reset Keycloak data:

```bash
# Stop services
make docker-down

# Remove postgres volume
docker volume rm otel-motel_postgres_data

# Start services (will reinitialize)
make docker-up
```

## Health Checks

PostgreSQL includes a health check that verifies the database is ready:

```bash
# Check health
docker inspect otel-motel-postgres | grep -A 5 Health

# Via pg_isready
docker exec otel-motel-postgres pg_isready -U keycloak
```

## Troubleshooting

### Connection Issues

**Problem**: Keycloak can't connect to PostgreSQL
```bash
# Check if postgres is running
docker ps | grep postgres

# Check postgres logs
docker logs otel-motel-postgres

# Check network connectivity
docker exec otel-motel-keycloak ping -c 3 postgres
```

**Solution**: Ensure postgres service starts before keycloak (handled by `depends_on`)

### Data Not Persisting

**Problem**: Keycloak configuration resets on restart
```bash
# Verify volume is mounted
docker inspect otel-motel-postgres | grep -A 10 Mounts

# Check if data directory has content
docker exec otel-motel-postgres ls -la /var/lib/postgresql/data
```

**Solution**: Ensure postgres_data volume is properly configured in docker-compose.yml

### Slow Performance

**Problem**: Database queries are slow

```sql
-- Check database size
SELECT pg_size_pretty(pg_database_size('keycloak'));

-- Check table sizes
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Solution**: Consider vacuum, analyze, or tune PostgreSQL settings for your workload

## Production Considerations

⚠️ **DO NOT use these settings in production:**

### Security
- [ ] Change default password from `keycloak`
- [ ] Use secrets management (Docker secrets, Kubernetes secrets)
- [ ] Enable SSL/TLS for database connections
- [ ] Restrict network access to database
- [ ] Use strong passwords (16+ characters, mixed case, numbers, symbols)

### Performance
- [ ] Tune PostgreSQL settings for your workload
- [ ] Configure connection pooling
- [ ] Set appropriate shared_buffers, effective_cache_size
- [ ] Enable query logging for slow queries
- [ ] Monitor database metrics

### High Availability
- [ ] Set up PostgreSQL replication (primary-replica)
- [ ] Configure automated backups
- [ ] Implement backup retention policies
- [ ] Test restore procedures
- [ ] Use managed database service (RDS, CloudSQL, etc.)

### Monitoring
- [ ] Set up database metrics collection
- [ ] Monitor connection counts
- [ ] Track query performance
- [ ] Alert on disk space usage
- [ ] Monitor replication lag

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| POSTGRES_DB | keycloak | Database name |
| POSTGRES_USER | keycloak | Database user |
| POSTGRES_PASSWORD | keycloak | Database password |
| PGDATA | /var/lib/postgresql/data | Data directory |

## Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Keycloak Database Configuration](https://www.keycloak.org/server/db)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)

## Related Documentation

- [Keycloak Configuration](../keycloak/README.md)
- [Docker Configuration](../README.md)
- [Main README](../../README.md)
