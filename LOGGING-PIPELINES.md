# Three Logging Pipelines Configuration

This document explains the three separate logging pipelines available in otel-motel and how to control them.

## Overview

The otel-motel application supports three independent logging pipelines, each writing to a separate Elasticsearch index. You can enable or disable any combination of these pipelines based on your needs.

## The Three Pipelines

### Pipeline 1: OTEL Direct (quarkus → otel-collector → elasticsearch)
- **Index Pattern**: `otel-motel-otel-direct-logs-*`
- **Path**: Application → OpenTelemetry Collector → Elasticsearch
- **Protocol**: OTLP (OpenTelemetry Protocol)
- **Format**: ECS (Elastic Common Schema)
- **Control Property**: `logging.pipeline.otel-direct.enabled`
- **Environment Variable**: `LOGGING_PIPELINE_OTEL_DIRECT_ENABLED`

### Pipeline 2: GELF via Vector (quarkus → gelf → vector → elasticsearch)
- **Index Pattern**: `otel-motel-gelf-logs-*`
- **Path**: Application → GELF Handler → Vector → Elasticsearch
- **Protocol**: GELF over UDP (port 12201)
- **Format**: ECS-compliant, enriched by Vector
- **Control Property**: `logging.pipeline.gelf-vector.enabled`
- **Environment Variable**: `LOGGING_PIPELINE_GELF_VECTOR_ENABLED`

### Pipeline 3: OTEL via Vector (quarkus → otel-collector → vector → elasticsearch)
- **Index Pattern**: `otel-motel-otel-vector-logs-*`
- **Path**: Application → OpenTelemetry Collector → Vector → Elasticsearch
- **Protocol**: OTLP → gRPC (port 4319)
- **Format**: ECS-compliant, enriched by Vector with `pipeline.source=otel-vector`
- **Control Property**: `logging.pipeline.otel-vector.enabled`
- **Environment Variable**: `LOGGING_PIPELINE_OTEL_VECTOR_ENABLED`

## Enabling/Disabling Pipelines

### Default Behavior
By default, all three pipelines are **enabled**.

### Using Application Properties
Edit `src/main/resources/application.properties`:

```properties
# Enable all pipelines (default)
logging.pipeline.otel-direct.enabled=true
logging.pipeline.gelf-vector.enabled=true
logging.pipeline.otel-vector.enabled=true

# Example: Disable GELF pipeline
logging.pipeline.gelf-vector.enabled=false

# Example: Only enable OTEL Direct
logging.pipeline.otel-direct.enabled=true
logging.pipeline.gelf-vector.enabled=false
logging.pipeline.otel-vector.enabled=false
```

### Using Environment Variables
Set environment variables before running the application:

```bash
# Disable GELF pipeline
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false
make dev

# Or inline
LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false java -jar target/quarkus-app/quarkus-run.jar

# Disable multiple pipelines
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=false
make dev
```

### Using .env File
Create a `.env` file in the project root (see `.env.example`):

```bash
# Copy example
cp .env.example .env

# Edit .env
LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true
LOGGING_PIPELINE_GELF_VECTOR_ENABLED=true
LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=false
```

Note: Docker Compose automatically loads `.env` files.

## Verifying Pipeline Status

### Check Elasticsearch Indices
```bash
# List all otel-motel indices
curl http://localhost:9200/_cat/indices/otel-motel-*?v

# Check document count in each pipeline
curl "http://localhost:9200/otel-motel-otel-direct-logs/_count?pretty"
curl "http://localhost:9200/otel-motel-gelf-logs-*/_count?pretty"
curl "http://localhost:9200/otel-motel-otel-vector-logs-*/_count?pretty"
```

### View Logs from Each Pipeline
```bash
# OTEL Direct logs
curl -s "http://localhost:9200/otel-motel-otel-direct-logs/_search?size=5&sort=@timestamp:desc&pretty"

# GELF logs
curl -s "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=5&sort=@timestamp:desc&pretty"

# OTEL-Vector logs (note the pipeline.source field)
curl -s "http://localhost:9200/otel-motel-otel-vector-logs-*/_search?size=5&sort=@timestamp:desc&pretty"
```

### Check Vector Status
```bash
# View Vector logs
docker compose logs vector

# Check Vector health
docker compose ps vector
```

### Check OTEL Collector Status
```bash
# View collector logs
docker compose logs otel-collector

# Check collector metrics
curl http://localhost:8888/metrics
```

## Use Cases

### Use Case 1: High-Performance Production
**Enable**: GELF via Vector  
**Disable**: OTEL Direct, OTEL via Vector

GELF over UDP provides the lowest overhead for high-throughput applications.

```properties
logging.pipeline.otel-direct.enabled=false
logging.pipeline.gelf-vector.enabled=true
logging.pipeline.otel-vector.enabled=false
```

### Use Case 2: Comprehensive Observability
**Enable**: OTEL Direct  
**Disable**: GELF via Vector, OTEL via Vector

Use when you want full OpenTelemetry integration with traces and logs correlated.

