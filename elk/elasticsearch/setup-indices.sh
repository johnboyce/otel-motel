#!/bin/bash
# Elasticsearch Index Setup Script for otel-motel
# This script creates ECS-compliant index templates and index lifecycle policies

set -e

ELASTICSEARCH_HOST="${ELASTICSEARCH_HOST:-http://localhost:9200}"

echo "Waiting for Elasticsearch to be ready..."
until curl -s "$ELASTICSEARCH_HOST/_cluster/health" > /dev/null; do
  echo "Elasticsearch is unavailable - sleeping"
  sleep 5
done

echo "Elasticsearch is up - creating index templates"

# Create index template for logs with ECS mapping
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-logs-template" \
  -H 'Content-Type: application/json' \
  -d '{
  "index_patterns": ["otel-motel-logs-*"],
  "priority": 500,
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.refresh_interval": "5s"
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "ecs": {
          "properties": {
            "version": { "type": "keyword" }
          }
        },
        "service": {
          "properties": {
            "name": { "type": "keyword" },
            "version": { "type": "keyword" },
            "environment": { "type": "keyword" }
          }
        },
        "log": {
          "properties": {
            "level": { "type": "keyword" },
            "logger": { "type": "keyword" },
            "origin": {
              "properties": {
                "file": {
                  "properties": {
                    "name": { "type": "keyword" },
                    "line": { "type": "integer" }
                  }
                },
                "function": { "type": "keyword" }
              }
            }
          }
        },
        "message": { "type": "text" },
        "process": {
          "properties": {
            "thread": {
              "properties": {
                "name": { "type": "keyword" },
                "id": { "type": "keyword" }
              }
            }
          }
        },
        "trace": {
          "properties": {
            "id": { "type": "keyword" }
          }
        },
        "span": {
          "properties": {
            "id": { "type": "keyword" }
          }
        },
        "event": {
          "properties": {
            "code": { "type": "keyword" },
            "action": { "type": "keyword" },
            "dataset": { "type": "keyword" },
            "original": { "type": "text" }
          }
        },
        "labels": {
          "type": "object"
        },
        "host": {
          "properties": {
            "name": { "type": "keyword" }
          }
        },
        "error": {
          "properties": {
            "message": { "type": "text" },
            "stack_trace": { "type": "text" },
            "type": { "type": "keyword" }
          }
        }
      }
    }
  }
}'

echo ""
echo "Created logs index template"

# Create index template for traces
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-traces-template" \
  -H 'Content-Type: application/json' \
  -d '{
  "index_patterns": ["otel-motel-traces-*"],
  "priority": 500,
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.refresh_interval": "5s"
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "trace": {
          "properties": {
            "id": { "type": "keyword" }
          }
        },
        "span": {
          "properties": {
            "id": { "type": "keyword" },
            "name": { "type": "keyword" }
          }
        },
        "service": {
          "properties": {
            "name": { "type": "keyword" }
          }
        },
        "duration": { "type": "long" }
      }
    }
  }
}'

echo ""
echo "Created traces index template"

# Create index template for metrics
curl -X PUT "$ELASTICSEARCH_HOST/_index_template/otel-motel-metrics-template" \
  -H 'Content-Type: application/json' \
  -d '{
  "index_patterns": ["otel-motel-metrics-*"],
  "priority": 500,
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.refresh_interval": "30s"
    },
    "mappings": {
      "properties": {
        "@timestamp": { "type": "date" },
        "service": {
          "properties": {
            "name": { "type": "keyword" }
          }
        },
        "metric": {
          "properties": {
            "name": { "type": "keyword" },
            "value": { "type": "double" }
          }
        }
      }
    }
  }
}'

echo ""
echo "Created metrics index template"

