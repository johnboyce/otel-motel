# Bruno API Collection for otel-motel

This directory contains a complete Bruno API collection for testing all GraphQL operations in the otel-motel hotel booking system.

**Bruno Version**: 2.11.0

## What is Bruno?

Bruno is a fast, Git-friendly, open-source API client. Unlike Postman, Bruno stores collections directly in your filesystem, making it perfect for version control.

**Website**: https://www.usebruno.com/

## Installation

### macOS (Homebrew)
```bash
brew install bruno
```

### Manual Download
Download from: https://www.usebruno.com/downloads

### CLI (for automation)
```bash
npm install -g @usebruno/cli
```

## Collection Structure

```
bruno/
├── bruno.json                    # Collection metadata
├── Hotels/
│   ├── Get All Hotels.bru
│   ├── Get Hotel by ID.bru
│   └── Get Hotels by City.bru
├── Rooms/
│   ├── Get Available Rooms.bru
│   └── Get Rooms by Hotel.bru
├── Bookings/
│   ├── Create Booking.bru
│   ├── Get Upcoming Bookings.bru
│   ├── Get Bookings by Customer.bru
│   └── Cancel Booking.bru
└── Customers/
    └── Get Customer by Email.bru
```

## Usage

### Prerequisites
1. Start the application:
   ```bash
   make dev
   ```

2. Ensure the GraphQL endpoint is accessible:
   ```
   http://localhost:8080/graphql
   ```

### Using Bruno GUI

1. Open Bruno
2. Open Collection: `File` → `Open Collection`
3. Select the `bruno/` directory
4. Execute requests by clicking "Send"

### Using Bruno CLI

Run all requests:
```bash
bru run bruno/
```

Run specific folder:
```bash
bru run bruno/Hotels/
```

Run specific request:
```bash
bru run bruno/Hotels/"Get All Hotels.bru"
```

## API Endpoints

### Hotels

#### Get All Hotels
Retrieves a list of all hotels with basic information.

**Response includes**: id, name, address, city, country, description, star rating

#### Get Hotel by ID
Retrieves detailed information about a specific hotel including all its rooms.

**Parameters**: 
- `id` (Long) - Hotel ID

#### Get Hotels by City
Filters hotels by city name.

**Parameters**:
- `city` (String) - City name (e.g., "Miami Beach")

### Rooms

#### Get Rooms by Hotel
Lists all rooms for a specific hotel.

**Parameters**:
- `hotelId` (Long) - Hotel ID

#### Get Available Rooms
Checks room availability for specific dates.

**Parameters**:
- `hotelId` (Long) - Hotel ID
- `checkIn` (String) - Check-in date (YYYY-MM-DD)
- `checkOut` (String) - Check-out date (YYYY-MM-DD)

### Bookings

#### Create Booking
Creates a new hotel booking.

**Parameters**:
- `roomId` (Long) - Room ID
- `customerId` (Long) - Customer ID
- `checkInDate` (String) - Check-in date (YYYY-MM-DD)
- `checkOutDate` (String) - Check-out date (YYYY-MM-DD)
- `numberOfGuests` (Integer) - Number of guests
- `specialRequests` (String, optional) - Special requests

**Returns**: Booking details including calculated total price

#### Get Upcoming Bookings
Retrieves all bookings with future check-in dates.

#### Get Bookings by Customer
Lists all bookings for a specific customer.

**Parameters**:
- `customerId` (Long) - Customer ID

#### Cancel Booking
Cancels an existing booking.

**Parameters**:
- `bookingId` (Long) - Booking ID

### Customers

#### Get Customer by Email
Retrieves customer information by email address, including their booking history.

**Parameters**:
- `email` (String) - Customer email address

## Sample Data

The application is pre-loaded with sample data:

### Hotels
1. Grand Pacific Resort (Miami Beach) - 5 stars
2. Metropolitan Business Hotel (New York) - 4 stars
3. The Vintage Inn (Charleston) - 4 stars
4. Alpine Mountain Lodge (Aspen) - 4 stars
5. Sky Harbor Hotel (Los Angeles) - 3 stars

### Sample Customer
- Email: `john.doe@example.com`
- Name: John Doe
- Customer ID: 1

### Sample Dates
The system supports bookings for the next 3 months from the current date.

## Testing Workflow

### 1. Explore Hotels
```graphql
# Get all hotels
query { hotels { id name city starRating } }

# Get hotel details
query { hotel(id: 1) { name rooms { roomNumber roomType pricePerNight } } }
```

### 2. Check Room Availability
```graphql
query {
  availableRooms(hotelId: 1, checkIn: "2025-11-01", checkOut: "2025-11-05") {
    roomNumber
    roomType
    pricePerNight
  }
}
```

### 3. Create a Booking
```graphql
mutation {
  createBooking(
    roomId: 1,
    customerId: 1,
    checkInDate: "2025-11-15",
    checkOutDate: "2025-11-20",
    numberOfGuests: 2,
    specialRequests: "Late check-in"
  ) {
    id
    totalPrice
    status
  }
}
```

### 4. View Bookings
```graphql
query {
  upcomingBookings {
    id
    checkInDate
    room { roomNumber hotel { name } }
  }
}
```

## GraphQL UI

Alternatively, use the built-in GraphQL UI:

**URL**: http://localhost:8080/q/graphql-ui

Features:
- Schema documentation
- Query autocomplete
- Interactive query builder
- Response visualization

## Continuous Integration

Integrate Bruno tests into CI/CD:

```bash
# In your CI script
npm install -g @usebruno/cli
make dev &  # Start server in background
sleep 10    # Wait for server to start
bru run bruno/ --reporter json > test-results.json
```

## Customization

### Adding New Requests

1. Create a new `.bru` file in the appropriate folder
2. Use the Bruno format:
   ```
   meta {
     name: Request Name
     type: http
     seq: 1
   }
   
   post {
     url: http://localhost:8080/graphql
     body: graphql
     auth: none
   }
   
   body:graphql {
     query { ... }
   }
   ```

### Environment Variables

Create `bruno/.env` for different environments:
```
GRAPHQL_URL=http://localhost:8080/graphql
```

Update requests to use variables:
```
url: {{GRAPHQL_URL}}
```

## Troubleshooting

### Server Not Running
```
Error: connect ECONNREFUSED 127.0.0.1:8080
```
**Solution**: Start the server with `make dev`

### Invalid Date Format
```
Error: Cannot parse date
```
**Solution**: Use ISO format: `YYYY-MM-DD`

### Room Not Available
```
Error: Room is not available for the selected dates
```
**Solution**: Query available rooms first, or use different dates

## References

- [Bruno Documentation](https://docs.usebruno.com/)
- [GraphQL Specification](https://graphql.org/learn/)
- [otel-motel GraphQL UI](http://localhost:8080/q/graphql-ui)

## Contributing

When adding new API endpoints:
1. Create corresponding Bruno requests
2. Follow the existing naming convention
3. Add examples to this README
4. Test all requests before committing
