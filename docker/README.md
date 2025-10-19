# Docker Configuration

This directory contains Docker and container orchestration configuration files for the otel-motel project.

## Directory Structure

```
docker/
├── README.md                           # This file
├── keycloak/                           # Keycloak configuration
│   ├── README.md                       # Keycloak setup documentation
│   └── otel-motel-realm.json         # Realm configuration with users and roles
├── otel-collector/                     # OpenTelemetry Collector configuration
│   ├── README.md                       # OTEL Collector documentation
│   └── otel-collector-config.yaml     # Collector configuration
└── postgres/                           # PostgreSQL configuration
    ├── README.md                       # PostgreSQL documentation
    └── init-keycloak-db.sql           # Database initialization script
```

## Contents

### `keycloak/`
Keycloak realm configuration for OAuth2/OIDC authentication and authorization:
- Pre-configured realm with users, roles, and client settings
- Imports automatically on container startup
- See [keycloak/README.md](./keycloak/README.md) for details

### `otel-collector/`
OpenTelemetry Collector configuration that:
- Receives traces, metrics, and logs via OTLP (gRPC and HTTP/Protobuf)
- Processes telemetry data with batching and resource attributes
- Exports to Elasticsearch with ECS-compliant mapping
- Provides Prometheus metrics endpoint
- See [otel-collector/README.md](./otel-collector/README.md) for details

### `postgres/`
PostgreSQL database configuration for Keycloak persistence:
- Initialization scripts for database setup
- Configured for Keycloak authentication data storage
- Provides persistent storage across container restarts
- See [postgres/README.md](./postgres/README.md) for details

## Services Architecture

```
                    ┌─────────────────┐
                    │  Quarkus App    │
                    │  (otel-motel)   │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
    ┌─────────┐      ┌─────────────┐     ┌──────────┐
    │ DynamoDB│      │   Keycloak  │     │   OTEL   │
    │ (Local) │      │   (Auth)    │     │Collector │
    └─────────┘      └──────┬──────┘     └─────┬────┘
                            │                   │
                            ▼                   │
                     ┌──────────┐               │
                     │PostgreSQL│               │
                     │(Keycloak)│               │
                     └──────────┘               │
                                                ▼
                                         ┌─────────────┐
                                         │Elasticsearch│
                                         └──────┬──────┘
                                                │
                                                ▼
                                           ┌────────┐
                                           │ Kibana │
                                           └────────┘
```

### Service Dependencies
- **PostgreSQL**: Standalone, starts first
- **DynamoDB**: Standalone, starts first
- **Elasticsearch**: Standalone, starts first
- **Keycloak**: Depends on PostgreSQL
- **Kibana**: Depends on Elasticsearch
- **OTEL Collector**: Depends on Elasticsearch
- **Quarkus App**: Depends on all services (managed separately)

## Service Details

### PostgreSQL
- **Port**: 5432
- **Purpose**: Keycloak data persistence
- **Database**: keycloak
- **Credentials**: keycloak/keycloak (⚠️ dev only)
- **Volume**: postgres_data

### DynamoDB (LocalStack)
- **Ports**: 4566 (gateway), 4571 (DynamoDB)
- **Purpose**: Application data storage
- **Region**: us-east-1
- **Volume**: .localstack

### Keycloak
- **Port**: 8180
- **Purpose**: OAuth2/OIDC authentication
- **Admin**: admin/admin
- **Database**: PostgreSQL (postgres:5432/keycloak)
- **Realm**: Auto-imported from keycloak/otel-motel-realm.json

### Elasticsearch
- **Ports**: 9200 (HTTP), 9300 (transport)
- **Purpose**: Log and trace storage
- **Mode**: Single-node
- **Volume**: elasticsearch_data

### Kibana
- **Port**: 5601
- **Purpose**: Visualization and exploration
- **Connected to**: Elasticsearch

### OTEL Collector
- **Ports**: 4317 (gRPC), 4318 (HTTP), 8888 (metrics), 13133 (health)
- **Purpose**: Telemetry collection and export
- **Config**: otel-collector/otel-collector-config.yaml

## OTLP Endpoints

- **HTTP (Protobuf)**: `http://localhost:4318` - Used by Quarkus application
- **gRPC**: `http://localhost:4317` - Alternative protocol
- **Prometheus Metrics**: `http://localhost:8889/metrics`
- **Health Check**: `http://localhost:13133`

## PostgreSQL Endpoints

- **Database**: `localhost:5432`
- **Console**: `psql -h localhost -U keycloak -d keycloak`
- **JDBC URL**: `jdbc:postgresql://localhost:5432/keycloak`

## Configuration Management

All service configurations are organized by service type:

### Modifying Configurations

**Keycloak**:
1. Edit files in `keycloak/`
2. See [keycloak/README.md](./keycloak/README.md) for details
3. Restart: `docker compose restart keycloak`

**OTEL Collector**:
1. Edit `otel-collector/otel-collector-config.yaml`
2. See [otel-collector/README.md](./otel-collector/README.md) for details
3. Restart: `docker compose restart otel-collector`
4. Verify: `docker logs otel-motel-collector`

**PostgreSQL**:
1. Edit files in `postgres/`
2. See [postgres/README.md](./postgres/README.md) for details
3. For init script changes: Remove volume and recreate
   ```bash
   docker compose down -v
   docker compose up -d
   ```

## Data Persistence

Data is persisted in Docker volumes:

| Volume | Purpose | Location |
|--------|---------|----------|
| postgres_data | Keycloak data | PostgreSQL /var/lib/postgresql/data |
| elasticsearch_data | Logs/traces | Elasticsearch /usr/share/elasticsearch/data |
| .localstack | DynamoDB data | LocalStack /var/lib/localstack |

### Managing Volumes

```bash
# List volumes
docker volume ls

# Inspect a volume
docker volume inspect otel-motel_postgres_data

# Remove all volumes (⚠️ deletes all data!)
docker compose down -v

# Backup a volume
docker run --rm -v otel-motel_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

## Starting Services

### Start All Services
```bash
# Using Makefile
make docker-up

# Using docker compose
docker compose up -d

# View logs
docker compose logs -f
```

### Start Specific Services
```bash
# Just database services
docker compose up -d postgres dynamodb

# Just observability stack
docker compose up -d elasticsearch kibana otel-collector

# Just authentication
docker compose up -d postgres keycloak
```

### Service Startup Order
Services start in dependency order:
1. PostgreSQL, DynamoDB, Elasticsearch (parallel)
2. Keycloak (waits for PostgreSQL), Kibana, OTEL Collector (wait for their dependencies)
3. Application (started separately with `make dev`)

## Stopping Services

```bash
# Stop all services (keep data)
make docker-down
# or
docker compose down

# Stop and remove volumes (⚠️ deletes data)
docker compose down -v

# Stop specific service
docker compose stop keycloak
```

## Monitoring

### Service Health

```bash
# Check all services
docker compose ps

# Check specific service health
docker inspect otel-motel-postgres | grep -A 10 Health
docker inspect otel-motel-keycloak | grep -A 10 Health

# Using Makefile
make elk-health
```

### Service Logs

```bash
# All services
make docker-logs

# Specific service
docker compose logs -f postgres
docker compose logs -f keycloak
docker compose logs -f otel-collector

# PostgreSQL initialization logs (verify init script ran)
make postgres-init-logs

# Using Makefile
make docker-logs-elk
make docker-logs-otel
```

## Troubleshooting

### Service Won't Start

**Check logs**:
```bash
docker compose logs [service-name]
```

**Common issues**:
- Port already in use: Change port mapping in docker-compose.yml
- Volume permission issues: Check file ownership
- Memory issues: Increase Docker memory allocation
- Network issues: Check if otel-network exists

### Keycloak Connection Issues

**Keycloak can't connect to PostgreSQL**:
```bash
# Check if postgres is running
docker compose ps postgres

# Check postgres logs
docker compose logs postgres

# Verify network connectivity
docker compose exec keycloak ping postgres
```

**Solution**: Ensure postgres starts before keycloak (handled by `depends_on`)

### Data Not Persisting

**Check volume mounts**:
```bash
# List volumes
docker volume ls | grep otel-motel

# Inspect volume
docker volume inspect otel-motel_postgres_data
```

**Verify data**:
```bash
# PostgreSQL
docker compose exec postgres psql -U keycloak -d keycloak -c "\dt"

# Elasticsearch
curl http://localhost:9200/_cat/indices
```

### Performance Issues

**Check resource usage**:
```bash
# View container stats
docker stats

# Check individual service
docker stats otel-motel-postgres
docker stats otel-motel-elasticsearch
```

**Solutions**:
- Increase Docker memory/CPU allocation
- Tune Elasticsearch heap size (ES_JAVA_OPTS)
- Optimize database queries
- Reduce log verbosity

### Network Connectivity

**Services can't communicate**:
```bash
# Check network
docker network inspect otel-motel_otel-network

# Test connectivity between services
docker compose exec keycloak ping postgres
docker compose exec otel-collector ping elasticsearch
```

## Production Considerations

⚠️ **DO NOT use these configurations in production!**

### Security Checklist
- [ ] Change all default passwords
- [ ] Use secrets management (Docker secrets, Kubernetes secrets)
- [ ] Enable TLS/SSL for all services
- [ ] Restrict network access (use internal networks)
- [ ] Use non-root users in containers
- [ ] Enable authentication for Elasticsearch
- [ ] Implement backup strategies
- [ ] Set up monitoring and alerting

### High Availability
- [ ] Use managed database services (RDS, CloudSQL)
- [ ] Set up Elasticsearch cluster with replicas
- [ ] Implement load balancing
- [ ] Configure health checks and auto-restart
- [ ] Use container orchestration (Kubernetes)
- [ ] Implement disaster recovery plan

### Performance
- [ ] Tune database connection pools
- [ ] Optimize Elasticsearch settings
- [ ] Use SSD for database volumes
- [ ] Monitor and optimize queries
- [ ] Implement caching strategies
- [ ] Scale services horizontally

## Environment Variables

Services use environment variables for configuration. See individual README files:
- [PostgreSQL Environment Variables](./postgres/README.md#environment-variables)
- [Keycloak Configuration](./keycloak/README.md#customization)
- [OTEL Collector Settings](./otel-collector/README.md#customization)

## Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker Networking](https://docs.docker.com/network/)
- [Docker Volumes](https://docs.docker.com/storage/volumes/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Keycloak Docker Guide](https://www.keycloak.org/server/containers)
- [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/)
- [Elasticsearch Docker](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html)

## Related Documentation

- [Keycloak Configuration](./keycloak/README.md)
- [PostgreSQL Setup](./postgres/README.md)
- [OTEL Collector Configuration](./otel-collector/README.md)
- [Main README](../README.md)
- [Quick Start Guide](../docs/QUICKSTART.md)
- [ELK Stack Guide](../elk/README.md)
