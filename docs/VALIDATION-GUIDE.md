# Docker Services Validation Guide

This guide explains how to validate that all Docker services in the otel-motel project are running correctly.

## Quick Start

To validate all services at once:

```bash
make validate-services
```

Or run the validation script directly:

```bash
./scripts/validate-services.sh
```

## Services Validated

The validation script checks the following services:

1. **PostgreSQL** - Keycloak database
2. **DynamoDB (LocalStack)** - Application data storage
3. **Elasticsearch** - Log and trace storage
4. **Kibana** - Visualization dashboard
5. **OTEL Collector** - Telemetry collection
6. **Keycloak** - OAuth2/OIDC authentication

## Validation Steps

### PostgreSQL Validation

The script checks:
- Container is running
- Health status is healthy
- PostgreSQL is accepting connections
- Keycloak database exists and is accessible
- Database owner and privileges are correct
- No critical initialization errors

**Expected Output:**
```
✓ PostgreSQL container is running
✓ PostgreSQL is healthy
✓ PostgreSQL is accepting connections
✓ Keycloak database is accessible
✓ Database connection verified: keycloak|keycloak
✓ No critical errors in PostgreSQL logs
```

**Common Issues:**
- If PostgreSQL shows "not healthy", wait a few more seconds for initialization
- Database errors about "does not exist at character" during first startup are NORMAL (Keycloak checking for migration tables)

### Keycloak Validation

The script checks:
- Container is running
- Health status is healthy
- No database authentication failures
- Keycloak is listening on port 8180
- Health endpoint is responding
- No critical startup errors

**Expected Output:**
```
✓ Keycloak container is running
✓ Keycloak is healthy
✓ No database authentication errors detected
✓ Keycloak health endpoint is responding
✓ No critical errors in Keycloak logs
```

**Common Issues:**
- Keycloak takes 60-120 seconds to fully start
- "password authentication failed" errors indicate PostgreSQL isn't ready yet
- Database errors about "does not exist at character" during first startup are NORMAL

### DynamoDB Validation

The script checks:
- Container is running
- Health status is healthy
- DynamoDB API is responding

**Expected Output:**
```
✓ DynamoDB container is running
✓ DynamoDB is healthy
✓ DynamoDB API is responding
```

### Elasticsearch Validation

The script checks:
- Container is running
- Health status is healthy
- Cluster health is green or yellow

**Expected Output:**
```
✓ Elasticsearch container is running
✓ Elasticsearch is healthy
✓ Elasticsearch cluster health: "status":"green"
```

**Common Issues:**
- Cluster health may be yellow on single-node setups (this is expected)

### Kibana Validation

The script checks:
- Container is running
- Health status is healthy
- API is responding

**Expected Output:**
```
✓ Kibana container is running
✓ Kibana is healthy
✓ Kibana API is responding
```

### OTEL Collector Validation

The script checks:
- Container is running
- Health endpoint is responding

**Expected Output:**
```
✓ OTEL Collector container is running
ℹ OTEL Collector has no health check configured (using endpoint test instead)
✓ OTEL Collector health endpoint is responding
```

**Note:** The OTEL Collector image is minimal and doesn't support Docker healthchecks, but the service is validated via its HTTP endpoint.

## Validation Summary

At the end of validation, you'll see a summary like this:

```
Service Status:
  ✓ otel-motel-postgres: Running and Healthy
  ✓ otel-motel-keycloak: Running and Healthy
  ✓ otel-motel-dynamodb: Running and Healthy
  ✓ otel-motel-elasticsearch: Running and Healthy
  ✓ otel-motel-kibana: Running and Healthy
  ✓ otel-motel-collector: Running (no health check)

Service URLs:
  PostgreSQL:     localhost:5432 (keycloak/keycloak)
  Keycloak:       http://localhost:8180 (admin/admin)
  DynamoDB:       localhost:4566
  Elasticsearch:  http://localhost:9200
  Kibana:         http://localhost:5601
  OTEL Collector: http://localhost:4318 (HTTP) / http://localhost:4317 (gRPC)
```

