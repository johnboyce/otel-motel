# Documentation

Complete documentation for the otel-motel hotel booking system.

## Quick Links

- 🚀 **[Quick Start Guide](QUICKSTART.md)** - Get running in 5 minutes
- 🧪 **[Testing Guide](TESTING.md)** - Comprehensive testing scenarios
- 📊 **[GraphQL API Docs](graphql/README.md)** - API reference and examples

## Project Documentation

### Getting Started
- **[Main README](../README.md)** - Project overview and features
- **[Quick Start](QUICKSTART.md)** - Fast setup guide
- **[Prerequisites](../README.md#prerequisites)** - Required software

### Configuration
- **[Docker Setup](../docker/README.md)** - Container configuration
- **[ELK Stack](../elk/README.md)** - Logging and observability
- **[Application Properties](../src/main/resources/application.properties)** - App configuration

### API Documentation
- **[GraphQL API](graphql/README.md)** - Complete API reference
- **[Bruno Collection](../bruno/README.md)** - API testing collection
- **[GraphQL UI](http://localhost:8080/q/graphql-ui)** - Interactive explorer (when running)

### Testing
- **[Testing Guide](TESTING.md)** - Test scenarios and examples
- **[Bruno Tests](../bruno/README.md)** - Automated API tests

### Observability
- **[OpenTelemetry Setup](../OTEL-SETUP.md)** - OTEL configuration
- **[GELF Logging Guide](GELF-LOGGING.md)** - **⭐ NEW!** High-performance logging with Vector
- **[GELF Quick Reference](GELF-QUICKREF.md)** - **⭐ NEW!** Quick commands and troubleshooting
- **[ELK Guide](../elk/README.md)** - Elasticsearch and Kibana
- **[Monitoring](../README.md#monitoring--observability)** - Metrics and traces

## Architecture

### System Overview

```
┌─────────────────────────────────────────────┐
│          Client Applications                 │
│  (Browser, Bruno, CLI, Mobile Apps)         │
└─────────────────┬───────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────────┐
│         GraphQL API Layer                    │
│      (Quarkus + SmallRye GraphQL)           │
│  - HotelGraphQLResource                     │
│  - Query & Mutation Resolvers               │
└─────────────────┬───────────────────────────┘
                  │
        ┌─────────┴─────────┬──────────────┐
        │                   │              │
        ▼                   ▼              ▼
┌──────────────┐   ┌──────────────────┐  ┌────────┐
│   DynamoDB   │   │  OpenTelemetry   │  │ Vector │ ⭐ NEW!
│ (LocalStack) │   │    Exporter      │  │ (GELF) │
│              │   │                  │  └────┬───┘
│ - Hotels     │   └────────┬─────────┘       │
│ - Rooms      │            │                 │
│ - Bookings   │            ▼                 │
│ - Customers  │   ┌──────────────────┐       │
└──────────────┘   │ OTEL Collector   │       │
                   │  (HTTP/Protobuf) │       │
                   └────────┬─────────┘       │
                            │                 │
                            └────────┬────────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │ Elasticsearch│
                              │   (Storage)  │
                              └──────┬───────┘
                                     │
                                     ▼
                              ┌──────────────┐
                              │    Kibana    │
                              │(Visualization)│
                              └──────────────┘
```

### Data Model

```
Hotel 1───* Room 1───* Booking *───1 Customer
  │                      │
  │ id                   │ id
  │ name                 │ checkInDate
  │ address              │ checkOutDate
  │ city                 │ numberOfGuests
  │ country              │ totalPrice
  │ description          │ status
  │ starRating           │ specialRequests
  └──────────────────────┘
```

### Technology Stack

- **Framework**: Quarkus 3.27.0
- **Language**: Java 17
- **GraphQL**: SmallRye GraphQL
- **Database**: DynamoDB LocalStack
- **SDK**: AWS SDK Enhanced DynamoDB Client
- **Observability**: OpenTelemetry
- **Logging**: ELK Stack (Elasticsearch 8.11, Kibana 8.11)
- **Log Shipper**: Vector (GELF) ⭐ NEW!
- **Containers**: Docker & Docker Compose
- **Build Tool**: Maven
- **API Testing**: Bruno

## Key Features

### 1. GraphQL API
- Full CRUD operations for hotels, rooms, bookings, customers
- Complex queries with nested relationships
- Efficient data fetching
- Built-in schema documentation

### 2. Sample Data
- 5 hotels across major US cities
- 108 rooms with various types and pricing
- 10 customers with realistic profiles
- ~200 bookings spanning 3 months
- Automatic data initialization on startup

### 3. Observability
- **Distributed Tracing**: Track requests across services
- **Structured Logging**: ECS-compliant JSON logs
- **GELF Logging**: High-performance UDP logging via Vector ⭐ NEW!
- **Metrics Collection**: Application performance metrics
- **Log Correlation**: Link logs with traces via trace IDs

### 4. ECS Compliance
All logs follow Elastic Common Schema:
- `@timestamp` - Event timestamp
- `service.name` - Service identifier
- `log.level` - Log severity
- `trace.id` - Distributed trace ID
- `message` - Log message

### 5. Developer Experience
- Hot reload in dev mode
- GraphQL UI for interactive testing
- Comprehensive Makefile with 40+ commands
- Bruno collection for API testing
- Complete documentation

## Development Workflow

### 1. Local Development
```bash
make docker-up      # Start services
make elk-setup      # Initialize ELK
make dev            # Start app with hot reload
```

### 2. Making Changes
```bash
make compile        # Quick compilation check
make test           # Run tests
make verify         # Full verification
```

### 3. Database Operations
```bash
make dynamodb-console      # Connect to DynamoDB
make dynamodb-list-tables  # List all tables
make dynamodb-reset        # Reset database
```

### 4. Monitoring
```bash
make elk-logs       # View logs
make elk-health     # Check ELK status
make otel-metrics   # View OTEL metrics
```

## Environment Configuration

### Development (Current)
- Single-node services
- Debug logging enabled
- Hot reload enabled
- Sample data auto-loaded
- No authentication

### Staging (Future)
- Multi-node database
- Structured logging
- Security enabled
- Realistic data volume
- Basic authentication

### Production (Future)
- Clustered database
- Production logging levels
- Full security stack
- High availability
- OAuth/OIDC authentication
- Rate limiting
- Caching layer

## API Patterns

### Query Pattern
```graphql
query GetHotelWithRooms($hotelId: Long!) {
  hotel(id: $hotelId) {
    id
    name
    rooms {
      id
      roomNumber
      pricePerNight
    }
  }
}
```

### Mutation Pattern
```graphql
mutation CreateBooking($input: BookingInput!) {
  createBooking(
    roomId: $input.roomId,
    customerId: $input.customerId,
    checkInDate: $input.checkInDate,
    checkOutDate: $input.checkOutDate,
    numberOfGuests: $input.numberOfGuests
  ) {
    id
    status
    totalPrice
  }
}
```

### Error Handling
```graphql
{
  "errors": [
    {
      "message": "Room not available",
      "path": ["createBooking"],
      "extensions": {
        "classification": "DataFetchingException"
      }
    }
  ]
}
```

## Performance Considerations

### Database
- Indexed foreign keys
- Lazy loading for relationships
- Connection pooling via Agroal
- Prepared statements

### Caching
- Entity-level caching (future)
- Query result caching (future)
- HTTP caching headers

### Scaling
- Stateless application design
- Horizontal scaling ready
- Database connection pooling
- Async processing capability

## Security Considerations

### Current (Development)
- No authentication
- No authorization
- Unencrypted connections
- Sample credit card data

### Future (Production)
- JWT/OAuth authentication
- Role-based access control
- HTTPS/TLS encryption
- PCI-compliant payment processing
- Rate limiting
- Input validation
- DynamoDB access control

## Maintenance

### Regular Tasks
```bash
make elk-indices          # Check index health
make dynamodb-list-tables # Database maintenance
make docker-logs          # Check service logs
```

### Troubleshooting
1. Check service health: `make elk-health`
2. View logs: `make docker-logs`
3. Reset database: `make dynamodb-reset`
4. Restart services: `make docker-restart`

## Contributing

When adding features:
1. Update entity models if needed
2. Add GraphQL queries/mutations
3. Update documentation
4. Create Bruno test requests
5. Test end-to-end
6. Update README files

## Resources

### External Documentation
- [Quarkus Guides](https://quarkus.io/guides/)
- [GraphQL Best Practices](https://graphql.org/learn/best-practices/)
- [OpenTelemetry Docs](https://opentelemetry.io/docs/)
- [Elastic Stack Docs](https://www.elastic.co/guide/)
- [DynamoDB Documentation](https://docs.aws.amazon.com/dynamodb/)
- [LocalStack Documentation](https://docs.localstack.cloud/)

### Internal Documentation
- [Main README](../README.md)
- [Docker README](../docker/README.md)
- [ELK README](../elk/README.md)
- [Bruno README](../bruno/README.md)

## Support

For issues or questions:
1. Check [Testing Guide](TESTING.md) for common scenarios
2. Review [Troubleshooting](#troubleshooting) section
3. Check service logs: `make docker-logs`
4. Verify health: `make elk-health`

---

**Last Updated**: 2025-10-14
