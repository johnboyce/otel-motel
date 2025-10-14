# otel-motel 🏨

A modern hotel booking GraphQL server built with Quarkus, featuring comprehensive observability with OpenTelemetry and the ELK stack.

## 🌟 Features

- **GraphQL API** - Complete hotel booking system with queries and mutations
- **Database** - DynamoDB with LocalStack for local development
- **Sample Data** - Pre-loaded with 5 hotels, 10 customers, and ~50% booking capacity
- **Observability** - Full OpenTelemetry integration with ELK stack
- **ECS Compliance** - Elasticsearch logs follow Elastic Common Schema
- **HTTP/Protobuf** - OTLP export using modern HTTP/Protobuf protocol
- **Docker Compose** - Complete local development stack
- **Bruno Collection** - Ready-to-use API test collection
- **Makefile** - Structured task automation for developers

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (configured for Java 17)
- **Docker & Docker Compose**
- **Maven 3.9+** (included via wrapper)
- **Bruno** (optional, for API testing)

For macOS M3:
```bash
brew install openjdk@17 docker docker-compose
```

### 1. Start Infrastructure

```bash
make docker-up    # Start DynamoDB, ELK stack, OTEL Collector
make elk-setup    # Initialize Elasticsearch indices
```

### 2. Run Application

```bash
make dev          # Start in development mode with hot reload
```

### 3. Access Services

| Service | URL | Description |
|---------|-----|-------------|
| GraphQL UI | http://localhost:8080/q/graphql-ui | Interactive API explorer |
| GraphQL Endpoint | http://localhost:8080/graphql | API endpoint |
| Dev UI | http://localhost:8080/q/dev | Quarkus dev console |
| Kibana | http://localhost:5601 | Log and trace visualization |
| Elasticsearch | http://localhost:9200 | Data store |
| OTEL Collector | http://localhost:4318 | Telemetry collector |

## 📚 Documentation

- **[Quick Start Guide](docs/QUICKSTART.md)** - Get running in 5 minutes!
- **[Testing Guide](docs/TESTING.md)** - Comprehensive test scenarios
- **[GraphQL API Documentation](docs/graphql/README.md)** - Complete API reference
- **[Documentation Index](docs/README.md)** - All documentation organized
- **[Docker Configuration](docker/README.md)** - Container setup and OTEL Collector
- **[ELK Stack Guide](elk/README.md)** - Elasticsearch and Kibana setup
- **[Bruno API Collection](bruno/README.md)** - GraphQL API testing
- **[OpenTelemetry Setup](OTEL-SETUP.md)** - Observability configuration

## 🏗️ Architecture

```
┌─────────────────┐
│  GraphQL API    │
│  (Quarkus)      │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌──────┐  ┌──────────────┐
│  DB  │  │ OTEL         │
│(DDB) │  │ Collector    │
└──────┘  └──────┬───────┘
                 │
                 ▼
            ┌──────────┐
            │ Elastic  │
            │ search   │
            └────┬─────┘
                 │
                 ▼
            ┌─────────┐
            │ Kibana  │
            └─────────┘
```

## 📊 Data Model

### Entities

- **Hotel** - Hotel information (name, location, rating)
- **Room** - Room details (type, price, capacity)
- **Customer** - Customer profiles with secure payment info
- **Booking** - Reservations linking customers to rooms

### Sample Data

The system initializes with:
- **5 Hotels** across different US cities
- **108 Rooms** with various types and prices
- **10 Customers** with sample credit card details
- **~200 Bookings** spanning the next 3 months

## 🎯 GraphQL API

### Sample Queries

```graphql
# Get all hotels
query {
  hotels {
    id
    name
    city
    starRating
  }
}

# Check room availability
query {
  availableRooms(
    hotelId: 1,
    checkIn: "2025-11-01",
    checkOut: "2025-11-05"
  ) {
    roomNumber
    roomType
    pricePerNight
  }
}

# Get upcoming bookings
query {
  upcomingBookings {
    id
    checkInDate
    room {
      hotel {
        name
      }
    }
    customer {
      firstName
      lastName
    }
  }
}
```

### Sample Mutations

```graphql
# Create a booking
mutation {
  createBooking(
    roomId: 1,
    customerId: 1,
    checkInDate: "2025-11-15",
    checkOutDate: "2025-11-20",
    numberOfGuests: 2,
    specialRequests: "Late check-in please"
  ) {
    id
    totalPrice
    status
  }
}

# Cancel a booking
mutation {
  cancelBooking(bookingId: 1) {
    id
    status
  }
}
```

## 🔧 Makefile Commands

### Development
```bash
make help           # Show all available commands
make build          # Build the project
make compile        # Compile sources only
make dev            # Run in development mode
make test           # Run tests
```

