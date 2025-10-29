# otel-motel 🏨

A modern hotel booking GraphQL server built with Quarkus, featuring comprehensive observability with OpenTelemetry and the ELK stack.

## 🌟 Features

- **Modern Web UI** - Professional React-based interface with Quinoa integration ⭐ NEW!
- **GraphQL API** - Complete hotel booking system with queries and mutations
- **OAuth2/OIDC Security** - JWT-based authentication with role-based access control (via Keycloak)
- **Keycloak Authentication** - Persistent user authentication with PostgreSQL backend
- **Database** - DynamoDB with LocalStack for application data, PostgreSQL for Keycloak
- **Sample Data** - Pre-loaded with 5 hotels, 10 customers, and ~50% booking capacity
- **Three Logging Pipelines** - Independent logging paths with toggle controls ⭐ NEW!
- **Observability** - Full OpenTelemetry integration with ELK stack
- **GELF Logging** - High-performance logging with Vector log shipper
- **ECS Compliance** - Elasticsearch logs follow Elastic Common Schema
- **HTTP/Protobuf** - OTLP export using modern HTTP/Protobuf protocol
- **Docker Compose** - Complete local development stack with organized structure
- **Bruno Collection** - Ready-to-use API test collection with authentication
- **Makefile** - Structured task automation for developers

## 🚀 Quick Start

### Prerequisites

- **Java 17+** (configured for Java 17)
- **Docker & Docker Compose**
- **Maven 3.9+** (included via wrapper)
- **AWS CLI** (for DynamoDB table creation with LocalStack)
- **Bruno** (optional, for API testing)

For macOS:
```bash
brew install openjdk@17 docker docker-compose awscli
```

For Linux (Ubuntu/Debian):
```bash
# Install Java
sudo apt update
sudo apt install openjdk-17-jdk

# Install Docker & Docker Compose (follow official Docker documentation)
# https://docs.docker.com/engine/install/

# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

### One-Command Setup (Recommended)

```bash
make app-ready    # Sets up infrastructure, builds, and runs the application
```

This single command:
- ✅ Starts all Docker services (PostgreSQL, Keycloak, DynamoDB, ELK, OTEL Collector)
- ✅ Waits for all services to be healthy
- ✅ Initializes Elasticsearch indices
- ✅ Creates DynamoDB tables
- ✅ Builds the application
- ✅ Starts the application in development mode

**Or, for infrastructure only:**
```bash
make infrastructure-up    # Complete infrastructure setup with health checks
make dev                  # Run application separately
```

### Traditional Step-by-Step Setup

```bash
# 1. Start all services
make docker-up

# 2. Initialize Elasticsearch
make elk-setup

# 3. Create DynamoDB tables
make dynamodb-create-tables

# 4. Run application
make dev
```

### Access Services

| Service | URL | Description |
|---------|-----|-------------|
| **Web UI** | **http://localhost:8080/** | **Modern hotel booking interface ⭐ NEW!** |
| GraphQL UI | http://localhost:8080/q/graphql-ui | Interactive API explorer |
| GraphQL Endpoint | http://localhost:8080/graphql | API endpoint |
| Dev UI | http://localhost:8080/q/dev | Quarkus dev console |
| Keycloak | http://localhost:8180 | Authentication server (admin/admin) |
| Kibana | http://localhost:5601 | Log and trace visualization |
| Elasticsearch | http://localhost:9200 | Data store |
| OTEL Collector | http://localhost:4318 | Telemetry collector |
| Vector | UDP localhost:12201 | **GELF log shipper ⭐ NEW!** |
| PostgreSQL | localhost:5432 | Keycloak database (keycloak/keycloak) |

## 📚 Documentation

- **[Three Logging Pipelines](LOGGING-PIPELINES.md)** - **⭐ NEW!** Configure and control three independent logging paths
- **[UI Documentation](UI-README.md)** - Modern React UI setup and features
- **[UI Features Guide](UI-FEATURES.md)** - Detailed UI components and design
- **[GELF Logging Guide](docs/GELF-LOGGING.md)** - High-performance logging with Vector
- **[GELF Quick Reference](docs/GELF-QUICKREF.md)** - Quick commands and troubleshooting
- **[Infrastructure Setup Guide](INFRASTRUCTURE.md)** - Complete guide to infrastructure setup and Keycloak schema loading
- **[Quick Start Guide](docs/QUICKSTART.md)** - Get running in 5 minutes!
- **[Security Setup Guide](docs/SECURITY.md)** - OAuth2/OIDC authentication and authorization
- **[Testing Guide](docs/TESTING.md)** - Comprehensive test scenarios
- **[GraphQL API Documentation](docs/graphql/README.md)** - Complete API reference
- **[Documentation Index](docs/README.md)** - All documentation organized
- **[Docker Configuration](docker/README.md)** - Container setup and OTEL Collector
- **[ELK Stack Guide](elk/README.md)** - Elasticsearch and Kibana setup
- **[Bruno API Collection](bruno/README.md)** - GraphQL API testing with authentication
- **[OpenTelemetry Setup](OTEL-SETUP.md)** - Observability configuration

## 🏗️ Architecture

```
┌─────────────────┐
│   React UI      │  ⭐ NEW!
│   (Quinoa)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  GraphQL API    │
│  (Quarkus)      │
└────────┬────────┘
         │
    ┌────┴────┬─────────────────────────────┐
    │         │                │             │
    ▼         ▼                ▼             ▼
