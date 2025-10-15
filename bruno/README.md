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

## Authentication Setup

The collection supports three environments with different authentication modes:

### 1. Development (No Auth)
- **Use for:** Local development without security
- **Authentication:** Disabled
- **Best for:** Rapid development and testing

### 2. User
- **Use for:** Testing as a regular user
- **Username:** user1
- **Password:** password
- **Roles:** `user`
- **Can access:** Public endpoints, own bookings, customer data

### 3. Admin
- **Use for:** Testing as an administrator
- **Username:** admin1
- **Password:** admin
- **Roles:** `admin`, `user`
- **Can access:** All endpoints including admin-only operations

## Quick Start

### 1. Start Services
```bash
# Start all services including Keycloak
docker compose up -d

# Wait for Keycloak to initialize (60-90 seconds)
docker compose logs -f keycloak
```

### 2. Start Application
```bash
# Development mode (no auth)
make dev

# Or with authentication enabled
./mvnw quarkus:dev -Dquarkus.profile=prod
```

### 3. Open Bruno
1. Launch Bruno
2. File → Open Collection
3. Navigate to `./bruno/ote-motel`
4. Select an environment from the dropdown

### 4. Run Requests
- The pre-request script automatically obtains JWT tokens
- Click "Send" on any request
- Tokens are automatically attached

## Collection Structure

```
bruno/ote-motel/
├── collection.bru                          # Collection config with pre-request script
├── environments/
│   ├── Development (No Auth).bru          # No authentication
│   ├── User.bru                           # User role
│   └── Admin.bru                          # Admin role
├── Hotels/
│   ├── Get All Hotels.bru                 # Public
│   ├── Get Hotel by ID.bru                # Public
│   └── Get Hotels by City.bru             # Public
├── Rooms/
│   ├── Get Available Rooms.bru            # Public
│   └── Get Rooms by Hotel.bru             # Public
├── Bookings/
│   ├── Create Booking.bru                 # Requires: user, admin
│   ├── Get Upcoming Bookings.bru          # Requires: admin only
│   ├── Get Bookings by Customer.bru       # Requires: user, admin
│   └── Cancel Booking.bru                 # Requires: user, admin
└── Customers/
    └── Get Customer by Email.bru          # Requires: user, admin
```

## Usage

### Prerequisites
1. Start Keycloak and all services:
   ```bash
   docker compose up -d
   ```

2. Wait for Keycloak initialization (check logs):
   ```bash
   docker compose logs -f keycloak
   ```

3. Start the application:
   ```bash
   make dev  # For no-auth development
   # or
   ./mvnw quarkus:dev -Dquarkus.profile=prod  # For auth-enabled testing
   ```

4. Ensure the GraphQL endpoint is accessible:
   ```
   http://localhost:8080/graphql
   ```

### Using Bruno GUI

1. Open Bruno
2. Open Collection: `File` → `Open Collection`
3. Select the `bruno/ote-motel` directory
4. **Select an environment** from the dropdown (top-right):
   - "Development (No Auth)" - for local testing without security
   - "User" - for testing with user role
   - "Admin" - for testing with admin role
5. Execute requests by clicking "Send"

The collection includes automatic token management:
- Pre-request scripts fetch JWT tokens from Keycloak
- Tokens are automatically attached to requests
- No manual token management required

### Using Bruno CLI

Run all requests:
```bash
bru run bruno/ote-motel/
```

Run specific folder:
```bash
bru run bruno/ote-motel/Hotels/
```

Run specific request:
```bash
bru run bruno/ote-motel/Hotels/"Get All Hotels.bru"
```

Run with specific environment:
```bash
bru run bruno/ote-motel/ --env "User"
```

## API Endpoints

### Security Roles

The API implements role-based access control with two roles:

**Public Access (No Authentication)**
- All hotel queries
- All room queries

**User Role (`user`)**
- View and create own bookings
- View customer information
- Cancel bookings

**Admin Role (`admin`)**
- All user permissions
- View all upcoming bookings across all customers

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
- [Security Setup Guide](../docs/SECURITY.md) - Complete security and authentication guide
- [Keycloak Admin Console](http://localhost:8180) - Username: admin, Password: admin

## Contributing

When adding new API endpoints:
1. Create corresponding Bruno requests
2. Follow the existing naming convention
3. Add examples to this README
4. Test all requests before committing
