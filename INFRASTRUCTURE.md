# Infrastructure Setup Guide

This guide explains how the otel-motel infrastructure is set up and managed, including detailed information about database schema loading and comprehensive setup workflows.

## Overview

The otel-motel application relies on several infrastructure components:

1. **PostgreSQL** - Database for Keycloak (authentication server)
2. **Keycloak** - OAuth2/OIDC authentication and authorization server
3. **DynamoDB (LocalStack)** - Application data storage
4. **Elasticsearch** - Log and trace storage
5. **Kibana** - Log and trace visualization
6. **OpenTelemetry Collector** - Telemetry data collection and forwarding

## Quick Start

### Option 1: Infrastructure Only
```bash
make infrastructure-up
```

This command:
- ✅ Starts all Docker services
- ✅ Waits for all services to be healthy
- ✅ Initializes Elasticsearch indices
- ✅ Creates DynamoDB tables
- ✅ Verifies all services are ready

**Note:** You need AWS CLI installed to create DynamoDB tables. The Makefile uses dummy credentials (test/test) which work with LocalStack.

### Option 2: Complete Application Setup
```bash
make app-ready
```

This command does everything from Option 1, plus:
- ✅ Builds the application
- ✅ Starts the application in development mode
- ✅ Makes all services available and ready to use

## How Keycloak Database Schema is Loaded

One of the key questions about the infrastructure is: **How is the Keycloak database schema loaded into PostgreSQL?**

### The Answer: Automatic Schema Management by Keycloak

Keycloak **automatically manages its own database schema**. You don't need to manually create tables or run SQL scripts. Here's the detailed process:

#### Step 1: PostgreSQL Initialization (First Startup)

When the PostgreSQL container starts for the first time:

```yaml
# docker-compose.yml
postgres:
  image: postgres:16-alpine
  environment:
    POSTGRES_DB: keycloak      # Creates database
    POSTGRES_USER: keycloak    # Creates user
    POSTGRES_PASSWORD: keycloak
  volumes:
    - ./docker/postgres/init-keycloak-db.sql:/docker-entrypoint-initdb.d/init.sql
```

The `init-keycloak-db.sql` script runs automatically via Docker's entrypoint mechanism:
- Sets up the `keycloak` database
- Configures the `keycloak` user with appropriate permissions
- Sets timezone to UTC
- The database is now **empty but ready** for Keycloak

**Important**: This script does NOT create Keycloak tables. It only initializes the database.

#### Step 2: Keycloak Schema Creation (First Startup)

When Keycloak starts for the first time:

```yaml
# docker-compose.yml
keycloak:
  image: quay.io/keycloak/keycloak:23.0
  environment:
    KC_DB: postgres
    KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
    KC_DB_USERNAME: keycloak
    KC_DB_PASSWORD: keycloak
```

Keycloak performs the following automatically:

1. **Connects to PostgreSQL** using the configured credentials
2. **Detects empty database** (no Keycloak tables exist)
3. **Runs internal migration scripts** to create all required tables (~100+ tables)
4. **Creates core schema** including:
   - `realm`, `realm_attribute` - Realm configuration
   - `user_entity`, `user_attribute`, `credential` - User management
   - `keycloak_role`, `role_attribute` - Role management
   - `client`, `client_attribute` - Client applications
   - `user_session`, `authentication_session` - Session management
   - Many more supporting tables

All of this happens **automatically** - no manual intervention required!

#### Step 3: Realm Import (First Startup)

After schema creation, Keycloak imports the realm configuration:

```yaml
# docker-compose.yml
keycloak:
  command: start-dev --import-realm
  volumes:
    - ./docker/keycloak:/opt/keycloak/data/import
```

This imports the `otel-motel-realm.json` file, which creates:
- Pre-configured users (user1, admin1)
- Roles (user, admin)
- Client configuration (otel-motel-client)
- Authentication flows

#### Step 4: Subsequent Startups

On subsequent container startups:

1. Keycloak connects to PostgreSQL
2. Detects that schema already exists
3. Checks schema version
4. Runs any necessary migrations for version updates
5. Data persists via the `postgres_data` Docker volume

### Key Takeaways

✅ **No manual table creation needed** - Keycloak does it all

✅ **Schema is version-controlled** - Keycloak manages migrations

✅ **Data persists** - Docker volumes preserve database state

✅ **Automatic on first run** - Just start the containers

❌ **Don't manually modify schema** - Let Keycloak manage it

## Infrastructure Setup Tasks

### make infrastructure-up

The most comprehensive setup task. It performs all steps needed to have a complete, ready-to-use infrastructure:

**What it does:**

1. **Starts Docker Services**
   ```bash
   docker compose up -d
   ```
   Starts all services in detached mode:
   - postgres (Keycloak database)
   - keycloak (authentication server)
   - dynamodb (application database)
   - elasticsearch (log storage)
   - kibana (log visualization)
   - otel-collector (telemetry collector)