┌──────┐  ┌────────┐   ┌──────────────┐  ┌────────┐
│  DB  │  │Keycloak│   │ OTEL         │  │ Vector │  ⭐ NEW!
│(DDB) │  │ (Auth) │   │ Collector    │  │ (GELF) │
└──────┘  └───┬────┘   └──────┬───────┘  └────┬───┘
              │                │               │
              ▼                └───────┬───────┘
         ┌─────────┐                  │
         │Postgres │                  ▼
         │(Keycloak)            ┌──────────┐
         └─────────┘            │ Elastic  │
                                │ search   │
                                └────┬─────┘
                                     │
                                     ▼
                                ┌─────────┐
                                │ Kibana  │
                                └─────────┘
```

### Three Independent Logging Pipelines ⭐ NEW!

The application now supports **three independent logging pipelines**, each writing to a separate Elasticsearch index. You can enable or disable any combination based on your needs.

#### Pipeline 1: OTEL Direct (quarkus → otel-collector → elasticsearch)
- **Index**: `otel-motel-otel-direct-logs-*`
- **Best for**: Observability with distributed tracing
- **Toggle**: `LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=true`

#### Pipeline 2: GELF via Vector (quarkus → gelf → vector → elasticsearch)
- **Index**: `otel-motel-gelf-logs-*`
- **Best for**: High-performance production logging
- **Toggle**: `LOGGING_PIPELINE_GELF_VECTOR_ENABLED=true`

#### Pipeline 3: OTEL via Vector (quarkus → otel-collector → vector → elasticsearch)
- **Index**: `otel-motel-otel-vector-logs-*`
- **Best for**: Advanced log processing and transformation
- **Toggle**: `LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=true`

**Quick Toggle Example**:
```bash
# Enable only GELF pipeline for production
export LOGGING_PIPELINE_OTEL_DIRECT_ENABLED=false
export LOGGING_PIPELINE_GELF_VECTOR_ENABLED=true
export LOGGING_PIPELINE_OTEL_VECTOR_ENABLED=false
make dev
```

For detailed configuration, see **[LOGGING-PIPELINES.md](LOGGING-PIPELINES.md)**.

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

### GELF Logging with Vector ⭐

The application includes a high-performance GELF (Graylog Extended Log Format) logging pipeline using Vector as a log shipper.

#### Architecture

```
Application (Quarkus)
  ├─> Console (JSON/ECS)
  ├─> OpenTelemetry Collector (OTLP)
  └─> GELF (UDP:12201)
        │
        └─> Vector (Log Shipper)
              │
              └─> Elasticsearch (otel-motel-gelf-logs-*)
                    │
                    └─> Kibana
```

#### Quick Start

1. **Start infrastructure** (includes Vector):
```bash
make infrastructure-up
```

2. **Initialize Elasticsearch indices** (includes GELF templates):
```bash
make elk-setup
```

3. **Run the application**:
```bash
make dev
```

4. **View GELF logs in Kibana**:
- Open Kibana: http://localhost:5601
- Navigate to **Discover**
- Create index pattern: `otel-motel-gelf-logs-*`
- View real-time logs with full stack traces and metadata

#### Configuration

GELF logging is configured in `application.properties`:
```properties
quarkus.log.handler.gelf.enabled=true
quarkus.log.handler.gelf.host=localhost
quarkus.log.handler.gelf.port=12201
```

Vector configuration is in `docker/vector/vector.yaml`.

#### Benefits

- **High Performance**: UDP transport with minimal overhead
- **Rich Metadata**: Includes logger name, thread, source class/method
- **Stack Traces**: Full exception traces automatically captured
- **ECS Compliant**: Logs mapped to Elastic Common Schema
- **Separate Pipeline**: Independent from OTLP for flexibility

#### Documentation

For complete GELF and Vector documentation, see:
- **[GELF Logging Guide](docs/GELF-LOGGING.md)** - Complete setup and usage
- **[GELF Quick Reference](docs/GELF-QUICKREF.md)** - Quick commands and troubleshooting
- **[ELK Stack Guide](elk/README.md)** - Elasticsearch configuration

#### Viewing GELF Logs

```bash
# Check Vector status
docker compose ps vector
docker compose logs vector

