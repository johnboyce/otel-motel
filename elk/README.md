# ELK Stack Configuration

This directory contains all configuration files and scripts for Elasticsearch and Kibana integration with OpenTelemetry.

## Directory Structure

```
elk/
├── elasticsearch/
│   ├── setup-indices.sh      # Index template and ILM policy setup
│   └── templates/            # Externalized JSON templates
│       ├── index-templates/  # Index template definitions
│       │   ├── otel-motel-logs-template.json
│       │   ├── otel-motel-traces-template.json
│       │   └── otel-motel-metrics-template.json
│       ├── ilm-policies/     # Index lifecycle management policies
│       │   └── otel-motel-logs-policy.json
│       └── ingest-pipelines/ # Data processing pipelines
│           └── otel-motel-logs-ecs.json
└── README.md                 # This file
```

## Components

### Elasticsearch
**Version**: 8.11.1  
**Ports**: 9200 (HTTP), 9300 (Transport)

Central data store for logs, traces, and metrics with ECS-compliant schema.

#### Index Patterns
- `otel-motel-logs-*` - Application logs
- `otel-motel-otlp-logs-*` - OTLP protocol logs
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

### ECS Fields Used and Mapping Status

The following table summarizes the ECS fields mapped in the `otel-motel-logs` index and their status:

| ECS Field                  | Present | Source / Mapping Description                       |
|----------------------------|---------|---------------------------------------------------|
| `@timestamp`               | Yes     | Log timestamp (from OTEL/collector)                |
| `ecs.version`              | Yes     | Set by ingest pipeline (`8.11.0`)                  |
| `service.name`             | Yes     | `Resource.service.name` → `service.name`           |
| `service.version`          | Yes     | `Resource.service.version` → `service.version`     |
| `service.environment`      | Yes     | `Resource.deployment.environment` → `service.environment` |
| `log.level`                | Yes     | `SeverityText` or OTEL log level                   |
| `log.logger`               | Yes     | `Attributes.log.logger.namespace`                  |
| `log.origin.file.name`     | Yes     | `Attributes.code.namespace`                        |
| `log.origin.file.line`     | Yes     | `Attributes.code.lineno` (as integer)              |
| `log.origin.function`      | Yes     | `Attributes.code.function`                         |
| `message`                  | Yes     | `Body` or OTEL log message                         |
| `trace.id`                 | Yes     | `TraceId`                                         |
| `span.id`                  | Yes     | `SpanId`                                          |
| `process.thread.name`      | Yes     | `Attributes.thread.name`                           |
| `process.thread.id`        | Yes     | `Attributes.thread.id`                             |
| `event.code`               | Yes     | `Attributes.code.function`                         |
| `event.action`             | Yes     | `Attributes.code.function`                         |
| `event.dataset`            | Yes     | `Attributes.code.namespace`                        |
| `event.original`           | Yes     | `Body` (raw log message)                           |
| `host.name`                | Yes     | `Resource.host.name`                               |
| `labels.parent_id`         | Yes     | `Attributes.parentId`                              |
| `error.message`            | If present | `Attributes.error.message`                      |
| `error.stack_trace`        | If present | `Attributes.error.stack_trace`                  |
| `error.type`               | If present | `Attributes.error.type`                         |

**Note:**
- All raw OpenTelemetry fields (e.g., `Resource`, `Attributes`, `Body`, `SeverityText`) are removed after mapping.
- Any non-ECS fields (e.g., `TraceFlags`, `SeverityNumber`, `Scope`) are candidates for removal for strict ECS compliance.
- The ingest pipeline ensures all ECS fields are populated and formatted as required.

### What Was Accomplished

- **Full ECS mapping:** All log fields are now mapped to ECS, including code location, trace, span, and error fields.
- **Ingest pipeline:** Automatically remaps OpenTelemetry fields to ECS and removes non-ECS fields.
- **Index template:** Ensures all ECS fields are mapped with correct types for Kibana and Elastic integrations.
- **No Logstash required:** The OTEL Collector and ingest pipeline handle all ECS formatting.
- **Ready for Kibana:** Logs are fully compatible with Kibana Discover, APM, and dashboards.

## Elasticsearch Setup Script

### `elasticsearch/setup-indices.sh`

Creates index templates with:
- ECS-compliant field mappings
- Optimized shard configuration
- Index lifecycle policies for log rotation
- Ingest pipelines for data transformation

**Run manually**:
```bash
./elk/elasticsearch/setup-indices.sh
```

**Environment Variables**:
- `ELASTICSEARCH_HOST` - Elasticsearch URL (default: http://localhost:9200)

### Template Files

All Elasticsearch configurations are now externalized as JSON files in the `templates/` directory:

#### Index Templates (`templates/index-templates/`)
- **otel-motel-logs-template.json** - Defines the schema for application logs with full ECS field mappings
- **otel-motel-otlp-logs-template.json** - Defines the schema for OTLP protocol logs with full ECS field mappings
- **otel-motel-traces-template.json** - Defines the schema for distributed traces
- **otel-motel-metrics-template.json** - Defines the schema for application metrics

#### ILM Policies (`templates/ilm-policies/`)
- **otel-motel-logs-policy.json** - Lifecycle policy for automatic log rotation and deletion (7 day rollover, 30 day retention)
- **otel-motel-otlp-logs-policy.json** - Lifecycle policy for automatic OTLP log rotation and deletion (7 day rollover, 30 day retention)

#### Ingest Pipelines (`templates/ingest-pipelines/`)
- **otel-motel-logs-ecs.json** - Pipeline that remaps OpenTelemetry fields to ECS standard format and removes redundant fields
- **otel-motel-otlp-logs-ecs.json** - Pipeline that remaps OpenTelemetry OTLP fields to ECS standard format and removes redundant fields

These files can be edited directly to customize Elasticsearch behavior without modifying the setup script.

## OpenTelemetry Integration

The OTEL Collector exports data directly to Elasticsearch using the elasticsearch exporter:
- **Logs**: Exported to `otel-motel-logs` index
- **OTLP Logs**: Exported to `otel-motel-otlp-logs` index (dedicated OTLP protocol logs)
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
   - `otel-motel-otlp-logs-*`
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
