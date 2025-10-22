# GELF Logging with Vector

This document describes the GELF (Graylog Extended Log Format) logging implementation in otel-motel using Vector as a log shipper to Elasticsearch.

## Overview

The otel-motel application now supports multiple logging pipelines:

1. **Console Logging**: JSON-formatted logs to console (ECS format)
2. **OpenTelemetry Logs**: Logs sent via OTLP to the OpenTelemetry Collector
3. **GELF Logs**: Logs sent via GELF protocol to Vector, then forwarded to Elasticsearch ⭐ NEW!

## Architecture

```
Quarkus Application
  ├─> Console (JSON/ECS)
  ├─> OpenTelemetry Collector (OTLP)
  └─> GELF (UDP:12201)
        │
        └─> Vector
              │
              └─> Elasticsearch (otel-motel-gelf-logs-*)
                    │
                    └─> Kibana
```

## Components

### 1. Quarkus GELF Handler

The application uses the `quarkus-logging-gelf` extension to send logs in GELF format.

**Configuration** (`application.properties`):
```properties
# GELF Logging Configuration for Vector
quarkus.log.handler.gelf.enabled=true
quarkus.log.handler.gelf.host=localhost
quarkus.log.handler.gelf.port=12201
quarkus.log.handler.gelf.facility=otel-motel
quarkus.log.handler.gelf.extract-stack-trace=true
quarkus.log.handler.gelf.include-full-mdc=true
quarkus.log.handler.gelf.skip-hostname-resolution=true
quarkus.log.handler.gelf.additional-field._service_name.value=otel-motel
quarkus.log.handler.gelf.additional-field._service_version.value=1.0-SNAPSHOT
quarkus.log.handler.gelf.additional-field._deployment_environment.value=dev
```

**Features**:
- Sends logs via UDP to minimize performance impact
- Includes full stack traces for exceptions
- Includes MDC (Mapped Diagnostic Context) data
- Adds custom service metadata fields
- Hostname resolution disabled for faster processing

### 2. Vector Log Processor

Vector receives GELF messages, processes them, and forwards to Elasticsearch.

**Configuration** (`docker/vector/vector.yaml`):

#### Source
```yaml
sources:
  gelf_input:
    type: "socket"
    mode: "udp"
    address: "0.0.0.0:12201"
    max_length: 8192
    decoding:
      codec: "bytes"
    framing:
      method: "bytes"
```

#### Transforms
1. **Decompression**: Handles GZIP-compressed GELF messages
2. **JSON Parsing**: Parses GELF JSON format
3. **Field Mapping**: Maps GELF fields to ECS-compatible structure

```yaml
transforms:
  decompress_gelf:
    type: "remap"
    inputs:
      - "gelf_input"
    source: |
      # Decompress GZIP if needed and parse JSON
      raw_bytes = .message
      decompressed, err = decode_gzip(raw_bytes)
      if err == null {
        .temp = parse_json!(decompressed)
      } else {
        .temp = parse_json!(to_string!(raw_bytes))
      }
      . = .temp
  
  parse_gelf:
    type: "remap"
    inputs:
      - "decompress_gelf"
    source: |
      # Map GELF fields to ECS structure
      # ...field mappings...
```

#### Sink
```yaml
sinks:
  elasticsearch_gelf:
    type: "elasticsearch"
    inputs:
      - "parse_gelf"
    endpoints:
      - "http://elasticsearch:9200"
    mode: "bulk"
    bulk:
      index: "otel-motel-gelf-logs-%Y.%m.%d"
      action: "create"
```

### 3. Elasticsearch Index

**Index Pattern**: `otel-motel-gelf-logs-*`

**Index Template** (`elk/elasticsearch/templates/index-templates/otel-motel-gelf-logs-template.json`):
- Daily index rotation (based on timestamp)
- ECS-compatible field mappings
- ILM policy attached for automatic lifecycle management

**ILM Policy** (`elk/elasticsearch/templates/ilm-policies/otel-motel-gelf-logs-policy.json`):
- **Hot phase**: Rollover after 7 days or 50GB
- **Warm phase**: Move to warm storage after 7 days, shrink to 1 shard
- **Delete phase**: Remove indices older than 30 days

**Ingest Pipeline** (`elk/elasticsearch/templates/ingest-pipelines/otel-motel-gelf-logs-ecs.json`):
- Adds ECS version information
- Normalizes log levels
- Adds event severity based on log level
- Sets event category and kind

## Log Fields

GELF logs in Elasticsearch include:

### Standard GELF Fields
- `version`: GELF version (1.1)
- `host`: Hostname where log originated
- `timestamp`: Unix timestamp with milliseconds
- `level`: Syslog severity level (0-7)
- `short_message`: Short log message
- `full_message`: Full log message (including stack traces)

### Application-Specific Fields
- `_LoggerName`: Java logger class name (e.g., `com.johnnyb.service.HotelService`)
- `_Severity`: Log level string (e.g., `INFO`, `ERROR`, `DEBUG`)
- `_SourceClassName`: Source class that generated the log
- `_SourceMethodName`: Source method that generated the log
- `_SourceSimpleClassName`: Simple name of source class
- `_Thread`: Thread name that generated the log
- `_Time`: Human-readable timestamp

