package com.johnnyb.service;

import com.johnnyb.model.Hotel;
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
public class HotelService {

    private static final Logger LOG = Logger.getLogger(HotelService.class);
    private static final String TABLE_NAME = "hotels";

    @Inject
    DynamoDbEnhancedClient dynamoDb;

    private DynamoDbTable<Hotel> hotelTable;

    @PostConstruct
    void init() {
        hotelTable = dynamoDb.table(TABLE_NAME, TableSchema.fromBean(Hotel.class));
    }

    public Hotel save(Hotel hotel) {
        LOG.infof("Saving hotel: %s", hotel.getId());
        hotelTable.putItem(hotel);
        return hotel;
    }

    public Optional<Hotel> findById(String id) {
        LOG.infof("Finding hotel by ID: %s", id);
        try {
            Hotel hotel = hotelTable.getItem(Key.builder().partitionValue(id).build());
            return Optional.ofNullable(hotel);
        } catch (ResourceNotFoundException e) {
            LOG.warnf("Hotel not found: %s", id);
            return Optional.empty();
        }
    }

    public List<Hotel> findAll() {
        LOG.info("Finding all hotels");
        List<Hotel> hotels = new ArrayList<>();
        try {
            PageIterable<Hotel> pages = hotelTable.scan();
            pages.items().forEach(hotels::add);
        } catch (ResourceNotFoundException e) {
            LOG.warn("Hotel table not found");
        }
        return hotels;
    }

    public List<Hotel> findByCity(String city) {
        LOG.infof("Finding hotels by city: %s", city);
        return findAll().stream()
                .filter(h -> city.equals(h.getCity()))
                .collect(Collectors.toList());
    }

    public List<Hotel> findByCountry(String country) {
        LOG.infof("Finding hotels by country: %s", country);
        return findAll().stream()
                .filter(h -> country.equals(h.getCountry()))
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        LOG.infof("Deleting hotel: %s", id);
        hotelTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public long count() {
        return findAll().size();
    }
}