### Docker & Infrastructure
```bash
make docker-up      # Start all services
make docker-down    # Stop all services
make docker-logs    # View logs
make elk-setup      # Initialize Elasticsearch
make elk-health     # Check ELK status
```

### Monitoring
```bash
make kibana-open    # Open Kibana in browser
make graphql-ui     # Open GraphQL UI
make elk-logs       # View recent logs
make otel-metrics   # View OTEL metrics
```

### Complete Workflows
```bash
make setup          # Complete setup (Docker + ELK)
make start          # Setup + run application
make stop           # Stop everything
make restart        # Full restart
```

## 📝 OpenTelemetry & ELK

### Observability Features

- **Distributed Tracing** - Track requests across the application
- **Structured Logging** - JSON logs with ECS compliance
- **Metrics Collection** - Application performance metrics
- **Log Correlation** - Link logs with traces via trace IDs

### ECS Compliance

All logs follow the Elastic Common Schema with fields:
- `@timestamp` - Event timestamp
- `service.name` - Service identifier
- `log.level` - Log severity
- `trace.id` - Distributed trace ID
- `span.id` - Span identifier
- `message` - Log message

### OTLP Protocol

Uses HTTP/Protobuf for efficient telemetry export:
```properties
quarkus.otel.exporter.otlp.endpoint=http://localhost:4318
quarkus.otel.exporter.otlp.protocol=http/protobuf
```

## 🗂️ Project Structure

```
otel-motel/
├── src/main/java/com/johnnyb/
│   ├── entity/              # DynamoDB entities (Hotel, Room, Booking, Customer)
│   ├── graphql/             # GraphQL resources and resolvers
│   └── service/             # Business logic and data initialization
├── src/main/resources/
│   └── application.properties  # Application configuration
├── docker/
│   ├── otel-collector-config.yaml  # OTEL Collector setup
│   └── README.md            # Docker documentation
├── elk/
│   ├── elasticsearch/       # ES index templates and scripts
│   └── README.md           # ELK documentation
├── bruno/                  # Bruno API collection
│   ├── Hotels/             # Hotel queries
│   ├── Rooms/              # Room queries
│   ├── Bookings/           # Booking operations
│   ├── Customers/          # Customer queries
│   └── README.md           # Bruno documentation
├── docker-compose.yml      # Local development stack
├── Makefile               # Task automation
└── README.md              # This file
```

## 🧪 Testing

### Run Tests
```bash
make test
make verify  # Includes integration tests
```

### API Testing with Bruno

1. Install Bruno: https://www.usebruno.com/
2. Open collection: `bruno/`
3. Execute requests

Or use the CLI:
```bash
bru run bruno/
```

### Manual Testing

Use the GraphQL UI at http://localhost:8080/q/graphql-ui for interactive testing.

## 🔨 Building

### Development Build
```bash
make build
```

### Native Build (requires GraalVM)
```bash
make build-native
```

## 🗄️ Database

### Connect to DynamoDB
```bash
make dynamodb-console      # Open DynamoDB CLI
make dynamodb-list-tables  # List all tables
```

### Reset Database
```bash
make dynamodb-reset
```

### Configuration
```properties
Endpoint: http://localhost:4566
Region: us-east-1
AWS Access Key: test
AWS Secret Key: test
```

## 📈 Monitoring & Observability

### View Traces in Kibana

1. Open Kibana: http://localhost:5601
2. Navigate to "Observability" → "APM"
3. Select "otel-motel" service
4. Explore traces and performance metrics

### Query Logs

```bash
# Recent application logs
make elk-logs

# All indices
make elk-indices
```

### Prometheus Metrics

```bash
# OTEL Collector metrics
make otel-metrics
```

## 🌍 Environments

Currently configured for **dev** environment:
- Local DynamoDB (LocalStack)
- Single-node Elasticsearch
- Debug logging enabled
- Hot reload enabled

Future considerations for **staging** and **prod**:
- AWS DynamoDB in cloud
- Elasticsearch cluster with replication
- Production logging levels
- Security and authentication
- Load balancing

## 🛠️ Development Tools

### For macOS M3 Developers

```bash
# Install tools
make install-tools

# Check dependencies
make check-deps
```

## 📖 Additional Resources

- [Quarkus Documentation](https://quarkus.io/guides/)
- [GraphQL Documentation](https://graphql.org/learn/)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Elastic Stack Documentation](https://www.elastic.co/guide/)
- [Bruno Documentation](https://docs.usebruno.com/)

## 🤝 Contributing

1. Follow existing code structure and style
2. Add tests for new features
3. Update documentation
4. Create Bruno requests for new API endpoints
5. Ensure `make verify` passes

## 📄 License

This project uses the Quarkus framework.

For more information about Quarkus, visit: https://quarkus.io/

---

**Built with** ❤️ **using Quarkus, OpenTelemetry, and the ELK Stack**
