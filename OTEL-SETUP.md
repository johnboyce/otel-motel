# OpenTelemetry Setup Guide for otel-motel

This guide explains how to set up OpenTelemetry (OTEL) observability for the otel-motel Quarkus application, including distributed tracing, metrics collection, and log correlation.

## Prerequisites

- **Java 21** (required for building and running the application)
- Docker and Docker Compose (for running the observability stack)
- Maven 3.9.x or higher

## What is OpenTelemetry?

OpenTelemetry (OTEL) is an observability framework that provides:
- **Distributed Tracing**: Track requests across services
- **Metrics**: Collect application performance metrics
- **Log Correlation**: Link logs with traces for better debugging

## Architecture

The otel-motel application uses the following components:

1. **Quarkus OpenTelemetry Extension**: Auto-instruments the application
2. **OTLP Exporter**: Sends telemetry data to collectors
3. **Jaeger**: Receives, stores, and visualizes traces

## Quick Start

### 1. Start the Observability Stack

Start Jaeger using Docker Compose:

```bash
docker-compose up -d
```

Verify Jaeger is running:
- Jaeger UI: http://localhost:16686
- OTLP gRPC endpoint: http://localhost:4317
- OTLP HTTP endpoint: http://localhost:4318

### 2. Build the Application

Ensure you are using Java 21:

```bash
java -version
# Should output: openjdk version "21..."
```

Build the application:

```bash
./mvnw clean package
```

### 3. Run the Application

Run the application in development mode:

```bash
./mvnw quarkus:dev
```

Or run the packaged JAR:

```bash
java -jar target/quarkus-app/quarkus-run.jar
```

### 4. Generate Traces

Make requests to the GraphQL endpoint:

```bash
# Using curl with GraphQL query
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ sayHello(name: \"OpenTelemetry\") }"}'
```

Or visit the GraphQL UI at: http://localhost:8080/q/graphql-ui

### 5. View Traces in Jaeger

1. Open Jaeger UI: http://localhost:16686
2. Select "otel-motel" from the Service dropdown
3. Click "Find Traces"
4. Click on any trace to see detailed span information

## Configuration

The OpenTelemetry configuration is in `src/main/resources/application.properties`:

```properties
# Application name
quarkus.application.name=otel-motel

# OTLP Exporter endpoint (Jaeger)
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317
quarkus.otel.exporter.otlp.protocol=grpc

# Enable tracing
quarkus.otel.traces.enabled=true
quarkus.otel.traces.sampler=always_on

# Enable metrics
quarkus.otel.metrics.enabled=true

# Enable logs (for log correlation)
quarkus.otel.logs.enabled=true

# Resource attributes
quarkus.otel.resource.attributes=service.name=otel-motel,service.version=1.0-SNAPSHOT,deployment.environment=development
```

### Configuration Options

- **quarkus.otel.traces.sampler**: Controls trace sampling
  - `always_on`: Samples all traces (good for development)
  - `always_off`: Disables tracing
  - `traceidratio`: Samples a percentage of traces (e.g., `0.1` for 10%)

- **quarkus.otel.exporter.otlp.protocol**: Protocol for OTLP export
  - `grpc`: Uses gRPC (default, more efficient)
  - `http`: Uses HTTP/protobuf

## Verification Steps

### 1. Verify Application Startup

When the application starts, you should see OpenTelemetry initialization logs:

```
INFO  [io.quarkus] (main) otel-motel 1.0-SNAPSHOT on JVM (powered by Quarkus 3.27.0) started in X.XXXs.
```

### 2. Verify Trace Export

Check that traces are being exported:

```bash
# Make a request
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ sayHello }"}'

# Check Jaeger
# You should see traces in Jaeger UI within a few seconds
```

### 3. Verify Trace Details

In Jaeger UI, traces should show:
- Service name: `otel-motel`
- Operation names: GraphQL operations
- Span details: HTTP method, status code, GraphQL query
- Duration and timing information

## Docker Compose Services

### Jaeger All-in-One

The `docker-compose.yml` includes Jaeger all-in-one, which provides:

- **Jaeger Agent**: Receives traces from the application
- **Jaeger Collector**: Processes and stores traces
- **Jaeger Query**: Serves the Jaeger UI
- **Jaeger UI**: Web interface for viewing traces (port 16686)

Exposed ports:
- `16686`: Jaeger UI
- `4317`: OTLP gRPC receiver
- `4318`: OTLP HTTP receiver
- `14250`: Jaeger gRPC collector
- `6831`: Jaeger agent (UDP)

## Troubleshooting

### Application can't connect to Jaeger

**Problem**: Application logs show connection errors to `localhost:4317`

**Solution**:
1. Verify Jaeger is running: `docker-compose ps`
2. Check port is accessible: `nc -zv localhost 4317`
3. Restart Jaeger: `docker-compose restart jaeger`

### No traces appearing in Jaeger

**Problem**: Traces are not visible in Jaeger UI

**Solutions**:
1. Verify tracing is enabled in `application.properties`
2. Check sampler is set to `always_on` during development
3. Wait a few seconds for traces to be processed
4. Check Jaeger logs: `docker-compose logs jaeger`

### Java version mismatch

**Problem**: Build fails with Java version errors

**Solution**:
1. Verify Java 21 is installed: `java -version`
2. Set JAVA_HOME to Java 21:
   ```bash
   export JAVA_HOME=/path/to/java21
   export PATH=$JAVA_HOME/bin:$PATH
   ```

## Production Considerations

For production deployments:

1. **Sampling**: Adjust the sampling rate to reduce overhead
   ```properties
   quarkus.otel.traces.sampler=traceidratio
   quarkus.otel.traces.sampler.arg=0.1  # 10% sampling
   ```

2. **Endpoint**: Configure the OTLP endpoint for your production collector
   ```properties
   quarkus.otel.exporter.otlp.endpoint=https://your-otel-collector:4317
   ```

3. **Security**: Use TLS for OTLP export
   ```properties
   quarkus.otel.exporter.otlp.tls.enabled=true
   ```

4. **Resource Attributes**: Set appropriate environment labels
   ```properties
   quarkus.otel.resource.attributes=service.name=otel-motel,deployment.environment=production
   ```

## Advanced Features

### Custom Spans

You can create custom spans in your code:

```java
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyService {
    
    @Inject
    Tracer tracer;
    
    public void myMethod() {
        Span span = tracer.spanBuilder("my-operation").startSpan();
        try {
            // Your code here
        } finally {
            span.end();
        }
    }
}
```

### Custom Metrics

Add custom metrics using Micrometer (built into Quarkus):

```java
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyService {
    
    @Inject
    MeterRegistry registry;
    
    public void recordMetric() {
        registry.counter("my.custom.metric").increment();
    }
}
```

## Additional Resources

- [Quarkus OpenTelemetry Guide](https://quarkus.io/guides/opentelemetry)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)
- [OTLP Specification](https://opentelemetry.io/docs/specs/otlp/)

## Stopping the Stack

To stop the observability stack:

```bash
docker-compose down
```

To stop and remove all data:

```bash
docker-compose down -v
```

## Summary

You now have a fully functional OpenTelemetry setup for the otel-motel application with:
- ✅ Distributed tracing enabled
- ✅ Metrics collection configured
- ✅ Log correlation enabled
- ✅ Jaeger for visualization
- ✅ Java 21 as the build target

For questions or issues, refer to the [Quarkus OpenTelemetry Guide](https://quarkus.io/guides/opentelemetry).
