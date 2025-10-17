package com.johnnyb.service;

import com.johnnyb.model.Hotel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class HotelService implements IHotelService {

    private static final Logger LOG = Logger.getLogger(HotelService.class);
    private static final String TABLE_NAME = "hotels";

    @Inject
    DynamoDbEnhancedClient dynamoDb;

    private DynamoDbTable<Hotel> hotelTable;

    @PostConstruct
    void init() {
        hotelTable = dynamoDb.table(TABLE_NAME, Hotel.HOTEL_TABLE_SCHEMA);
    }

    @Override
    public Hotel save(Hotel hotel) {
        LOG.infof("Saving hotel: %s", hotel.getId());
        // Prevent DynamoDB empty set error
        if (hotel.getRoomIds() != null && hotel.getRoomIds().isEmpty()) {
            hotel.setRoomIds(null);
        }
        hotelTable.putItem(hotel);
        return hotel;
    }

    @Override
    public Optional<Hotel> findById(String id) {
        LOG.infof("Finding hotel by ID: %s", id);
        try {
            var hotel = hotelTable.getItem(Key.builder().partitionValue(id).build());
            return Optional.ofNullable(hotel);
        } catch (ResourceNotFoundException e) {
            LOG.warnf("Hotel not found: %s", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Hotel> findAll() {
        LOG.info("Finding all hotels");
        var hotels = new ArrayList<Hotel>();
        try {
            var pages = hotelTable.scan();
            pages.items().forEach(hotels::add);
        } catch (ResourceNotFoundException e) {
            LOG.warn("Hotel table not found");
        }
        return hotels;
    }

    @Override
    public List<Hotel> findByCity(String city) {
        LOG.infof("Finding hotels by city: %s", city);
        return findAll().stream()
                .filter(h -> city.equals(h.getCity()))
                .toList();
    }

    @Override
    public List<Hotel> findByCountry(String country) {
        LOG.infof("Finding hotels by country: %s", country);
        return findAll().stream()
                .filter(h -> country.equals(h.getCountry()))
                .toList();
    }

    @Override
    public void delete(String id) {
        LOG.infof("Deleting hotel: %s", id);
        hotelTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    @Override
    public long count() {
        return findAll().size();
    }
}
