# Migration to DynamoDB - Implementation Guide

## Overview

The infrastructure and configuration have been updated to use DynamoDB (LocalStack) instead of PostgreSQL, and Logstash has been removed. This document outlines the remaining code changes needed to complete the migration.

## ✅ Completed Infrastructure Changes

1. **Docker Compose**: PostgreSQL replaced with LocalStack DynamoDB
2. **Dependencies**: Removed Hibernate/PostgreSQL, added AWS SDK for DynamoDB
3. **Configuration**: Updated application.properties for DynamoDB endpoint
4. **Documentation**: All docs updated to reference DynamoDB
5. **Logstash**: Completely removed; OTEL exports directly to Elasticsearch
6. **Makefile**: Commands updated for DynamoDB operations

## ⚠️ Code Migration Required

The following code changes are needed to make the application functional with DynamoDB:

### 1. Entity Models (4 files)

All entities need to be converted from JPA annotations to DynamoDB Enhanced Client annotations:

#### Before (JPA/Hibernate):
```java
@Entity
@Table(name = "hotels")
public class Hotel extends PanacheEntity {
    @Column(nullable = false)
    public String name;
    
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    public List<Room> rooms;
}
```

#### After (DynamoDB):
```java
@DynamoDbBean
public class Hotel {
    private String id;  // UUID instead of auto-generated Long
    private String name;
    private List<String> roomIds;  // Store IDs instead of relationships
    
    @DynamoDbPartitionKey
    public String getId() { return id; }
    
    @DynamoDbAttribute("name")
    public String getName() { return name; }
    
    // No direct relationships - store IDs instead
}
```

**Files to modify:**
- `src/main/java/com/johnnyb/entity/Hotel.java`
- `src/main/java/com/johnnyb/entity/Room.java`
- `src/main/java/com/johnnyb/entity/Booking.java`
- `src/main/java/com/johnnyb/entity/Customer.java`

### 2. Repository/DAO Layer

DynamoDB doesn't use Panache's Active Record pattern. Need to create repository classes:

```java
@ApplicationScoped
public class HotelRepository {
    @Inject
    DynamoDbEnhancedClient dynamoDb;
    
    private DynamoDbTable<Hotel> hotelTable;
    
    @PostConstruct
    void init() {
        hotelTable = dynamoDb.table("hotels", TableSchema.fromBean(Hotel.class));
    }
    
    public Hotel findById(String id) {
        return hotelTable.getItem(Key.builder().partitionValue(id).build());
    }
    
    public List<Hotel> findAll() {
        return hotelTable.scan().items().stream().collect(Collectors.toList());
    }
    
    public void save(Hotel hotel) {
        hotelTable.putItem(hotel);
    }
}
```

**New files to create:**
- `src/main/java/com/johnnyb/repository/HotelRepository.java`
- `src/main/java/com/johnnyb/repository/RoomRepository.java`
- `src/main/java/com/johnnyb/repository/BookingRepository.java`
- `src/main/java/com/johnnyb/repository/CustomerRepository.java`

### 3. Data Initialization Service

Update `DataInitializationService.java` to:
- Create DynamoDB tables if they don't exist
- Use DynamoDB SDK instead of JPA persist operations
- Handle relationships via IDs instead of object references

```java
@ApplicationScoped
public class DataInitializationService {
    @Inject
    HotelRepository hotelRepository;
    
    void onStart(@Observes StartupEvent event) {
        createTablesIfNeeded();
        initializeData();
    }
    
    private void createTablesIfNeeded() {
        // Create tables using DynamoDB client
    }
    
    private void initializeData() {
        // Use repositories instead of persist()
        Hotel hotel = new Hotel();
        hotel.setId(UUID.randomUUID().toString());
        hotel.setName("Grand Pacific Resort");
        hotelRepository.save(hotel);
    }
}
```

### 4. GraphQL Resources

Update query resolvers to use repositories instead of Panache:

#### Before:
```java
public List<Hotel> hotels() {
    return Hotel.listAll();
}

public Hotel hotel(Long id) {
    return Hotel.findById(id);
}
```

#### After:
```java
@Inject
HotelRepository hotelRepository;

public List<Hotel> hotels() {
    return hotelRepository.findAll();
}

public Hotel hotel(String id) {  // Note: String instead of Long
    return hotelRepository.findById(id);
}
```

### 5. Relationship Handling

DynamoDB doesn't support foreign keys or joins. Options:

**Option A: Embed data**
```java
public class Booking {
    private String hotelName;  // Denormalized
    private String roomNumber; // Denormalized
}
```

**Option B: Multiple queries**
```java
public Hotel hotelWithRooms(String hotelId) {
    Hotel hotel = hotelRepository.findById(hotelId);
    List<Room> rooms = roomRepository.findByHotelId(hotelId);
    hotel.setRooms(rooms);
    return hotel;
}
```

**Option C: Global Secondary Index (GSI)**
```java
@DynamoDbSecondaryPartitionKey(indexNames = "hotel-id-index")
public String getHotelId() { return hotelId; }
```

### 6. Query Patterns

Common patterns need to be rewritten:

#### Find by attribute (requires GSI):
```java
// Add GSI to entity
@DynamoDbSecondarySortKey(indexNames = "city-index")
public String getCity() { return city; }

// Query using GSI
public List<Hotel> findByCity(String city) {
    return hotelTable.index("city-index")
        .query(QueryEnhancedRequest.builder()
            .queryConditional(QueryConditional.keyEqualTo(
                Key.builder().partitionValue(city).build()))
            .build())
        .items().stream().collect(Collectors.toList());
}
```

## Testing Strategy

1. **Unit Tests**: Test repositories with LocalStack
2. **Integration Tests**: Test with DynamoDB Local
3. **Manual Testing**: Use GraphQL UI to verify all operations

## Migration Steps

1. Update entity models with DynamoDB annotations
2. Create repository classes
3. Update DataInitializationService
4. Update GraphQL resolvers
5. Test basic CRUD operations
6. Implement query patterns with GSI
7. Update all tests
8. Verify observability still works

## Key Differences: SQL vs DynamoDB

| Aspect | PostgreSQL/JPA | DynamoDB |
|--------|----------------|----------|
| Primary Key | Auto-increment Long | UUID String |
| Relationships | Foreign keys, @OneToMany | IDs, denormalization |
| Queries | SQL, JPQL | Key-based, GSI |
| Joins | Native support | Application-level |
| Transactions | ACID | Limited, conditional |
| Schema | Rigid, migrations | Flexible, schemaless |

## Resources

- [Quarkus Amazon DynamoDB Guide](https://quarkus.io/guides/amazon-dynamodb)
- [AWS SDK Enhanced Client](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/dynamodb-enhanced-client.html)
- [DynamoDB Data Modeling](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-modeling-nosql.html)
- [LocalStack DynamoDB](https://docs.localstack.cloud/user-guide/aws/dynamodb/)

## Estimated Effort

- Entity conversion: 4-6 hours
- Repository creation: 3-4 hours  
- Service updates: 4-6 hours
- GraphQL updates: 2-3 hours
- Testing & debugging: 6-8 hours

**Total: 19-27 hours** of development work

## Notes

This is a significant architectural change from relational to NoSQL. Consider whether DynamoDB is the right choice for this use case, as the hotel booking domain has clear relational patterns that map well to SQL.

Alternative approaches:
- Keep using PostgreSQL in production, use LocalStack only for testing
- Use Amazon Aurora (PostgreSQL-compatible) for best of both worlds
- Consider DocumentDB or other document stores if NoSQL is required
