package com.johnnyb.service;

import com.johnnyb.model.Booking;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookingService {

    private static final Logger LOG = Logger.getLogger(BookingService.class);
    private static final String TABLE_NAME = "bookings";

    @Inject
    DynamoDbEnhancedClient dynamoDb;

    private DynamoDbTable<Booking> bookingTable;

    @PostConstruct
    void init() {
        bookingTable = dynamoDb.table(TABLE_NAME, TableSchema.fromBean(Booking.class));
    }

    public Booking save(Booking booking) {
        LOG.infof("Saving booking: %s", booking.getId());
        bookingTable.putItem(booking);
        return booking;
    }

    public Optional<Booking> findById(String id) {
        LOG.infof("Finding booking by ID: %s", id);
        try {
            Booking booking = bookingTable.getItem(Key.builder().partitionValue(id).build());
            return Optional.ofNullable(booking);
        } catch (ResourceNotFoundException e) {
            LOG.warnf("Booking not found: %s", id);
            return Optional.empty();
        }
    }

    public List<Booking> findAll() {
        LOG.info("Finding all bookings");
        List<Booking> bookings = new ArrayList<>();
        try {
            PageIterable<Booking> pages = bookingTable.scan();
            pages.items().forEach(bookings::add);
        } catch (ResourceNotFoundException e) {
            LOG.warn("Booking table not found");
        }
        return bookings;
    }

    public List<Booking> findByCustomerId(String customerId) {
        LOG.infof("Finding bookings by customer ID: %s", customerId);
        return findAll().stream()
                .filter(b -> customerId.equals(b.getCustomerId()))
                .collect(Collectors.toList());
    }

    public List<Booking> findByRoomId(String roomId) {
        LOG.infof("Finding bookings by room ID: %s", roomId);
        return findAll().stream()
                .filter(b -> roomId.equals(b.getRoomId()))
                .collect(Collectors.toList());
    }

    public List<Booking> findUpcomingBookings() {
        LOG.info("Finding upcoming bookings");
        LocalDate today = LocalDate.now();
        return findAll().stream()
                .filter(b -> b.getCheckInDate() != null && !b.getCheckInDate().isBefore(today))
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .collect(Collectors.toList());
    }

    public List<Booking> findOverlappingBookings(String roomId, LocalDate checkIn, LocalDate checkOut) {
        LOG.infof("Finding overlapping bookings for room %s from %s to %s", roomId, checkIn, checkOut);
        return findByRoomId(roomId).stream()
                .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
                .filter(b -> b.getCheckInDate() != null && b.getCheckOutDate() != null)
                .filter(b -> b.getCheckInDate().isBefore(checkOut) && b.getCheckOutDate().isAfter(checkIn))
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        LOG.infof("Deleting booking: %s", id);
        bookingTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public long count() {
        return findAll().size();
    }
}
