# GELF Logging Quick Reference

Quick reference guide for using GELF logging with Vector in otel-motel.

## 🚀 Quick Start

```bash
# 1. Start infrastructure (includes Vector)
make infrastructure-up

# 2. Initialize Elasticsearch (includes GELF templates)
make elk-setup

# 3. Run application
make dev

# 4. View logs in Kibana
# Navigate to http://localhost:5601
# Create index pattern: otel-motel-gelf-logs-*
```

## 📊 Services

| Component | Port/Protocol | Description |
|-----------|---------------|-------------|
| Application | TCP 8080 | Quarkus application |
| Vector | UDP 12201 | GELF log receiver |
| Elasticsearch | TCP 9200 | Log storage |
| Kibana | TCP 5601 | Log visualization |

## ✅ Health Checks

```bash
# Check all services are running
docker compose ps

# Check Vector status specifically
docker compose ps vector

# Check Vector logs
docker compose logs vector

# Check Vector is receiving GELF messages
docker compose logs vector | grep "Processing GELF"

# Check Elasticsearch health
curl -s http://localhost:9200/_cluster/health?pretty

# Check if GELF indices exist
curl -s http://localhost:9200/_cat/indices/otel-motel-gelf-logs-*?v
```

## 🔍 Viewing Logs

### Via Elasticsearch API

```bash
# View most recent GELF logs
curl "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=5&sort=@timestamp:desc&pretty"

# Count GELF logs
curl "http://localhost:9200/otel-motel-gelf-logs-*/_count?pretty"

# Search for error logs
curl -X POST "http://localhost:9200/otel-motel-gelf-logs-*/_search?pretty" \
  -H 'Content-Type: application/json' \
  -d '{"query": {"match": {"log.level": "ERROR"}}}'
```

### Via Kibana

1. Open Kibana: http://localhost:5601
2. Navigate to **Discover**
3. Create index pattern: `otel-motel-gelf-logs-*`
4. Use the following queries:

```
# Error logs only
log.level: "ERROR"

# Logs from a specific logger
log.logger: "com.johnnyb.service.HotelService"

# Logs with stack traces
_exists_: error.stack_trace

# Recent logs (last 15 minutes)
@timestamp: [now-15m TO now]

# Logs from specific service
service.name: "otel-motel"
```

## 🔧 Configuration Files

| File | Purpose |
|------|---------|
| `application.properties` | GELF handler configuration |
| `docker/vector/vector.yaml` | Vector pipeline configuration |
| `docker-compose.yml` | Vector service definition |
| `elk/elasticsearch/templates/index-templates/otel-motel-gelf-logs-template.json` | Elasticsearch index template |
| `elk/elasticsearch/templates/ilm-policies/otel-motel-gelf-logs-policy.json` | Index lifecycle policy |
| `elk/elasticsearch/templates/ingest-pipelines/otel-motel-gelf-logs-ecs.json` | ECS mapping pipeline |

## 🐛 Troubleshooting

### Problem: No logs appearing in Elasticsearch

**Check 1**: Is Vector running?
```bash
docker compose ps vector
```

**Check 2**: Are there errors in Vector logs?
```bash
docker compose logs vector | grep -i error
```

**Check 3**: Is the application sending GELF logs?
```bash
# Check application logs for GELF errors
docker compose logs app 2>&1 | grep -i gelf
```

**Check 4**: Test GELF connectivity
```bash
# Send a test GELF message
echo '{"version":"1.1","host":"test","short_message":"Test message","level":6}' | \
  nc -u localhost 12201
  
# Check if it appears in Elasticsearch
curl "http://localhost:9200/otel-motel-gelf-logs-*/_search?q=short_message:Test&pretty"
```

**Check 5**: Is Elasticsearch healthy?
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### Problem: Vector shows "Failed deserializing frame" errors

**Solution 1**: Check GELF message format
- Ensure messages are valid GELF JSON
- Verify GELF version is 1.1

**Solution 2**: Check for network issues
```bash
# Check Vector network connectivity
docker compose exec vector ping -c 3 elasticsearch
```

**Solution 3**: Restart Vector
```bash
docker compose restart vector
```

### Problem: Logs appear but are missing fields

**Check 1**: Verify ingest pipeline is applied
```bash
curl "http://localhost:9200/_ingest/pipeline/otel-motel-gelf-logs-ecs?pretty"
```

**Check 2**: Check index template
```bash
curl "http://localhost:9200/_index_template/otel-motel-gelf-logs-template?pretty"
```

**Solution**: Re-run ELK setup
```bash
make elk-setup
```

### Problem: High memory usage by Vector

**Check**: Vector buffer size
```bash
docker compose logs vector | grep "buffer"
```

**Solution**: Adjust Vector buffer configuration in `docker/vector/vector.yaml`

### Problem: GELF logs not appearing but OTLP logs work

**Check 1**: Verify GELF handler is enabled
```bash
grep "quarkus.log.handler.gelf.enabled" src/main/resources/application.properties
```