2. **Waits for Service Health**
   ```bash
   ./scripts/wait-for-services.sh
   ```
   Monitors health checks for all services:
   - Waits up to 5 minutes for all services to be healthy
   - Checks every 5 seconds
   - Provides real-time status updates
   - Fails if timeout is reached

3. **Initializes Elasticsearch**
   ```bash
   ./elk/elasticsearch/setup-indices.sh
   ```
   Creates:
   - Index templates for logs, traces, and metrics
   - Index Lifecycle Management (ILM) policies
   - Ingest pipelines for ECS compliance

4. **Creates DynamoDB Tables**
   ```bash
   aws dynamodb create-table ...
   ```
   Creates application tables:
   - `bookings` - Hotel booking records
   - `customers` - Customer information
   - `hotels` - Hotel details
   - `rooms` - Room inventory

5. **Verifies Service Availability**
   Tests each service endpoint:
   - PostgreSQL connection
   - Keycloak health endpoint
   - DynamoDB list-tables
   - Elasticsearch cluster health
   - Kibana API status

**Usage:**
```bash
make infrastructure-up
```

**Expected Output:**
```
═══════════════════════════════════════════════════════════════
  Starting Complete Infrastructure Setup
═══════════════════════════════════════════════════════════════

Step 1: Starting Docker services...
[+] Running 6/6
 ✔ Container otel-motel-postgres       Started
 ✔ Container otel-motel-dynamodb       Started
 ✔ Container otel-motel-elasticsearch  Started
 ✔ Container otel-motel-kibana         Started
 ✔ Container otel-motel-otel-collector Started
 ✔ Container otel-motel-keycloak       Started

Step 2: Waiting for all services to be healthy...
  ✓ otel-motel-postgres is healthy
  ✓ otel-motel-dynamodb is healthy
  ✓ otel-motel-elasticsearch is healthy
  ✓ otel-motel-kibana is healthy
  ✓ otel-motel-keycloak is healthy

✅ All services are healthy!

Step 3: Initializing Elasticsearch indices...
Created logs index template
Created traces index template
Created metrics index template
Created ILM policy for logs
Created ECS remapping ingest pipeline

Step 4: Creating DynamoDB tables...
✓ DynamoDB tables created

Step 5: Verifying service availability...
  ✓ PostgreSQL is ready
  ✓ Keycloak is ready
  ✓ DynamoDB is ready
  ✓ Elasticsearch is ready
  ✓ Kibana is ready

═══════════════════════════════════════════════════════════════
  ✓ Infrastructure Setup Complete!
═══════════════════════════════════════════════════════════════

Services Available:
  PostgreSQL:     localhost:5432 (keycloak/keycloak)
  Keycloak:       http://localhost:8180 (admin/admin)
  DynamoDB:       localhost:4566
  Elasticsearch:  http://localhost:9200
  Kibana:         http://localhost:5601
  OTEL Collector: localhost:4318 (HTTP) / localhost:4317 (gRPC)

Next step: Build and run the application with 'make app-ready'
```

### make app-ready

The complete workflow that sets up infrastructure AND runs the application.

**What it does:**

1. Runs `make infrastructure-up` (all steps above)
2. Builds the application with Maven
3. Starts the application in development mode

**Usage:**
```bash
make app-ready
```

**Expected Output:**
After infrastructure setup completes:
```
═══════════════════════════════════════════════════════════════
  Starting Application
═══════════════════════════════════════════════════════════════

The application will start in development mode...

Note: The application will run in the foreground.
Press Ctrl+C to stop the application.

Application URLs (once started):
  GraphQL UI:     http://localhost:8080/q/graphql-ui
  GraphQL API:    http://localhost:8080/graphql
  Dev UI:         http://localhost:8080/q/dev

Starting in 3 seconds...

[INFO] --- quarkus:3.x.x:dev (default-cli) @ otel-motel ---
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/

INFO  [io.quarkus] (Quarkus Main Thread) otel-motel 1.0-SNAPSHOT on JVM started in 2.345s
INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [...]
```

**When to use:**
- First time setup
- After pulling new changes
- When you want a clean, complete environment
- For testing the full stack

### Legacy Tasks

These tasks are still available for granular control:

**make setup** - Original setup task (docker-up + elk-setup)
```bash
make setup
```

**make docker-up** - Start Docker services only
```bash
make docker-up
```

**make elk-setup** - Initialize Elasticsearch only
```bash
make elk-setup
```

**make dynamodb-create-tables** - Create DynamoDB tables only
```bash
make dynamodb-create-tables
```

## Service Health Checks

All services have health checks defined in `docker-compose.yml`:

### PostgreSQL
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U keycloak"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### DynamoDB (LocalStack)
```yaml
healthcheck:
  test: ["CMD-SHELL", "awslocal dynamodb list-tables || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### Elasticsearch
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
```

### Kibana
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:5601/api/status || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
```

### Keycloak
```yaml
healthcheck:
  test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/8180;..."]
  interval: 30s
  timeout: 10s
  retries: 5
  start_period: 90s