## Manual Validation

You can also manually validate each service:

### PostgreSQL
```bash
# Check if running
docker ps | grep postgres

# Test connection
docker exec otel-motel-postgres pg_isready -U keycloak

# Check database
docker exec otel-motel-postgres psql -U keycloak -d keycloak -c "SELECT 1"
```

### Keycloak
```bash
# Check if running
docker ps | grep keycloak

# Check health endpoint
curl http://localhost:8180/health/ready

# Check for errors
docker logs otel-motel-keycloak 2>&1 | grep -i "error\|fatal"
```

### DynamoDB
```bash
# Check if running
docker ps | grep dynamodb

# Test API
docker exec otel-motel-dynamodb awslocal dynamodb list-tables
```

### Elasticsearch
```bash
# Check if running
docker ps | grep elasticsearch

# Check cluster health
curl http://localhost:9200/_cluster/health?pretty
```

### Kibana
```bash
# Check if running
docker ps | grep kibana

# Check API
curl http://localhost:5601/api/status
```

### OTEL Collector
```bash
# Check if running
docker ps | grep collector

# Check health endpoint
curl http://localhost:13133/
```

## Troubleshooting

### Services Not Starting

1. **Check Docker is running:**
   ```bash
   docker ps
   ```

2. **Check service logs:**
   ```bash
   docker logs otel-motel-<service-name>
   ```

3. **Restart a specific service:**
   ```bash
   docker compose restart <service-name>
   ```

4. **Full restart:**
   ```bash
   docker compose down -v
   docker compose up -d
   ```

### Keycloak Database Connection Issues

If Keycloak shows "password authentication failed":

1. Check PostgreSQL is healthy:
   ```bash
   docker exec otel-motel-postgres pg_isready -U keycloak
   ```

2. Verify database exists:
   ```bash
   docker exec otel-motel-postgres psql -U keycloak -d keycloak -c "SELECT 1"
   ```

3. Check init script ran:
   ```bash
   docker logs otel-motel-postgres | grep "Keycloak database initialized"
   ```

4. If still failing, recreate from scratch:
   ```bash
   docker compose down -v
   docker compose up -d
   ```

### Health Checks Failing

If services show as unhealthy but are actually running:

1. **Wait longer** - Some services take time to initialize (especially Elasticsearch and Keycloak)

2. **Check actual service health:**
   ```bash
   # PostgreSQL
   docker exec otel-motel-postgres pg_isready -U keycloak
   
   # Keycloak
   curl http://localhost:8180/health/ready
   
   # Elasticsearch
   curl http://localhost:9200/_cluster/health
   ```

3. **Increase healthcheck timing** in docker-compose.yml:
   - Increase `start_period`
   - Increase `retries`
   - Increase `interval`

## Best Practices

1. **Always validate after starting services:**
   ```bash
   docker compose up -d
   ./scripts/wait-for-services.sh
   ./scripts/validate-services.sh
   ```

2. **Use the Makefile for complete setup:**
   ```bash
   make infrastructure-up  # Starts services, waits, and validates
   ```

3. **Check logs regularly:**
   ```bash
   docker compose logs -f
   ```

4. **Monitor service health:**
   ```bash
   docker compose ps
   ```

5. **Clean restart when in doubt:**
   ```bash
   docker compose down -v && docker compose up -d
   ```

## Integration with CI/CD

The validation scripts can be used in CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Start services
  run: docker compose up -d

- name: Wait for services
  run: ./scripts/wait-for-services.sh

- name: Validate services
  run: ./scripts/validate-services.sh

- name: Run tests
  run: make test
```

## Related Documentation

- [Docker Configuration](./docker/README.md)
- [PostgreSQL Setup](./docker/postgres/README.md)
- [Keycloak Configuration](./docker/keycloak/README.md)
- [Main README](./README.md)
