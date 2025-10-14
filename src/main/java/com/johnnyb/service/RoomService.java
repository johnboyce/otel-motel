package com.johnnyb.service;

import com.johnnyb.model.Room;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoomService {

    private static final Logger LOG = Logger.getLogger(RoomService.class);
    private static final String TABLE_NAME = "rooms";

    @Inject
    DynamoDbEnhancedClient dynamoDb;

    private DynamoDbTable<Room> roomTable;

    @PostConstruct
    void init() {
        roomTable = dynamoDb.table(TABLE_NAME, TableSchema.fromBean(Room.class));
    }

    public Room save(Room room) {
        LOG.infof("Saving room: %s", room.getId());
        roomTable.putItem(room);
        return room;
    }

    public Optional<Room> findById(String id) {
        LOG.infof("Finding room by ID: %s", id);
        try {
            Room room = roomTable.getItem(Key.builder().partitionValue(id).build());
            return Optional.ofNullable(room);
        } catch (ResourceNotFoundException e) {
            LOG.warnf("Room not found: %s", id);
            return Optional.empty();
        }
    }

    public List<Room> findAll() {
        LOG.info("Finding all rooms");
        List<Room> rooms = new ArrayList<>();
        try {
            PageIterable<Room> pages = roomTable.scan();
            pages.items().forEach(rooms::add);
        } catch (ResourceNotFoundException e) {
            LOG.warn("Room table not found");
        }
        return rooms;
    }

    public List<Room> findByHotelId(String hotelId) {
        LOG.infof("Finding rooms by hotel ID: %s", hotelId);
        return findAll().stream()
                .filter(r -> hotelId.equals(r.getHotelId()))
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        LOG.infof("Deleting room: %s", id);
        roomTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public long count() {
        return findAll().size();
    }
}
