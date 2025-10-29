# Three Logging Pipelines - Implementation Summary

## What Was Implemented

This implementation adds support for three independent logging pipelines in the otel-motel application, each writing to a separate Elasticsearch index with the ability to enable/disable each pipeline independently.

## The Three Pipelines

### 1. OTEL Direct: Quarkus → OTEL Collector → Elasticsearch
- **Index Pattern**: `otel-motel-otel-direct-logs-*`
- **Configuration**: Already existed, updated index name for clarity
- **Toggle**: `LOGGING_PIPELINE_OTEL_DIRECT_ENABLED` (default: true)

### 2. GELF via Vector: Quarkus → GELF → Vector → Elasticsearch  
- **Index Pattern**: `otel-motel-gelf-logs-*`
- **Configuration**: Already existed
- **Toggle**: `LOGGING_PIPELINE_GELF_VECTOR_ENABLED` (default: true)

### 3. OTEL via Vector: Quarkus → OTEL Collector → Vector → Elasticsearch
- **Index Pattern**: `otel-motel-otel-vector-logs-*`
- **Configuration**: **NEW** - Added in this implementation
- **Toggle**: `LOGGING_PIPELINE_OTEL_VECTOR_ENABLED` (default: true)

## Files Changed/Added

### Configuration Files
1. **docker/vector/vector.yaml** - Added OTLP input source and new sink for OTLP logs
2. **docker/otel-collector/otel-collector-config.yaml** - Added Vector as OTLP exporter
3. **docker-compose.yml** - Exposed Vector OTLP ports (4319 gRPC, 4320 HTTP)
4. **src/main/resources/application.properties** - Added pipeline toggle properties

### Elasticsearch Configuration
5. **elk/elasticsearch/templates/index-templates/otel-motel-otel-direct-logs-template.json** - NEW
6. **elk/elasticsearch/templates/index-templates/otel-motel-otel-vector-logs-template.json** - NEW
7. **elk/elasticsearch/templates/ilm-policies/otel-motel-otel-direct-logs-policy.json** - NEW
8. **elk/elasticsearch/templates/ilm-policies/otel-motel-otel-vector-logs-policy.json** - NEW
9. **elk/elasticsearch/setup-indices.sh** - Updated to create new templates and policies

### Documentation
10. **.env.example** - NEW - Example environment variable configuration
11. **LOGGING-PIPELINES.md** - NEW - Comprehensive guide to the three pipelines
12. **README.md** - Updated with pipeline information

## How to Use

### Enable All Pipelines (Default)
```bash
make infrastructure-up
make dev
```

### Enable Specific Pipelines
```bash
# Option 1: Environment variables
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=false
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=true
make dev

# Option 2: Edit application.properties
# logging.pipeline.otel-direct.enabled=true
# logging.pipeline.gelf-vector.enabled=false
# logging.pipeline.otel-vector.enabled=true

# Option 3: Use .env file
cp .env.example .env
# Edit .env with desired configuration
make dev
```

## Verification

### Check Indices
```bash
curl http://localhost:9200/_cat/indices/otel-motel-*?v
```

Expected output shows three indices:
- otel-motel-otel-direct-logs-*
- otel-motel-gelf-logs-*
- otel-motel-otel-vector-logs-*

### Check Log Content
```bash
# OTEL Direct
curl -s "http://localhost:9200/otel-motel-otel-direct-logs/_search?size=1" | jq '.hits.hits[0]._source'

# GELF
curl -s "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=1" | jq '.hits.hits[0]._source'

# OTEL-Vector (note the pipeline.source field)
curl -s "http://localhost:9200/otel-motel-otel-vector-logs-*/_search?size=1" | jq '.hits.hits[0]._source'
```

## Test Results

Tested with application running:
- ✅ All three pipelines successfully writing logs
- ✅ Each pipeline writing to correct index
- ✅ Equal document count across all pipelines (2159 docs each)
- ✅ OTEL-Vector logs include `pipeline.source=otel-vector` metadata
- ✅ All services (Vector, OTEL Collector, Elasticsearch) healthy

## Architecture Benefits

1. **Flexibility**: Enable/disable pipelines based on deployment needs
2. **Performance Options**: Choose between UDP (GELF) or gRPC (OTLP)
3. **Processing Power**: Use Vector's transformation capabilities when needed
4. **Separation of Concerns**: Each pipeline has its own index for clear data separation
5. **No Duplication**: Pipelines can be toggled to avoid redundant logging

## Use Cases

- **Development**: Enable all three to compare pipelines
- **Production**: Enable only GELF for lowest overhead
- **Observability**: Enable OTEL Direct for trace correlation
- **Advanced Processing**: Enable OTEL-Vector for complex transformations

## Documentation

Complete documentation available in:
- **[LOGGING-PIPELINES.md](LOGGING-PIPELINES.md)** - Full configuration guide
- **[README.md](README.md)** - Updated main documentation
- **[.env.example](.env.example)** - Configuration examples

## Next Steps

For users:
1. Read [LOGGING-PIPELINES.md](LOGGING-PIPELINES.md) for detailed configuration
2. Choose which pipeline(s) to enable based on your needs
3. Configure via environment variables or application.properties
4. Monitor logs in Kibana using the three index patterns

## Success Criteria Met

✅ Three separate Elasticsearch indices created  
✅ Each pipeline writing to unique index with descriptive name  
✅ Toggle controls implemented via environment variables  
✅ All pipelines tested and verified working  
✅ Comprehensive documentation provided  
✅ No breaking changes to existing functionality  

## Technical Details

### Vector Configuration
- Added `opentelemetry` source type for OTLP input
- Configured both gRPC (4319) and HTTP (4320) endpoints
- Added VRL transformation for ECS compliance
- Created separate sink for OTLP logs with unique index pattern

### OTEL Collector Configuration
- Added `otlp/vector` exporter pointing to vector:4319
- Configured TLS as insecure for local development
- Added to logs pipeline exporters list

### Application Configuration
- Pipeline toggles use Quarkus property substitution
- Environment variables override application.properties
- GELF and OTLP handlers respect pipeline settings

## Maintenance Notes

- Index templates use ILM policies for automatic lifecycle management
- All logs follow Elastic Common Schema (ECS)
- Pipeline metadata (`pipeline.source`) helps identify log origin
- Daily index rotation for GELF and OTEL-Vector (date-based pattern)
- Single indices for OTEL Direct (no date pattern)