# View GELF logs in Elasticsearch
curl "http://localhost:9200/otel-motel-gelf-logs-*/_search?size=5&sort=@timestamp:desc&pretty"

# View recent logs
make elk-logs
```

#### Kibana Queries

Search for specific log patterns:
```
# Error logs only
log.level: "ERROR"

# Logs from a specific service
log.logger: "com.johnnyb.service.HotelService"

# Logs with stack traces
_exists_: error.stack_trace

# Recent logs
@timestamp: [now-15m TO now]
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
│   ├── otel-collector/      # OTEL Collector configuration
│   ├── vector/              # ⭐ Vector log shipper configuration
│   ├── keycloak/            # Keycloak realm configuration
│   ├── postgres/            # PostgreSQL init scripts
│   └── README.md            # Docker documentation
├── elk/
│   ├── elasticsearch/       # ES index templates and scripts
│   │   ├── templates/
│   │   │   ├── index-templates/    # Index templates (incl. GELF)
│   │   │   ├── ilm-policies/       # Lifecycle policies
│   │   │   └── ingest-pipelines/   # ECS mapping pipelines
│   │   └── setup-indices.sh       # Setup script
│   └── README.md           # ELK documentation
├── docs/
│   ├── GELF-LOGGING.md     # ⭐ GELF and Vector guide
│   ├── QUICKSTART.md       # Quick start guide
│   ├── SECURITY.md         # Security setup
│   ├── TESTING.md          # Testing guide
│   └── README.md           # Documentation index
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

The application uses two databases:
- **DynamoDB (LocalStack)**: Application data (hotels, rooms, bookings, customers)
- **PostgreSQL**: Keycloak authentication and user data

### Connect to PostgreSQL (Keycloak)
```bash
make postgres-console    # Open psql console
make postgres-backup     # Backup Keycloak database
```

### Connect to DynamoDB
```bash
make dynamodb-console      # Open DynamoDB CLI
make dynamodb-list-tables  # List all tables
```

### Reset Database
```bash
make dynamodb-reset              # Reset DynamoDB tables
make docker-down-volumes         # Stop and remove all volumes (includes PostgreSQL)
```

**⚠️ Important: PostgreSQL Initialization**

PostgreSQL initialization scripts only run on **first startup**. If you need to re-run the initialization:
1. Stop services and remove volumes: `make docker-down-volumes`
2. Start services again: `make docker-up` or `make infrastructure-up`

To verify initialization ran: `make postgres-init-logs`

### PostgreSQL Configuration
```properties
Host: localhost
Port: 5432
Database: keycloak
User: keycloak
Password: keycloak
```

### DynamoDB Configuration
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

## 🐛 Troubleshooting

### PostgreSQL Initialization Issues

**Problem**: PostgreSQL initialization script didn't run or needs to be re-run.

**Solution**: The init script only runs on first startup when the volume is created. To re-initialize:
```bash
make docker-down-volumes    # Remove all volumes
make docker-up             # Restart (will run init script)
```

Check if initialization ran successfully:
```bash
make postgres-init-logs
```

### Infrastructure Setup Hangs or Fails

**Problem**: `make infrastructure-up` or `make app-ready` hangs or fails.

**Solution**: Services take time to become healthy. The script waits up to 5 minutes. If it times out:
```bash
# Check service status
docker compose ps

# View service logs
docker compose logs <service-name>

# Common services to check: postgres, keycloak, elasticsearch
```

### Service Not Healthy

**Problem**: A service shows as "unhealthy" or "starting" for a long time.

**Solution**:
```bash
# Check specific service logs
docker compose logs <service-name>

# Restart specific service
docker compose restart <service-name>

# Nuclear option: restart everything
make docker-down
make docker-up
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
