# Testing Guide for otel-motel

This guide provides comprehensive testing instructions for the otel-motel hotel booking system.

## Quick Start

```bash
# 1. Start infrastructure
make docker-up
make elk-setup

# 2. Run application
make dev

# 3. Run tests
make test
```

## Testing Stack

### Unit & Integration Tests
- **Framework**: JUnit 5
- **Location**: `src/test/java/`
- **Command**: `make test`

### API Testing
- **Tool**: Bruno API Client
- **Location**: `bruno/`
- **Command**: `bru run bruno/`

### Manual Testing
- **GraphQL UI**: http://localhost:8080/q/graphql-ui
- **Dev UI**: http://localhost:8080/q/dev

## Test Scenarios

### Scenario 1: Hotel Search and Booking

#### Step 1: Search for Hotels
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

**Expected**: Returns list of 5 hotels

#### Step 2: View Hotel Details
```graphql
query {
  hotel(id: 1) {
    id
    name
    address
    city
    description
    starRating
    rooms {
      id
      roomNumber
      roomType
      pricePerNight
      capacity
    }
  }
}
```

**Expected**: Returns hotel with 20 rooms

#### Step 3: Check Room Availability
```graphql
query {
  availableRooms(
    hotelId: 1,
    checkIn: "2025-12-01",
    checkOut: "2025-12-05"
  ) {
    id
    roomNumber
    roomType
    pricePerNight
    capacity
  }
}
```

**Expected**: Returns available rooms (will vary based on existing bookings)

#### Step 4: Create Booking
```graphql
mutation {
  createBooking(
    roomId: 1,
    customerId: 1,
    checkInDate: "2025-12-01",
    checkOutDate: "2025-12-05",
    numberOfGuests: 2,
    specialRequests: "Ocean view preferred"
  ) {
    id
    checkInDate
    checkOutDate
    totalPrice
    status
    room {
      roomNumber
      hotel {
        name
      }
    }
  }
}
```

**Expected**: 
- Booking created successfully
- Status: CONFIRMED
- Total price calculated correctly (4 nights × room price)

#### Step 5: Verify Booking
```graphql
query {
  booking(id: <booking-id-from-step-4>) {
    id
    checkInDate
    checkOutDate
    status
    totalPrice
    numberOfGuests
    specialRequests
    room {
      roomNumber
      hotel {
        name
        city
      }
    }
    customer {
      firstName
      lastName
      email
    }
  }
}
```

**Expected**: Returns complete booking details

### Scenario 2: Customer Management

#### View Customer Profile
```graphql
query {
  customerByEmail(email: "john.doe@example.com") {
    id
    firstName
    lastName
    email
    phone
    address
  }
}
```

**Expected**: Returns customer details

#### View Customer Bookings
```graphql
query {
  bookingsByCustomer(customerId: 1) {
    id
    checkInDate
    checkOutDate
    status
    totalPrice
    room {
      roomNumber
      hotel {
        name
        city
      }
    }
  }
}
```

**Expected**: Returns all bookings for the customer

### Scenario 3: Booking Management

#### View Upcoming Bookings
```graphql
query {
  upcomingBookings {
    id
    checkInDate
    checkOutDate
    status
    room {
      roomNumber
      hotel {
        name
        city
      }
    }
    customer {
      firstName
      lastName
    }
  }
}
```

**Expected**: Returns all future bookings

#### Cancel Booking
```graphql
mutation {
  cancelBooking(bookingId: 1) {
    id
    status
    checkInDate
    checkOutDate
  }
}
```

**Expected**: 
- Status changed to CANCELLED
- Returns updated booking

### Scenario 4: Multi-City Search

#### Search Hotels by City
```graphql
query {
  hotelsByCity(city: "Miami Beach") {
    id
    name
    address
    starRating
  }
}
```

**Expected**: Returns hotels in Miami Beach

#### Search Hotels by Country
```graphql
query {
  hotelsByCountry(country: "USA") {
    id
    name
    city
    starRating
  }
}
```

**Expected**: Returns all US hotels

### Scenario 5: Room Availability Check

#### Check Room Occupancy
```graphql
query {
  roomsByHotel(hotelId: 1) {
    id
    roomNumber
    roomType
    pricePerNight
  }
}
```

Then for each room, check bookings:
```graphql
query {
  booking(id: <room-booking-id>) {
    checkInDate
    checkOutDate
    status
  }
}
```

**Expected**: Can verify which rooms are booked

## Error Handling Tests

### Test 1: Invalid Booking Dates
```graphql
mutation {
  createBooking(
    roomId: 1,
    customerId: 1,
    checkInDate: "2025-11-20",
    checkOutDate: "2025-11-15",  # Before check-in!
    numberOfGuests: 2
  ) {
    id
  }
}
```

**Expected**: Error - checkout before checkin

### Test 2: Room Not Available
Try to book an already booked room for overlapping dates.

**Expected**: Error - "Room is not available for the selected dates"

### Test 3: Invalid Customer
```graphql
mutation {
  createBooking(
    roomId: 1,
    customerId: 9999,  # Non-existent
    checkInDate: "2025-11-15",
    checkOutDate: "2025-11-20",
    numberOfGuests: 2
  ) {
    id
  }
}
```

**Expected**: Error - "Customer not found"

### Test 4: Invalid Room
```graphql
mutation {
  createBooking(
    roomId: 9999,  # Non-existent
    customerId: 1,
    checkInDate: "2025-11-15",
    checkOutDate: "2025-11-20",
    numberOfGuests: 2
  ) {
    id
  }
}
```

**Expected**: Error - "Room not found"

## Performance Tests

