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
            "logger": { "type": "keyword" }
          }
        },
        "message": { "type": "text" },
        "process": {
          "properties": {
            "thread": {
              "properties": {
                "name": { "type": "keyword" }
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