# Create Index Lifecycle Management (ILM) policy for log rotation
curl -X PUT "$ELASTICSEARCH_HOST/_ilm/policy/otel-motel-logs-policy" \
  -H 'Content-Type: application/json' \
  -d '{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_age": "7d",
            "max_size": "50gb"
          }
        }
      },
      "delete": {
        "min_age": "30d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}'

echo ""
echo "Created ILM policy for logs"

# Create ingest pipeline for ECS remapping
curl -X PUT "$ELASTICSEARCH_HOST/_ingest/pipeline/otel-motel-logs-ecs" \
  -H 'Content-Type: application/json' \
  -d '{
    "description": "Remap OpenTelemetry log Attributes and Resource fields to ECS fields and remove redundant fields",
    "processors": [
      { "set": { "field": "ecs.version", "value": "8.11.0" } },
      { "set": { "field": "process.thread.name", "value": "{{Attributes.thread.name}}", "if": "ctx.Attributes != null && ctx.Attributes.thread != null && ctx.Attributes.thread.name != null" } },
      { "set": { "field": "process.thread.id", "value": "{{Attributes.thread.id}}", "if": "ctx.Attributes != null && ctx.Attributes.thread != null && ctx.Attributes.thread.id != null" } },
      { "set": { "field": "log.logger", "value": "{{Attributes.log.logger.namespace}}", "if": "ctx.Attributes != null && ctx.Attributes.log != null && ctx.Attributes.log.logger != null && ctx.Attributes.log.logger.namespace != null" } },
      { "set": { "field": "event.code", "value": "{{Attributes.code.function}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.function != null" } },
      { "set": { "field": "event.action", "value": "{{Attributes.code.function}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.function != null" } },
      { "set": { "field": "event.dataset", "value": "{{Attributes.code.namespace}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.namespace != null" } },
      { "set": { "field": "log.origin.file.line", "value": "{{Attributes.code.lineno}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.lineno != null" } },
      { "set": { "field": "log.origin.function", "value": "{{Attributes.code.function}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.function != null" } },
      { "set": { "field": "log.origin.file.name", "value": "{{Attributes.code.namespace}}", "if": "ctx.Attributes != null && ctx.Attributes.code != null && ctx.Attributes.code.namespace != null" } },
      { "set": { "field": "labels.parent_id", "value": "{{Attributes.parentId}}", "if": "ctx.Attributes != null && ctx.Attributes.parentId != null" } },
      { "set": { "field": "host.name", "value": "{{Resource.host.name}}", "if": "ctx.Resource != null && ctx.Resource.host != null && ctx.Resource.host.name != null" } },
      { "set": { "field": "service.name", "value": "{{Resource.service.name}}", "if": "ctx.Resource != null && ctx.Resource.service != null && ctx.Resource.service.name != null" } },
      { "set": { "field": "service.version", "value": "{{Resource.service.version}}", "if": "ctx.Resource != null && ctx.Resource.service != null && ctx.Resource.service.version != null" } },
      { "set": { "field": "service.environment", "value": "{{Resource.deployment.environment}}", "if": "ctx.Resource != null && ctx.Resource.deployment != null && ctx.Resource.deployment.environment != null" } },
      { "set": { "field": "trace.id", "value": "{{TraceId}}", "if": "ctx.TraceId != null" } },
      { "set": { "field": "span.id", "value": "{{SpanId}}", "if": "ctx.SpanId != null" } },
      { "set": { "field": "log.level", "value": "{{SeverityText}}", "if": "ctx.SeverityText != null" } },
      { "set": { "field": "message", "value": "{{Body}}", "if": "ctx.Body != null" } },
      { "set": { "field": "event.original", "value": "{{Body}}", "if": "ctx.Body != null" } },
      { "set": { "field": "error.message", "value": "{{Attributes.error.message}}", "if": "ctx.Attributes != null && ctx.Attributes.error != null && ctx.Attributes.error.message != null" } },
      { "set": { "field": "error.stack_trace", "value": "{{Attributes.error.stack_trace}}", "if": "ctx.Attributes != null && ctx.Attributes.error != null && ctx.Attributes.error.stack_trace != null" } },
      { "set": { "field": "error.type", "value": "{{Attributes.error.type}}", "if": "ctx.Attributes != null && ctx.Attributes.error != null && ctx.Attributes.error.type != null" } },
      { "remove": { "field": "TraceId", "ignore_missing": true } },
      { "remove": { "field": "SpanId", "ignore_missing": true } },
      { "remove": { "field": "Body", "ignore_missing": true } },
      { "remove": { "field": "SeverityText", "ignore_missing": true } },
      { "remove": { "field": "Resource", "ignore_missing": true } },
      { "remove": { "field": "Attributes", "ignore_missing": true } }
    ]
  }'
echo ""
echo "Created ECS remapping ingest pipeline"

echo ""
echo "✅ Elasticsearch setup completed successfully!"
echo ""
echo "Index patterns created:"
echo "  - otel-motel-logs-*"
echo "  - otel-motel-traces-*"
echo "  - otel-motel-metrics-*"
echo ""
echo "Access Kibana at: http://localhost:5601"
echo "Access Elasticsearch at: $ELASTICSEARCH_HOST"
