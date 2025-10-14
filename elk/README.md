# ELK Stack Configuration

This directory contains all configuration files and scripts for Elasticsearch and Kibana integration with OpenTelemetry.

## Directory Structure

```
elk/
├── elasticsearch/
│   └── setup-indices.sh      # Index template and ILM policy setup
└── README.md                 # This file
```

## Components

### Elasticsearch
**Version**: 8.11.1  
**Ports**: 9200 (HTTP), 9300 (Transport)

Central data store for logs, traces, and metrics with ECS-compliant schema.

#### Index Patterns
- `otel-motel-logs-*` - Application logs
- `otel-motel-traces-*` - Distributed traces
- `otel-motel-metrics-*` - Application metrics

### Kibana
**Version**: 8.11.1  
**Port**: 5601

Web UI for data visualization and exploration.

**Access**: http://localhost:5601

## Quick Start

### 1. Start the ELK Stack

```bash
make docker-up
```

### 2. Initialize Elasticsearch Indices

```bash
make elk-setup
```

This script creates:
- ECS-compliant index templates
- Index lifecycle management (ILM) policies
- Optimized mappings for logs, traces, and metrics

### 3. Verify Setup

```bash
make elk-health
```

## ECS (Elastic Common Schema) Compliance

All indices use ECS-compliant field mappings for seamless integration with Kibana APM and observability features.

### ECS Fields Used

```json
{
  "@timestamp": "ISO8601 date",
  "ecs.version": "8.0.0",
  "service.name": "otel-motel",
  "service.environment": "dev",
  "log.level": "info|debug|warn|error",
  "log.logger": "logger name",
  "message": "log message",
  "trace.id": "trace identifier",
  "span.id": "span identifier"
}
```

## Elasticsearch Setup Script

### `elasticsearch/setup-indices.sh`

Creates index templates with:
- ECS-compliant field mappings
- Optimized shard configuration
- Index lifecycle policies for log rotation

**Run manually**:
```bash
./elk/elasticsearch/setup-indices.sh
```

**Environment Variables**:
- `ELASTICSEARCH_HOST` - Elasticsearch URL (default: http://localhost:9200)

## OpenTelemetry Integration

The OTEL Collector exports data directly to Elasticsearch using the elasticsearch exporter:
- **Logs**: Exported to `otel-motel-logs` index
- **Traces**: Exported to `otel-motel-traces` index
- **Metrics**: Exported to `otel-motel-metrics` index

No Logstash is required - OTEL Collector handles ECS formatting natively.

## Index Lifecycle Management

Logs are automatically managed with ILM policies:

**Hot Phase**:
- Rollover after 7 days or 50GB
- Active indexing

**Delete Phase**:
- Delete after 30 days
- Saves storage space

## Kibana Setup

### Initial Setup

1. Open Kibana: http://localhost:5601
2. Navigate to "Stack Management" → "Index Patterns"
3. Create index patterns:
   - `otel-motel-logs-*`
   - `otel-motel-traces-*`
   - `otel-motel-metrics-*`
4. Set `@timestamp` as the time field

### Useful Kibana Features

- **Discover**: Search and filter logs
- **APM**: Distributed tracing visualization
- **Metrics**: Application performance metrics
- **Dashboard**: Custom visualizations

## Monitoring

### Check Cluster Health
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### List Indices
```bash
make elk-indices
```

### View Recent Logs
```bash
make elk-logs
```

## Troubleshooting

### Elasticsearch won't start
- Check Docker resources (needs ~2GB RAM)
- Verify port 9200 is not in use
- Check logs: `docker logs otel-motel-elasticsearch`

### Kibana connection failed
- Ensure Elasticsearch is healthy first
- Wait 30-60 seconds for Kibana to start
- Check logs: `docker logs otel-motel-kibana`

### Indices not created
- Run setup script: `make elk-setup`
- Check Elasticsearch health: `make elk-health`
- Verify templates: `curl http://localhost:9200/_index_template`

## Performance Tuning

### For Development (Current)
- Single node Elasticsearch
- No replication
- 512MB heap for Elasticsearch

### For Production
- Multi-node cluster
- Replication factor: 1-2
- Increase heap sizes
- Enable security (X-Pack)
- Use ILM for data retention

## Maintenance

### Delete Old Indices
```bash
make elk-delete-indices
```

### Reset Everything
```bash
make docker-down-volumes
make docker-up
make elk-setup
```

## References

- [Elasticsearch Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Kibana Documentation](https://www.elastic.co/guide/en/kibana/current/index.html)
- [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/current/index.html)
- [Index Lifecycle Management](https://www.elastic.co/guide/en/elasticsearch/reference/current/index-lifecycle-management.html)
- [OpenTelemetry Elasticsearch Exporter](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/exporter/elasticsearchexporter)
