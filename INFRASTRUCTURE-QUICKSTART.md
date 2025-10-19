# Infrastructure Setup - Quick Reference

This document provides a quick overview of the infrastructure setup improvements made to the otel-motel project.

## What Was Added

### 1. Comprehensive Documentation

**INFRASTRUCTURE.md** - Complete guide explaining:
- How Keycloak automatically manages its database schema
- Detailed step-by-step infrastructure setup process
- Service health checks and dependencies
- Troubleshooting guides

**Updated docker/postgres/README.md** - Added detailed explanation of:
- How Keycloak DB schema is automatically loaded
- PostgreSQL initialization process
- Schema creation and migration by Keycloak (92+ tables)

### 2. New Make Tasks

#### `make infrastructure-up`
Complete infrastructure setup in one command:
- ✅ Starts all Docker services (PostgreSQL, Keycloak, DynamoDB, Elasticsearch, Kibana, OTEL Collector)
- ✅ Waits for all services to be healthy (with health check monitoring)
- ✅ Initializes Elasticsearch indices and ILM policies
- ✅ Creates DynamoDB tables (bookings, customers, hotels, rooms)
- ✅ Verifies all services are ready

**Usage:**
```bash
make infrastructure-up
```

**Time:** ~1-2 minutes for complete setup

#### `make app-ready`
Complete workflow from zero to running application:
- ✅ Runs `make infrastructure-up` (all above steps)
- ✅ Builds the application
- ✅ Starts the application in development mode

**Usage:**
```bash
make app-ready
```

**Time:** ~2-3 minutes total

### 3. Service Health Monitoring Script

**scripts/wait-for-services.sh** - Automated health check script:
- Monitors all services until healthy
- Provides real-time status updates
- Times out after 5 minutes with clear error messages
- Checks: PostgreSQL, DynamoDB, Elasticsearch, Kibana, Keycloak

## How Keycloak Schema Loading Works

### The Answer
**Keycloak automatically manages its own database schema.** You don't need to manually create tables.

### The Process

1. **PostgreSQL Initialization** (First startup)
   - Docker creates the `keycloak` database
   - `init-keycloak-db.sql` runs via entrypoint
   - Sets up user permissions and timezone
   - Database is ready but empty

2. **Keycloak Schema Creation** (First startup)
   - Keycloak connects to PostgreSQL
   - Detects empty database
   - **Automatically runs migration scripts**
   - Creates ~92 tables for realms, users, roles, clients, sessions, etc.

3. **Realm Import** (First startup)
   - Imports `otel-motel-realm.json`
   - Creates pre-configured users (user1, admin1)
   - Sets up roles and client configuration

4. **Subsequent Startups**
   - Schema already exists
   - Keycloak runs any version migrations
   - Data persists via Docker volumes

### Key Points
- ✅ **No manual table creation** - Keycloak does it all
- ✅ **Schema is version-controlled** - Managed by Keycloak
- ✅ **Data persists** - Docker volumes preserve state
- ✅ **Automatic on first run** - Just start the containers

## Quick Start Guide

### First Time Setup
```bash
# One command to set everything up and run the app
make app-ready
```

This will:
1. Pull and start all Docker images
2. Wait for services to be healthy
3. Initialize Elasticsearch
4. Create DynamoDB tables
5. Build the application
6. Start the application

Access the application at: http://localhost:8080/q/graphql-ui

### Infrastructure Only
```bash
# Set up infrastructure without building/running the app
make infrastructure-up
```

### Development Workflow
```bash
# Set up infrastructure once
make infrastructure-up

# Then run the app multiple times as needed
make dev
```

## Services and Endpoints

After running `make infrastructure-up`, these services are available:

| Service | Endpoint | Credentials |
|---------|----------|-------------|
| PostgreSQL | localhost:5432 | keycloak/keycloak |
| Keycloak | http://localhost:8180 | admin/admin |
| DynamoDB | localhost:4566 | (LocalStack) |
| Elasticsearch | http://localhost:9200 | (no auth) |
| Kibana | http://localhost:5601 | (no auth) |
| OTEL Collector | localhost:4318 (HTTP) | - |

## Prerequisites

You need these tools installed:
- **Docker & Docker Compose** - For containers
- **Java 17+** - For building/running the application
- **Maven 3.9+** - Included via wrapper (./mvnw)
- **AWS CLI** - For DynamoDB table creation with LocalStack

Install on macOS:
```bash
brew install openjdk@17 docker docker-compose awscli
```

Install on Linux:
```bash
sudo apt install openjdk-17-jdk docker.io docker-compose
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

## Testing the Setup

### Verify Infrastructure
```bash
# Check all services are running
make docker-ps

# Check service health
make elk-health

# List DynamoDB tables
make dynamodb-list-tables

# Connect to PostgreSQL
make postgres-console
```

### Verify Keycloak Schema
```bash
# Connect to PostgreSQL
make postgres-console

# In psql, run:
\dt                    # List all tables (should show ~92 Keycloak tables)
\d+ keycloak_role      # View a specific table structure
SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';
```

### Run Tests
```bash
# Infrastructure must be running first
make infrastructure-up

# Run tests
make test
```

All 32 tests should pass.

## Troubleshooting

### Services not healthy?
```bash
# Check logs
make docker-logs

# Check specific service
docker compose logs keycloak
docker compose logs postgres
```

### DynamoDB tables not created?
Make sure AWS CLI is installed:
```bash
aws --version
```

### Keycloak not starting?
- Wait 90 seconds (start_period in healthcheck)
- Check PostgreSQL is healthy first
- Check logs: `docker compose logs keycloak`

## Cleanup

```bash
# Stop all services
make docker-down

# Stop and remove volumes (⚠️ deletes all data)
make docker-down-volumes
```

## Documentation Links

- **[INFRASTRUCTURE.md](INFRASTRUCTURE.md)** - Complete infrastructure guide
- **[README.md](README.md)** - Main project documentation
- **[docker/postgres/README.md](docker/postgres/README.md)** - PostgreSQL and Keycloak setup
- **[docker/keycloak/README.md](docker/keycloak/README.md)** - Keycloak configuration

## Summary

The improvements provide:
1. **Clear documentation** on how Keycloak schema loading works
2. **One-command setup** for complete infrastructure
3. **Automated health checks** ensuring services are ready
4. **Better developer experience** with `make infrastructure-up` and `make app-ready`

You can now go from zero to a fully running application with a single command!