```

## Troubleshooting

### Services Not Healthy

If services fail health checks:

```bash
# Check service status
make docker-ps

# Check logs for specific service
docker compose logs postgres
docker compose logs keycloak
docker compose logs dynamodb

# Check all logs
make docker-logs
```

### Keycloak Not Starting

Common issues:

1. **Port 8180 in use**
   ```bash
   lsof -i :8180
   kill <PID>
   ```

2. **PostgreSQL not ready**
   ```bash
   docker compose logs postgres
   docker exec otel-motel-postgres pg_isready -U keycloak
   ```

3. **Realm import failed**
   ```bash
   # Check realm file exists
   ls -la docker/keycloak/otel-motel-realm.json
   
   # Verify mount in container
   docker exec otel-motel-keycloak ls -la /opt/keycloak/data/import/
   ```

### DynamoDB Tables Not Created

```bash
# Check DynamoDB is running
docker exec otel-motel-dynamodb awslocal dynamodb list-tables

# Manually create tables
make dynamodb-create-tables

# Reset and recreate
make dynamodb-reset
make dynamodb-create-tables
```

### Elasticsearch Not Ready

```bash
# Check Elasticsearch health
curl http://localhost:9200/_cluster/health?pretty

# Check logs
docker compose logs elasticsearch

# Increase memory if needed (in docker-compose.yml)
ES_JAVA_OPTS=-Xms1g -Xmx1g
```

## Data Persistence

All data is stored in Docker volumes:

```yaml
volumes:
  postgres_data:      # Keycloak database
  elasticsearch_data: # Logs and traces
  # Note: DynamoDB and Keycloak realm data are in bind mounts
```

### View Volumes
```bash
docker volume ls | grep otel-motel
```

### Inspect Volume
```bash
docker volume inspect otel-motel_postgres_data
```

### Clean Up (⚠️ Destroys Data)
```bash
# Stop services and remove volumes
make docker-down-volumes

# Or manually
docker compose down -v
```

## Service Dependencies

Services start in this order (managed by `depends_on`):

```
1. postgres (no dependencies)
2. keycloak (depends on: postgres)
3. dynamodb (no dependencies)
4. elasticsearch (no dependencies)
5. kibana (depends on: elasticsearch)
6. otel-collector (depends on: elasticsearch)
```

## Environment Variables

Key configuration in `docker-compose.yml`:

### PostgreSQL
```yaml
POSTGRES_DB: keycloak
POSTGRES_USER: keycloak
POSTGRES_PASSWORD: keycloak  # ⚠️ Change in production
```

### Keycloak
```yaml
KEYCLOAK_ADMIN: admin
KEYCLOAK_ADMIN_PASSWORD: admin  # ⚠️ Change in production
KC_DB: postgres
KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
KC_DB_USERNAME: keycloak
KC_DB_PASSWORD: keycloak
KC_HTTP_PORT: 8180
```

### DynamoDB (LocalStack)
```yaml
SERVICES: dynamodb
DEBUG: 1
DATA_DIR: /tmp/localstack/data
```

### Elasticsearch
```yaml
discovery.type: single-node
xpack.security.enabled: false  # ⚠️ Enable in production
ES_JAVA_OPTS: -Xms512m -Xmx512m
```

## Production Considerations

⚠️ **DO NOT use development settings in production:**

### Security
- [ ] Change all default passwords
- [ ] Enable SSL/TLS for all services
- [ ] Use secrets management
- [ ] Restrict network access
- [ ] Enable authentication on Elasticsearch

### High Availability
- [ ] Use managed services (RDS, Elasticsearch Service)
- [ ] Set up database replication
- [ ] Configure backup strategies
- [ ] Implement monitoring and alerting

### Performance
- [ ] Tune database connection pools
- [ ] Adjust JVM heap sizes
- [ ] Configure resource limits
- [ ] Optimize Elasticsearch shards

## Related Documentation

- [PostgreSQL Configuration](docker/postgres/README.md) - Detailed PostgreSQL setup
- [Keycloak Configuration](docker/keycloak/README.md) - Authentication and realm configuration
- [ELK Stack Guide](elk/README.md) - Elasticsearch and Kibana details
- [OpenTelemetry Setup](OTEL-SETUP.md) - Observability configuration
- [Main README](README.md) - Project overview

## Summary

### For First-Time Setup
```bash
make app-ready
```
This single command sets up everything and starts the application.

### For Infrastructure Only
```bash
make infrastructure-up
```
Sets up all services without starting the application.

### For Development
```bash
make infrastructure-up  # Once
make dev                # Multiple times as needed
```

### Key Points
- ✅ Keycloak automatically manages its database schema
- ✅ All services have health checks
- ✅ Infrastructure setup is idempotent (safe to run multiple times)
- ✅ Data persists in Docker volumes
- ✅ Complete automation from zero to running application
