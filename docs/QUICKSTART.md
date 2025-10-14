# Quick Start Guide - otel-motel

Get up and running with otel-motel in 5 minutes!

## Prerequisites Check

```bash
java -version    # Should be 17+
docker --version # Should be 20+
make --version   # Should be available
```

If missing, install:
```bash
# macOS
brew install openjdk@17 docker docker-compose

# Set Java home
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

## Step 1: Clone and Build (2 min)

```bash
# Clone repository
cd otel-motel

# Build project
make build
```

## Step 2: Start Services (2 min)

```bash
# Start Docker services (DynamoDB, ELK, OTEL Collector)
make docker-up

# Wait 30 seconds for services to start, then initialize Elasticsearch
make elk-setup
```

## Step 3: Run Application (1 min)

```bash
# Start in development mode
make dev
```

Wait for: `Listening on: http://localhost:8080`

## Step 4: Try the API!

### Option A: GraphQL UI (Recommended)

Open browser: http://localhost:8080/q/graphql-ui

Try this query:
```graphql
query {
  hotels {
    id
    name
    city
    starRating
  }
}
```

### Option B: curl

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ hotels { id name city } }"}'
```

### Option C: Bruno

```bash
# Install Bruno
brew install bruno

# Open collection
bruno open bruno/

# Execute "Get All Hotels" request
```

## Step 5: Explore Observability

### View Logs in Kibana
1. Open: http://localhost:5601
2. Go to "Discover"
3. Create index pattern: `otel-motel-logs-*`
4. View structured logs with traces!

### Check Application Health
```bash
curl http://localhost:8080/q/health
```

## Common First Queries

### 1. Check Room Availability
```graphql
query {
  availableRooms(
    hotelId: 1,
    checkIn: "2025-12-01",
    checkOut: "2025-12-05"
  ) {
    roomNumber
    roomType
    pricePerNight
  }
}
```

### 2. Create a Booking
```graphql
mutation {
  createBooking(
    roomId: 1,
    customerId: 1,
    checkInDate: "2025-12-15",
    checkOutDate: "2025-12-20",
    numberOfGuests: 2,
    specialRequests: "Late check-in"
  ) {
    id
    totalPrice
    status
  }
}
```

### 3. View Customer Bookings
```graphql
query {
  customerByEmail(email: "john.doe@example.com") {
    firstName
    lastName
    bookings {
      id
      checkInDate
      room {
        hotel {
          name
        }
      }
    }
  }
}
```

## What's Running?

```bash
make docker-ps
```

Should show:
- DynamoDB/LocalStack (port 4566)
- Elasticsearch (port 9200)
- Kibana (port 5601)
- OTEL Collector (port 4318)

## Useful Commands

```bash
make help              # Show all commands
make elk-health        # Check ELK status
make dynamodb-console  # Connect to database
make elk-logs          # View recent logs
make docker-logs       # View all service logs
```

## Stop Everything

```bash
make stop           # Stop all services
```

## Troubleshooting

### Port Already in Use
```bash
make docker-down
lsof -i :4566  # Check what's using DynamoDB port
```

### Services Not Starting
```bash
make docker-logs    # Check logs
docker system prune # Clean up Docker
```

### Application Won't Start
```bash
make clean build    # Rebuild
make docker-restart # Restart services
```

## Next Steps

1. **Read Full Documentation**: [README.md](../README.md)
2. **Explore API**: [GraphQL Docs](graphql/README.md)
3. **Run Tests**: [Testing Guide](TESTING.md)
4. **Try Bruno Collection**: [Bruno README](../bruno/README.md)

## Sample Data

The system comes pre-loaded with:
- **5 hotels** in different cities
- **108 rooms** with various types
- **10 customers** with booking history
- **~200 bookings** over next 3 months

## Architecture at a Glance

```
You → GraphQL API → DynamoDB (data)
                 ↓
               OTEL Collector
                 ↓
          Elasticsearch (logs/traces)
                 ↓
              Kibana (visualization)
```

## Support

- Check [Main README](../README.md) for detailed docs
- View [Testing Guide](TESTING.md) for examples
- Use `make help` for available commands

---

**You're ready to go!** 🚀

Try the GraphQL UI: http://localhost:8080/q/graphql-ui