### Load Test: Multiple Queries
Execute multiple queries simultaneously:
```bash
# Using Bruno CLI
for i in {1..10}; do
  bru run bruno/Hotels/"Get All Hotels.bru" &
done
wait
```

**Expected**: All queries complete successfully

### Stress Test: Concurrent Bookings
Try to book the same room concurrently:
```bash
# This should fail for one of them
bru run bruno/Bookings/"Create Booking.bru" &
bru run bruno/Bookings/"Create Booking.bru" &
wait
```

**Expected**: One succeeds, one fails with availability error

## Observability Testing

### Test 1: Verify Logs in Kibana
1. Make API requests
2. Open Kibana: http://localhost:5601
3. Navigate to Discover
4. Search for `service.name: otel-motel`
5. Verify structured logs appear with ECS fields

### Test 2: Verify Traces
1. Execute GraphQL query
2. In Kibana, go to Observability → APM
3. Verify trace appears with correct service name
4. Check span details

### Test 3: Check Metrics
```bash
make otel-metrics
```

**Expected**: Returns OTEL Collector metrics

### Test 4: Query Elasticsearch
```bash
make elk-logs
```

**Expected**: Returns recent application logs

## Database Testing

### Verify Data Initialization
```bash
make dynamodb-list-tables
```

**Expected**: Returns list of DynamoDB tables (hotels, rooms, customers, bookings)

### Verify Data in DynamoDB
```bash
# Access DynamoDB console
make dynamodb-console

# List tables
awslocal dynamodb list-tables

# Scan a table (example)
awslocal dynamodb scan --table-name hotels

# Get item count
awslocal dynamodb scan --table-name hotels --select COUNT
```

### Test Database Reset
```bash
make dynamodb-reset
make dev  # Restart app to reinitialize
```

**Expected**: Tables are deleted and data is recreated

## Integration Testing Checklist

- [ ] All hotels load correctly
- [ ] Room availability check works
- [ ] Booking creation succeeds
- [ ] Booking cancellation works
- [ ] Customer lookup by email works
- [ ] Multiple bookings per customer work
- [ ] Room booking conflicts are detected
- [ ] Total price calculation is correct
- [ ] GraphQL UI is accessible
- [ ] Logs appear in Kibana
- [ ] Traces appear in APM
- [ ] Database persists data correctly
- [ ] Docker services start successfully
- [ ] Elasticsearch indices are created
- [ ] OTEL Collector exports telemetry

## Automated Testing

### Run All Tests
```bash
make test
```

### Run with Coverage
```bash
./mvnw verify
```

### Continuous Testing
```bash
make dev  # Enables continuous testing in dev mode
```

Press `r` in the dev console to run tests.

## Bruno Collection Testing

### Install Bruno CLI
```bash
npm install -g @usebruno/cli
```

### Run All Bruno Tests
```bash
bru run bruno/
```

### Run Specific Collection
```bash
bru run bruno/Hotels/
bru run bruno/Bookings/
```

### Run Single Test
```bash
bru run bruno/Hotels/"Get All Hotels.bru"
```

## Manual Verification

### 1. GraphQL UI Testing
- Open: http://localhost:8080/q/graphql-ui
- Try all sample queries
- Verify autocomplete works
- Check error messages

### 2. Kibana Verification
- Open: http://localhost:5601
- Create index pattern: `otel-motel-logs-*`
- Search for logs
- Check APM traces

### 3. Database Verification
```bash
make dynamodb-console
# Use awslocal commands to query DynamoDB
awslocal dynamodb list-tables
awslocal dynamodb scan --table-name hotels
awslocal dynamodb scan --table-name rooms --limit 10
awslocal dynamodb scan --table-name bookings --limit 10
```

## Common Issues

### Issue: Port Already in Use
```
Error: Port 4566 already in use
```

**Solution**:
```bash
make docker-down
# or
docker stop $(docker ps -aq)
```

### Issue: Database Connection Failed
```
Error: Connection refused to localhost:4566
```

**Solution**:
```bash
make docker-ps  # Check if DynamoDB (LocalStack) is running
make docker-logs dynamodb  # Check logs
docker exec otel-motel-dynamodb awslocal dynamodb list-tables  # Test connection
```

### Issue: No Logs in Kibana
```
No documents found
```

**Solution**:
1. Verify Elasticsearch is running: `make elk-health`
2. Check indices: `make elk-indices`
3. Run setup: `make elk-setup`
4. Restart app: `make dev`

### Issue: GraphQL Query Fails
```
Error: Server not available
```

**Solution**:
```bash
make dev  # Ensure app is running
curl http://localhost:8080/q/health  # Check health
```

## Test Data Reference

### Hotels
1. Grand Pacific Resort (Miami Beach) - 5★
2. Metropolitan Business Hotel (New York) - 4★
3. The Vintage Inn (Charleston) - 4★
4. Alpine Mountain Lodge (Aspen) - 4★
5. Sky Harbor Hotel (Los Angeles) - 3★

### Sample Customer
- Email: john.doe@example.com
- Customer ID: 1

### Date Range
- Bookings span: Today to +3 months
- Use dates in format: YYYY-MM-DD

## Best Practices

1. **Always check availability** before creating bookings
2. **Use realistic dates** (within next 3 months)
3. **Verify bookings** after creation
4. **Check logs** in Kibana for debugging
5. **Reset database** if data becomes inconsistent
6. **Use Bruno collection** for reproducible tests
7. **Monitor OTEL metrics** during load testing

## Next Steps

After completing these tests:
1. Create custom test scenarios
2. Add performance benchmarks
3. Implement automated CI/CD tests
4. Set up monitoring alerts
5. Document edge cases

## References

- [Main README](../README.md)
- [Bruno Collection](../bruno/README.md)
- [GraphQL API Docs](graphql/README.md)