**Check 2**: Verify GELF port is correct
```bash
grep "quarkus.log.handler.gelf.port" src/main/resources/application.properties
```

**Solution**: Ensure `quarkus.log.handler.gelf.enabled=true` and port is `12201`

## 📝 Log Fields Reference

### Standard GELF Fields

| Field | Description | Example |
|-------|-------------|---------|
| `version` | GELF version | `"1.1"` |
| `host` | Hostname | `"otel-motel"` |
| `timestamp` | Unix timestamp (ms) | `1698765432.123` |
| `level` | Syslog severity (0-7) | `3` (ERROR) |
| `short_message` | Short log message | `"Error processing booking"` |
| `full_message` | Full message with stack trace | `"Error processing...\n  at com..."` |

### ECS-Mapped Fields (in Elasticsearch)

| Field | Description | Example |
|-------|-------------|---------|
| `@timestamp` | ISO 8601 timestamp | `"2025-10-23T03:12:00.000Z"` |
| `service.name` | Service name | `"otel-motel"` |
| `service.version` | Service version | `"1.0-SNAPSHOT"` |
| `log.level` | Log level | `"ERROR"`, `"INFO"`, `"DEBUG"` |
| `log.logger` | Logger name | `"com.johnnyb.service.HotelService"` |
| `message` | Log message | `"Processing booking request"` |
| `host.name` | Hostname | `"otel-motel"` |
| `trace.id` | Trace ID | `"abc123..."` |
| `error.stack_trace` | Stack trace | `"java.lang.Exception..."` |

### Application-Specific Fields

| Field | Description | Example |
|-------|-------------|---------|
| `_LoggerName` | Logger class | `"com.johnnyb.service.HotelService"` |
| `_Thread` | Thread name | `"executor-thread-1"` |
| `_SourceClassName` | Source class | `"com.johnnyb.service.HotelService"` |
| `_SourceMethodName` | Source method | `"createBooking"` |

## 🎯 Common Use Cases

### Use Case 1: Debug a specific booking issue

```bash
# Find all logs related to a booking ID
# In Kibana Discover:
message: "booking-123"
```

### Use Case 2: Monitor error rates

```bash
# Count errors in last hour
curl -X POST "http://localhost:9200/otel-motel-gelf-logs-*/_search?pretty" \
  -H 'Content-Type: application/json' \
  -d '{
    "size": 0,
    "query": {
      "bool": {
        "must": [
          {"match": {"log.level": "ERROR"}},
          {"range": {"@timestamp": {"gte": "now-1h"}}}
        ]
      }
    },
    "aggs": {
      "errors_over_time": {
        "date_histogram": {
          "field": "@timestamp",
          "fixed_interval": "5m"
        }
      }
    }
  }'
```

### Use Case 3: Find logs with stack traces

```
# In Kibana:
_exists_: error.stack_trace
```

### Use Case 4: Trace a request across logs

```
# In Kibana, use the trace ID:
trace.id: "your-trace-id-here"
```

## 🔗 Related Documentation

- **[Complete GELF Logging Guide](GELF-LOGGING.md)** - Detailed setup and configuration
- **[Main README](../README.md)** - Project overview
- **[ELK Guide](../elk/README.md)** - Elasticsearch configuration
- **[Vector Documentation](https://vector.dev/docs/)** - Official Vector docs
- **[GELF Specification](https://docs.graylog.org/en/latest/pages/gelf.html)** - GELF protocol details

## 💡 Tips

1. **Use log levels appropriately**:
   - `ERROR`: Errors requiring attention
   - `WARN`: Potential issues
   - `INFO`: Important events
   - `DEBUG`: Detailed debugging info

2. **Include context in log messages**:
   - Use MDC for request/user context
   - Include relevant IDs (booking, customer, etc.)
   - Add trace IDs for correlation

3. **Monitor Vector health**:
   - Check Vector logs regularly
   - Monitor Vector memory usage
   - Set up alerts for Vector errors

4. **Index management**:
   - GELF logs are rotated daily
   - ILM policy keeps logs for 30 days
   - Use index patterns for searching across dates

5. **Performance**:
   - GELF uses UDP for minimal overhead
   - Some logs may be lost under extreme load
   - Consider TCP transport for critical logs (future enhancement)

## 🆘 Getting Help

If you're still experiencing issues:

1. Check the complete documentation: `docs/GELF-LOGGING.md`
2. Review Vector logs: `docker compose logs vector`
3. Check application logs: `docker compose logs app`
4. Verify Elasticsearch health: `make elk-health`
5. Try restarting services: `make docker-restart`

---

**Quick Commands Summary**:
```bash
# Status checks
docker compose ps vector
docker compose logs vector
make elk-health

# View logs
curl "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=5&sort=@timestamp:desc&pretty"

# Restart
docker compose restart vector
make elk-setup

# Troubleshoot
docker compose logs vector | grep -i error
```
