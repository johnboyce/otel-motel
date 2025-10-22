#!/bin/bash
# Elasticsearch Index Setup Script for otel-motel
# This script creates ECS-compliant index templates and index lifecycle policies

set -e

ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEMPLATES_DIR="$SCRIPT_DIR/templates"

echo "Waiting for Elasticsearch to be ready..."
until curl -s "$ELASTICSEARCH_HOST/_cluster/health" > /dev/null; do
  echo "Elasticsearch is unavailable - sleeping"
  sleep 5
done

echo "Elasticsearch is up - creating index templates"

# Create index template for logs with ECS mapping
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-logs-template" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/index-templates/otel-motel-logs-template.json"

echo ""
echo "Created logs index template"

# Create index template for OTLP logs with ECS mapping
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-otlp-logs-template" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/index-templates/otel-motel-otlp-logs-template.json"

echo ""
echo "Created OTLP logs index template"

# Create index template for traces
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-traces-template" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/index-templates/otel-motel-traces-template.json"

echo ""
echo "Created traces index template"

# Create index template for metrics
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-metrics-template" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/index-templates/otel-motel-metrics-template.json"

echo ""
echo "Created metrics index template"

# Create Index Lifecycle Management (ILM) policy for log rotation
curl -X PUT "$ELASTICSEARCH_HOST/_ilm/policy/otel-motel-logs-policy" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ilm-policies/otel-motel-logs-policy.json"

echo ""
echo "Created ILM policy for logs"

# Create Index Lifecycle Management (ILM) policy for OTLP log rotation
curl -X PUT "$ELASTICSEARCH_HOST/_ilm/policy/otel-motel-otlp-logs-policy" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ilm-policies/otel-motel-otlp-logs-policy.json"

echo ""
echo "Created ILM policy for OTLP logs"

# Create ingest pipeline for ECS remapping
curl -X PUT "$ELASTICSEARCH_HOST/_ingest/pipeline/otel-motel-logs-ecs" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ingest-pipelines/otel-motel-logs-ecs.json"
echo ""
echo "Created ECS remapping ingest pipeline"

# Create ingest pipeline for OTLP logs ECS remapping
curl -X PUT "$ELASTICSEARCH_HOST/_ingest/pipeline/otel-motel-otlp-logs-ecs" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ingest-pipelines/otel-motel-otlp-logs-ecs.json"
echo ""
echo "Created OTLP logs ECS remapping ingest pipeline"

# Create index template for GELF logs with ECS mapping
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-gelf-logs-template" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/index-templates/otel-motel-gelf-logs-template.json"

echo ""
echo "Created GELF logs index template"

# Create Index Lifecycle Management (ILM) policy for GELF log rotation
curl -X PUT "$ELASTICSEARCH_HOST/_ilm/policy/otel-motel-gelf-logs-policy" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ilm-policies/otel-motel-gelf-logs-policy.json"

echo ""
echo "Created ILM policy for GELF logs"

# Create ingest pipeline for GELF logs ECS remapping
curl -X PUT "$ELASTICSEARCH_HOST/_ingest/pipeline/otel-motel-gelf-logs-ecs" \
  -H 'Content-Type: application/json' \
  -d @"$TEMPLATES_DIR/ingest-pipelines/otel-motel-gelf-logs-ecs.json"
echo ""
echo "Created GELF logs ECS remapping ingest pipeline"

echo ""
echo "✅ Elasticsearch setup completed successfully!"
echo ""
echo "Index patterns created:"
echo "  - otel-motel-logs-*"
echo "  - otel-motel-otlp-logs-*"
echo "  - otel-motel-gelf-logs-*"
echo "  - otel-motel-traces-*"
echo "  - otel-motel-metrics-*"
echo ""
echo "Access Kibana at: http://localhost:5601"
echo "Access Elasticsearch at: $ELASTICSEARCH_HOST"