```properties
logging.pipeline.otel-direct.enabled=true
logging.pipeline.gelf-vector.enabled=false
logging.pipeline.otel-vector.enabled=false
```

### Use Case 3: Advanced Log Processing
**Enable**: OTEL via Vector  
**Disable**: OTEL Direct, GELF via Vector

Use when you want Vector's powerful transformation capabilities on OTLP logs.

```properties
logging.pipeline.otel-direct.enabled=false
logging.pipeline.gelf-vector.enabled=false
logging.pipeline.otel-vector.enabled=true
```

### Use Case 4: Development/Testing (All Enabled)
**Enable**: All pipelines

Useful for comparing different pipelines or testing.

```properties
logging.pipeline.otel-direct.enabled=true
logging.pipeline.gelf-vector.enabled=true
logging.pipeline.otel-vector.enabled=true
```

### Use Case 5: Minimal Overhead
**Enable**: None (console only)

For development when you only need console logs.

```properties
logging.pipeline.otel-direct.enabled=false
logging.pipeline.gelf-vector.enabled=false
logging.pipeline.otel-vector.enabled=false
```

## Architecture Diagrams

### All Pipelines Enabled
```
┌─────────────────────┐
│   Quarkus App       │
│   (otel-motel)      │
└──────────┬──────────┘
           │
    ┌──────┴──────┬──────────────────┐
    │             │                  │
    ▼             ▼                  ▼
┌────────┐   ┌────────┐        ┌─────────┐
│ OTLP   │   │ GELF   │        │ OTLP    │
│ Logs   │   │ Handler│        │ Logs    │
└───┬────┘   └───┬────┘        └────┬────┘
    │            │                  │
    ▼            ▼                  ▼
┌────────────┐ ┌────────┐     ┌────────────┐
│ OTEL       │ │ Vector │     │ OTEL       │
│ Collector  │ │        │     │ Collector  │
└─────┬──────┘ └───┬────┘     └─────┬──────┘
      │            │                 │
      │            │                 ▼
      │            │            ┌────────┐
      │            │            │ Vector │
      │            │            └───┬────┘
      │            │                │
      └────────┬───┴────────────────┘
               │
               ▼
      ┌────────────────┐
      │ Elasticsearch  │
      │  3 Indices:    │
      │  1. otel-direct│
      │  2. gelf-logs  │
      │  3. otel-vector│
      └────────┬───────┘
               │
               ▼
          ┌────────┐
          │ Kibana │
          └────────┘
```

## Troubleshooting

### Pipeline Not Working

1. **Check if the service is running**:
   ```bash
   docker compose ps
   ```

2. **Check service logs**:
   ```bash
   docker compose logs vector
   docker compose logs otel-collector
   docker compose logs elasticsearch
   ```

3. **Verify configuration**:
   ```bash
   # Check application properties
   grep "logging.pipeline" src/main/resources/application.properties
   
   # Check environment variables
   env | grep LOGGING_PIPELINE
   ```

4. **Check Elasticsearch indices**:
   ```bash
   curl http://localhost:9200/_cat/indices/otel-motel-*?v
   ```

### No Logs in Specific Index

1. **Verify pipeline is enabled**:
   - Check `application.properties` or environment variables

2. **Check service connectivity**:
   ```bash
   # Test GELF port
   nc -zvu localhost 12201
   
   # Test OTEL Collector HTTP
   curl http://localhost:4318/v1/logs
   
   # Test Vector OTLP
   curl http://localhost:4319
   ```

3. **Check application logs**:
   ```bash
   # Look for errors related to GELF or OTLP
   tail -f /tmp/app.log | grep -i "gelf\|otlp\|error"
   ```

### High Log Volume

If you're experiencing high log volume:

1. **Disable redundant pipelines** - You typically only need one pipeline
2. **Adjust log levels** in `application.properties`:
   ```properties
   quarkus.log.level=WARN
   quarkus.log.category."com.johnnyb".level=INFO
   ```

3. **Configure sampling** in OTEL Collector (`docker/otel-collector/otel-collector-config.yaml`)

## Performance Considerations

| Pipeline | Latency | Overhead | Use Case |
|----------|---------|----------|----------|
| OTEL Direct | Medium | Medium | Observability with traces |
| GELF via Vector | Low | Low | High-throughput production |
| OTEL via Vector | Medium | Medium | Advanced log processing |

## Additional Resources

- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Vector Documentation](https://vector.dev/docs/)
- [GELF Specification](https://docs.graylog.org/docs/gelf)
- [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/current/index.html)
- [Main README](../README.md)
- [ELK Setup Guide](../elk/README.md)
- [GELF Logging Guide](docs/GELF-LOGGING.md)

## Quick Reference

```bash
# Enable all pipelines
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=true
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=true

# Enable only OTEL Direct
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=false

# Enable only GELF
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=false
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=true
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=false

# Enable only OTEL-Vector
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=false
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=true

# View logs from each pipeline in Kibana
# Create index patterns:
#   otel-motel-otel-direct-logs-*
#   otel-motel-gelf-logs-*
#   otel-motel-otel-vector-logs-*
```
