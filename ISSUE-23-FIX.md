# Fix for Issue #23: GraphQL Schema Mismatch

## Problem
The UI GraphQL query expected `room` and `customer` fields on the `Booking` type, but the database model only had `roomId` and `customerId` (string IDs). This caused a validation error:

```
Field 'room' in type 'Booking' is undefined
Field 'customer' in type 'Booking' is undefined
```

## Solution
Added GraphQL field resolvers to resolve nested objects from their IDs:

### 1. BookingFieldResolver
Created `src/main/java/com/johnnyb/graphql/BookingFieldResolver.java` to add:
- `room` field resolver: Looks up the Room using `roomId`
- `customer` field resolver: Looks up the Customer using `customerId`

### 2. RoomFieldResolver
Created `src/main/java/com/johnnyb/graphql/RoomFieldResolver.java` to add:
- `hotel` field resolver: Looks up the Hotel using `hotelId`

## How It Works
MicroProfile GraphQL uses the `@Source` annotation to define field resolvers. When a GraphQL query requests a field that doesn't exist directly on the model (like `booking.room`), it looks for a method annotated with `@Source` that takes the parent type as a parameter and returns the requested type.

For example:
```java
public Room room(@Source Booking booking) {
    return roomService.findById(booking.getRoomId()).orElse(null);
}
```

This tells GraphQL: "When someone queries for the `room` field on a `Booking`, call this method to resolve it."

## Testing
The fix can be verified by:
1. Starting the application with `mvn quarkus:dev`
2. Opening the GraphQL UI at http://localhost:8080/q/graphql-ui
3. Running the query from BookingList.js:
```graphql
query GetUpcomingBookings {
  upcomingBookings {
    id
    room {
      id
      roomNumber
      roomType
      hotel {
        id
        name
        city
        state
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

The query should now execute without validation errors.

## Database Schema
No changes to the database schema were needed. The models still store only IDs (`roomId`, `customerId`, `hotelId`), and the resolvers fetch the full objects on demand. This follows the real-world pattern of:
- Database: Store normalized data with foreign keys
- GraphQL: Resolve nested objects on demand for API consumers
