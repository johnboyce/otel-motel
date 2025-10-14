# Docker Configuration

This directory contains Docker and container orchestration configuration files for the otel-motel project.

## Contents

### `otel-collector-config.yaml`
OpenTelemetry Collector configuration that:
- Receives traces, metrics, and logs via OTLP (gRPC and HTTP/Protobuf)
- Processes telemetry data with batching and resource attributes
- Exports to Elasticsearch with ECS-compliant mapping
- Provides Prometheus metrics endpoint

## Services Architecture

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

## OTLP Endpoints

- **HTTP (Protobuf)**: `http://localhost:4318` - Used by Quarkus application
- **gRPC**: `http://localhost:4317` - Alternative protocol
- **Prometheus Metrics**: `http://localhost:8889/metrics`
- **Health Check**: `http://localhost:13133`

## Configuration Highlights

### Receivers
- OTLP protocol support for both gRPC and HTTP
- Configurable for multi-protocol telemetry ingestion

### Processors
- **Batch Processor**: Optimizes telemetry export with batching
- **Memory Limiter**: Prevents OOM by limiting memory usage
- **Resource Processor**: Enriches telemetry with service metadata

### Exporters
- **Elasticsearch**: Exports to ELK stack with ECS mapping
- **Logging**: Debug output for development
- **Prometheus**: Metrics exposition

## Customization

To modify the OTEL Collector configuration:

1. Edit `otel-collector-config.yaml`
2. Restart the collector: `make docker-restart`
3. Verify configuration: `docker logs otel-motel-collector`

## ECS Compliance

The collector is configured to use ECS (Elastic Common Schema) mapping mode for Elasticsearch exports, ensuring compatibility with Kibana dashboards and APM.

## Monitoring

Check collector health:
```bash
curl http://localhost:13133
```

View collector metrics:
```bash
curl http://localhost:8889/metrics
```

## References

- [OpenTelemetry Collector Documentation](https://opentelemetry.io/docs/collector/)
- [OTLP Specification](https://opentelemetry.io/docs/specs/otlp/)
- [Elasticsearch Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/elasticsearchexporter)
