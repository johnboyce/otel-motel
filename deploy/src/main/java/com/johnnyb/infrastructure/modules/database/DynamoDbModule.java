package com.johnnyb.infrastructure.modules.database;

import com.pulumi.aws.dynamodb.Table;
import com.pulumi.aws.dynamodb.TableArgs;
import com.pulumi.aws.dynamodb.inputs.TableAttributeArgs;
import com.pulumi.aws.dynamodb.inputs.TableGlobalSecondaryIndexArgs;
import com.pulumi.core.Output;

import java.util.List;
import java.util.Map;

/**
 * DynamoDB Module - Creates DynamoDB tables for application data
 */
public class DynamoDbModule {
    private final String name;
    private final String environment;
    private final String billingMode;
    
    private Table hotelsTable;
    private Table roomsTable;
    private Table customersTable;
    private Table bookingsTable;

    /**
     * Create DynamoDB tables
     * 
     * @param name Base name for resources
     * @param environment Environment name
     * @param billingMode Billing mode (PAY_PER_REQUEST or PROVISIONED)
     */
    public DynamoDbModule(String name, String environment, String billingMode) {
        this.name = name;
        this.environment = environment;
        this.billingMode = billingMode;
        
        createHotelsTable();
        createRoomsTable();
        createCustomersTable();
        createBookingsTable();
    }

    /**
     * Create Hotels table
     */
    private void createHotelsTable() {
        this.hotelsTable = new Table(name + "-hotels", TableArgs.builder()
            .name(name + "-hotels")
            .billingMode(billingMode)
            .hashKey("id")
            .attributes(
                TableAttributeArgs.builder()
                    .name("id")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("city")
                    .type("S")
                    .build()
            )
            .globalSecondaryIndexes(
                TableGlobalSecondaryIndexArgs.builder()
                    .name("CityIndex")
                    .hashKey("city")
                    .projectionType("ALL")
                    .build()
            )
            .pointInTimeRecovery(builder -> builder.enabled(environment.equals("prod")))
            .tags(Map.of(
                "Name", name + "-hotels",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Hotel data storage"
            ))
            .build());
    }

    /**
     * Create Rooms table
     */
    private void createRoomsTable() {
        this.roomsTable = new Table(name + "-rooms", TableArgs.builder()
            .name(name + "-rooms")
            .billingMode(billingMode)
            .hashKey("id")
            .attributes(
                TableAttributeArgs.builder()
                    .name("id")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("hotelId")
                    .type("N")
                    .build()
            )
            .globalSecondaryIndexes(
                TableGlobalSecondaryIndexArgs.builder()
                    .name("HotelIndex")
                    .hashKey("hotelId")
                    .projectionType("ALL")
                    .build()
            )
            .pointInTimeRecovery(builder -> builder.enabled(environment.equals("prod")))
            .tags(Map.of(
                "Name", name + "-rooms",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Room data storage"
            ))
            .build());
    }

    /**
     * Create Customers table
     */
    private void createCustomersTable() {
        this.customersTable = new Table(name + "-customers", TableArgs.builder()
            .name(name + "-customers")
            .billingMode(billingMode)
            .hashKey("id")
            .attributes(
                TableAttributeArgs.builder()
                    .name("id")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("email")
                    .type("S")
                    .build()
            )
            .globalSecondaryIndexes(
                TableGlobalSecondaryIndexArgs.builder()
                    .name("EmailIndex")
                    .hashKey("email")
                    .projectionType("ALL")
                    .build()
            )
            .pointInTimeRecovery(builder -> builder.enabled(environment.equals("prod")))
            .tags(Map.of(
                "Name", name + "-customers",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Customer data storage"
            ))
            .build());
    }

    /**
     * Create Bookings table
     */
    private void createBookingsTable() {
        this.bookingsTable = new Table(name + "-bookings", TableArgs.builder()
            .name(name + "-bookings")
            .billingMode(billingMode)
            .hashKey("id")
            .attributes(
                TableAttributeArgs.builder()
                    .name("id")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("customerId")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("roomId")
                    .type("N")
                    .build(),
                TableAttributeArgs.builder()
                    .name("checkInDate")
                    .type("S")
                    .build()
            )
            .globalSecondaryIndexes(
                TableGlobalSecondaryIndexArgs.builder()
                    .name("CustomerIndex")
                    .hashKey("customerId")
                    .rangeKey("checkInDate")
                    .projectionType("ALL")
                    .build(),
                TableGlobalSecondaryIndexArgs.builder()
                    .name("RoomIndex")
                    .hashKey("roomId")
                    .rangeKey("checkInDate")
                    .projectionType("ALL")
                    .build()
            )
            .pointInTimeRecovery(builder -> builder.enabled(environment.equals("prod")))
            .tags(Map.of(
                "Name", name + "-bookings",
                "Environment", environment,
                "ManagedBy", "Pulumi",
                "Purpose", "Booking data storage"
            ))
            .build());
    }

    // Getters
    public Table getHotelsTable() {
        return hotelsTable;
    }

    public Table getRoomsTable() {
        return roomsTable;
    }

    public Table getCustomersTable() {
        return customersTable;
    }

    public Table getBookingsTable() {
        return bookingsTable;
    }

    public Output<String> getHotelsTableName() {
        return hotelsTable.name();
    }

    public Output<String> getRoomsTableName() {
        return roomsTable.name();
    }

    public Output<String> getCustomersTableName() {
        return customersTable.name();
    }

    public Output<String> getBookingsTableName() {
        return bookingsTable.name();
    }

    public Output<String> getHotelsTableArn() {
        return hotelsTable.arn();
    }

    public Output<String> getRoomsTableArn() {
        return roomsTable.arn();
    }

    public Output<String> getCustomersTableArn() {
        return customersTable.arn();
    }

    public Output<String> getBookingsTableArn() {
        return bookingsTable.arn();
    }
}
