# GraphQL API Documentation

This directory contains documentation and schema files for the otel-motel GraphQL API.

## Contents

- **schema.graphql** - The complete GraphQL schema (auto-generated)
- **API.md** - Human-readable API documentation (generated from schema)

## Generating Documentation

### Export Schema

The schema is automatically available when the application is running:

```bash
# Start the application
make dev

# Export the schema
make schema-export
```

This will create `schema.graphql` in this directory.

### Generate Documentation

To generate markdown documentation from the schema:

```bash
# Install graphql-markdown
npm install -g graphql-markdown

# Generate documentation
make schema-doc
```

## GraphQL UI

The easiest way to explore the API is through the built-in GraphQL UI:

**URL**: http://localhost:8080/q/graphql-ui

Features:
- Complete schema documentation
- Interactive query builder
- Query autocomplete
- Syntax highlighting
- Result visualization

## API Overview

### Queries

#### Hotels
- `hotels` - Get all hotels
- `hotel(id: Long)` - Get hotel by ID
- `hotelsByCity(city: String)` - Get hotels in a city
- `hotelsByCountry(country: String)` - Get hotels in a country

#### Rooms
- `room(id: Long)` - Get room by ID
- `roomsByHotel(hotelId: Long)` - Get rooms for a hotel
- `availableRooms(hotelId: Long, checkIn: Date, checkOut: Date)` - Check availability

#### Bookings
- `booking(id: Long)` - Get booking by ID
- `bookingsByCustomer(customerId: Long)` - Get customer's bookings
- `upcomingBookings` - Get all upcoming bookings

#### Customers
- `customer(id: Long)` - Get customer by ID
- `customerByEmail(email: String)` - Find customer by email

### Mutations

#### Bookings
- `createBooking(...)` - Create a new booking
- `cancelBooking(bookingId: Long)` - Cancel a booking

## Example Queries

### Get All Hotels

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

### Check Room Availability

```graphql
query {
  availableRooms(
    hotelId: 1,
    checkIn: "2025-11-01",
    checkOut: "2025-11-05"
  ) {
    id
    roomNumber
    roomType
    pricePerNight
    capacity
  }
}
```

### Create a Booking

```graphql
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
    room {
      roomNumber
      hotel {
        name
      }
    }
  }
}
```

### Get Customer with Bookings

```graphql
query {
  customerByEmail(email: "john.doe@example.com") {
    id
    firstName
    lastName
    email
    phone
    bookings {
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
}
```

## Type Definitions

### Hotel
```graphql
type Hotel {
  id: ID!
  name: String!
  address: String!
  city: String!
  country: String!
  description: String
  starRating: Int!
  rooms: [Room!]
}
```

### Room
```graphql
type Room {
  id: ID!
  hotel: Hotel!
  roomNumber: String!
  roomType: String!
  pricePerNight: BigDecimal!
  capacity: Int!
  description: String
  bookings: [Booking!]
}
```

### Booking
```graphql
type Booking {
  id: ID!
  room: Room!
  customer: Customer!
  checkInDate: Date!
  checkOutDate: Date!
  numberOfGuests: Int!
  totalPrice: BigDecimal!
  status: BookingStatus!
  specialRequests: String
}

enum BookingStatus {
  PENDING
  CONFIRMED
  CANCELLED
  COMPLETED
}
```

### Customer
```graphql
type Customer {
  id: ID!
  firstName: String!
  lastName: String!
  email: String!
  phone: String!
  address: String
  bookings: [Booking!]
}
```

## Bruno Collection

For a complete set of ready-to-use API requests, see the [Bruno Collection](../../bruno/README.md).

## Testing

Use the Bruno collection or GraphQL UI for testing:

```bash
# Using Bruno CLI
bru run bruno/

# Or open GraphQL UI
make graphql-ui
```

## Schema Introspection

Query the schema programmatically:

```graphql
query {
  __schema {
    types {
      name
      kind
      description
    }
  }
}
```

## References

- [GraphQL Specification](https://graphql.org/learn/)
- [SmallRye GraphQL Guide](https://quarkus.io/guides/smallrye-graphql)
- [otel-motel Main README](../../README.md)