### Custom Service Metadata
- `__service_name`: Service name (`otel-motel`)
- `__service_version`: Service version (`1.0-SNAPSHOT`)
- `__deployment_environment`: Deployment environment (`dev`)

### ECS-Mapped Fields
- `@timestamp`: ISO 8601 timestamp
- `service.name`: Service name
- `service.version`: Service version
- `service.environment`: Deployment environment
- `log.level`: Normalized log level
- `log.logger`: Logger name
- `message`: Log message
- `host.name`: Hostname
- `trace.id`: Trace ID (if present)
- `span.id`: Span ID (if present)
- `error.stack_trace`: Full stack trace for errors

## Setup

### Prerequisites
- Docker and Docker Compose
- Elasticsearch and Kibana running
- Vector service running

### Installation

1. **Start Infrastructure**:
```bash
make infrastructure-up
```

2. **Initialize Elasticsearch Indices**:
```bash
make elk-setup
```

This creates:
- GELF logs index template
- ILM policy for GELF logs
- Ingest pipeline for GELF logs

3. **Start Application**:
```bash
make dev
```

### Verification

1. **Check Vector Status**:
```bash
docker compose ps vector
docker compose logs vector
```

2. **Check GELF Logs in Elasticsearch**:
```bash
curl "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=5&sort=@timestamp:desc&pretty"
```

3. **View in Kibana**:
- Open Kibana: http://localhost:5601
- Navigate to **Discover**
- Create an index pattern: `otel-motel-gelf-logs-*`
- View real-time GELF logs

## Kibana Queries

### View Recent Logs
```
@timestamp: [now-15m TO now]
```

### Filter by Log Level
```
log.level: "ERROR"
```

### Filter by Logger
```
log.logger: "com.johnnyb.service.HotelService"
```

### Find Logs with Stack Traces
```
_exists_: error.stack_trace
```

### Filter by Service
```
service.name: "otel-motel" AND service.environment: "dev"
```

## Performance Considerations

### UDP Transport
- GELF uses UDP for minimal performance impact
- Fire-and-forget delivery (no ACKs)
- Suitable for high-volume logging
- Some log messages may be lost under extreme load

### Message Compression
- Large log messages are GZIP-compressed automatically
- Vector handles decompression transparently
- Reduces network bandwidth

### Chunking
- GELF automatically chunks messages > 8192 bytes
- Vector reassembles chunked messages
- Suitable for long stack traces

## Troubleshooting

### No Logs in Elasticsearch

1. **Check Vector is running**:
```bash
docker compose ps vector
```

2. **Check Vector logs for errors**:
```bash
docker compose logs vector | grep ERROR
```

3. **Test UDP connectivity**:
```bash
echo '{"version":"1.1","host":"test","short_message":"Test","level":6}' | nc -u localhost 12201
```

4. **Verify Elasticsearch index exists**:
```bash
curl "http://localhost:9200/_cat/indices/otel-motel-gelf-logs-*?v"
```

### Vector Parse Errors

If Vector shows "Failed deserializing frame" errors:
- GELF messages might be corrupted
- Check network MTU settings
- Verify GELF library compatibility

### Elasticsearch Connection Issues

Check Vector logs for:
```
ERROR ... elasticsearch_gelf ... connection refused
```

Ensure Elasticsearch is:
- Running and healthy
- Accessible from Vector container
- Not rejecting bulk requests

## Comparison with Other Logging Methods

| Feature | Console JSON | OTLP Logs | GELF Logs |
|---------|-------------|-----------|-----------|
| **Transport** | stdout | HTTP/gRPC | UDP |
| **Format** | ECS JSON | Protobuf | GELF JSON |
| **Performance** | Low overhead | Medium overhead | Low overhead |
| **Reliability** | High | High | Medium (UDP) |
| **Trace Correlation** | Yes | Yes | Yes |
| **Stack Traces** | Yes | Yes | Yes |
| **Structured Fields** | Yes | Yes | Yes |
| **Best For** | Development | Production observability | High-volume logging |

## Benefits of GELF Logging

1. **Performance**: UDP transport with minimal overhead
2. **Standardization**: GELF is an industry-standard format
3. **Compatibility**: Works with Graylog, Vector, Logstash, Fluentd
4. **Rich Metadata**: Includes source class, method, thread information
5. **Compression**: Automatic compression for large messages
6. **Separation**: Independent logging pipeline from OTLP
7. **Flexibility**: Can route logs to multiple destinations via Vector

## Future Enhancements

Potential improvements:
- Add TCP transport option for reliable delivery
- Implement log sampling for high-volume scenarios
- Add custom filters in Vector for log routing
- Integrate with alerting systems
- Add log aggregation and analytics dashboards

## References

- [GELF Specification](https://docs.graylog.org/en/latest/pages/gelf.html)
- [Vector Documentation](https://vector.dev/docs/)
- [Quarkus Logging GELF](https://quarkus.io/guides/logging#logging-adapters)
- [Elastic Common Schema](https://www.elastic.co/guide/en/ecs/current/index.html)
