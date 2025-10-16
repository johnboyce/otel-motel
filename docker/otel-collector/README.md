# OpenTelemetry Collector Configuration

This directory contains the OpenTelemetry Collector configuration for the otel-motel project.

## Overview

The OpenTelemetry Collector receives, processes, and exports telemetry data (traces, metrics, and logs) from the Quarkus application to Elasticsearch for visualization in Kibana.

## Files

### `otel-collector-config.yaml`
Main configuration file for the OTEL Collector. See [otel-collector-config.yaml](./otel-collector-config.yaml) for detailed configuration.

## Architecture

```
┌─────────────────┐
│  Quarkus App    │
│  (otel-motel)   │
└────────┬────────┘
         │ OTLP HTTP/Protobuf (4318)
         ▼
┌─────────────────┐
│ OTEL Collector  │
│  (contrib)      │
└────────┬────────┘
         │
         ├──► Elasticsearch (Logs/Traces/Metrics)
         └──► Prometheus Metrics (8889)
```

## Endpoints

- **OTLP HTTP (Protobuf)**: `http://localhost:4318` - Used by Quarkus application
- **OTLP gRPC**: `http://localhost:4317` - Alternative protocol
- **Prometheus Metrics**: `http://localhost:8889/metrics` - Collector metrics
- **Health Check**: `http://localhost:13133` - Collector health status

## Configuration Highlights

### Receivers
- **OTLP**: Receives traces, metrics, and logs via both gRPC and HTTP protocols
- Supports binary protobuf encoding for efficiency

### Processors
- **Batch Processor**: Batches telemetry data for efficient export
- **Memory Limiter**: Prevents OOM by limiting memory usage
- **Resource Processor**: Enriches telemetry with service metadata

### Exporters
- **Elasticsearch**: Exports to ELK stack with ECS (Elastic Common Schema) mapping
- **Logging**: Debug output for development
- **Prometheus**: Exposes collector metrics

## Customization

To modify the OTEL Collector configuration:

1. Edit `otel-collector-config.yaml`
2. Restart the collector: `make docker-restart` or `docker compose restart otel-collector`
3. Verify configuration: `docker logs otel-motel-collector`

## Monitoring

### Check Collector Health
```bash
curl http://localhost:13133
```

### View Collector Metrics
```bash
curl http://localhost:8889/metrics
# or
make otel-metrics
```

### View Collector Logs
```bash
docker logs otel-motel-collector
# or
make docker-logs-otel
```

## Troubleshooting

### Collector Not Starting

```bash
# Check logs for configuration errors
docker logs otel-motel-collector

# Common issues:
# - Invalid YAML syntax
# - Missing required fields
# - Port conflicts
```

### Data Not Reaching Elasticsearch

```bash
# Check collector logs
docker logs otel-motel-collector

# Verify Elasticsearch is accessible
curl http://localhost:9200/_cluster/health

# Check if data is being received
docker logs otel-motel-collector | grep "Traces"
```

### High Memory Usage

```bash
# Check memory limiter settings in config
# Adjust batch processor settings:
# - timeout: How long to wait before sending a batch
# - send_batch_size: Maximum batch size
```

## ECS Compliance

The collector uses ECS (Elastic Common Schema) mapping mode for Elasticsearch exports, ensuring compatibility with Kibana dashboards and APM.

## Resources

- [OpenTelemetry Collector Documentation](https://opentelemetry.io/docs/collector/)
- [OTLP Specification](https://opentelemetry.io/docs/specs/otlp/)
- [Elasticsearch Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/elasticsearchexporter)
- [Collector Configuration Reference](https://opentelemetry.io/docs/collector/configuration/)

## Related Documentation

- [Docker Configuration](../README.md)
- [ELK Stack Guide](../../elk/README.md)
- [OpenTelemetry Setup](../../OTEL-SETUP.md)
