# OpenTelemetry Setup for otel-motel

This application has been configured with OpenTelemetry support for distributed tracing, metrics, and logs.

## Configuration

The OpenTelemetry configuration is set in `src/main/resources/application.properties`:

```properties
# OpenTelemetry Configuration
quarkus.application.name=otel-motel

# Enable OpenTelemetry tracing
quarkus.otel.traces.enabled=true
quarkus.otel.metrics.enabled=true
quarkus.otel.logs.enabled=true

# OTLP Exporter Configuration (defaults to http://localhost:4317)
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317

# Service name for tracing
quarkus.otel.service.name=otel-motel

# Trace all requests
quarkus.otel.traces.sampler=always_on
```

## Running with OpenTelemetry Collector

To visualize traces, metrics, and logs, you need to run an OpenTelemetry Collector. Here's a quick setup using Docker:

### 1. Create an OpenTelemetry Collector configuration

Create a file `otel-collector-config.yaml`:

```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:

exporters:
  logging:
    loglevel: debug
  # Add other exporters like Jaeger, Zipkin, or Prometheus as needed

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging]
    metrics:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging]
    logs:
      receivers: [otlp]
      processors: [batch]
      exporters: [logging]
```

### 2. Run the OpenTelemetry Collector

```bash
docker run -d --name otel-collector \
  -p 4317:4317 \
  -p 4318:4318 \
  -v $(pwd)/otel-collector-config.yaml:/etc/otel-collector-config.yaml \
  otel/opentelemetry-collector:latest \
  --config=/etc/otel-collector-config.yaml
```

### 3. Run the application

```bash
./mvnw quarkus:dev
```

## Using with Jaeger

For a complete observability setup with Jaeger, you have two options:

### Option 1: Using docker-compose (Recommended)

```bash
# Start Jaeger with docker-compose
docker-compose up -d

# Run the application
./mvnw quarkus:dev
```

### Option 2: Using docker run

```bash
# Start Jaeger all-in-one
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest

# Run the application
./mvnw quarkus:dev
```

Access Jaeger UI at: http://localhost:16686

## Testing the Application

Once the application is running, you can test the GraphQL endpoint:

```bash
# Test the GraphQL endpoint
curl -X POST -H "Content-Type: application/json" \
  -d '{"query":"{ sayHello(name: \"OpenTelemetry\") }"}' \
  http://localhost:8080/graphql
```

Or access the GraphQL UI at: http://localhost:8080/q/graphql-ui/

## Customizing OpenTelemetry

You can customize the OpenTelemetry configuration by modifying `application.properties`. For example:

- Change the exporter endpoint
- Adjust sampling rates
- Configure specific exporters
- Add custom resource attributes

See the [Quarkus OpenTelemetry Guide](https://quarkus.io/guides/opentelemetry) for more configuration options.

## Benefits

With OpenTelemetry enabled, you get:

1. **Distributed Tracing**: Track requests across services
2. **Metrics Collection**: Monitor application performance
3. **Log Correlation**: Correlate logs with traces
4. **Standards-Based**: Use OpenTelemetry standard for vendor-neutral observability
