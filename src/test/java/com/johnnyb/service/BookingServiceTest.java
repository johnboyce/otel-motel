package com.johnnyb.service;

import com.johnnyb.model.Booking;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BookingServiceTest {

    @Inject
    IBookingService bookingService;

    @Test
    void testSaveAndFindById() {
        var id = UUID.randomUUID().toString();
        var roomId = UUID.randomUUID().toString();
        var customerId = UUID.randomUUID().toString();
        var checkIn = LocalDate.now().plusDays(10);
        var checkOut = checkIn.plusDays(3);

        var booking = Booking.builder()
            .id(id)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .numberOfGuests(2)
            .totalPrice(new BigDecimal("360.00"))
            .status(Booking.BookingStatus.CONFIRMED)
            .specialRequests("Test booking")
            .build();

        bookingService.save(booking);

        var found = bookingService.findById(id);
        assertTrue(found.isPresent());
        assertEquals(roomId, found.get().getRoomId());
        assertEquals(customerId, found.get().getCustomerId());
        assertEquals(checkIn, found.get().getCheckInDate());
        assertEquals(checkOut, found.get().getCheckOutDate());
        assertEquals(Booking.BookingStatus.CONFIRMED, found.get().getStatus());
        
        // Cleanup
        bookingService.delete(id);
    }

    @Test
    void testFindByCustomerId() {
        var customerId = UUID.randomUUID().toString();
        var booking1Id = UUID.randomUUID().toString();
        var booking2Id = UUID.randomUUID().toString();
        var roomId = UUID.randomUUID().toString();
        var checkIn = LocalDate.now().plusDays(15);

        var booking1 = Booking.builder()
            .id(booking1Id)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkIn.plusDays(2))
            .numberOfGuests(2)
            .totalPrice(new BigDecimal("240.00"))
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        var booking2 = Booking.builder()
            .id(booking2Id)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn.plusDays(10))
            .checkOutDate(checkIn.plusDays(13))
            .numberOfGuests(1)
            .totalPrice(new BigDecimal("360.00"))
            .status(Booking.BookingStatus.PENDING)
            .build();

        bookingService.save(booking1);
        bookingService.save(booking2);

        var found = bookingService.findByCustomerId(customerId);
        assertNotNull(found);
        assertEquals(2, found.size());
        
        // Cleanup
        bookingService.delete(booking1Id);
        bookingService.delete(booking2Id);
    }

    @Test
    void testFindByRoomId() {
        var roomId = UUID.randomUUID().toString();
        var bookingId = UUID.randomUUID().toString();
        var customerId = UUID.randomUUID().toString();
        var checkIn = LocalDate.now().plusDays(20);

        var booking = Booking.builder()
            .id(bookingId)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkIn.plusDays(1))
            .numberOfGuests(1)
            .totalPrice(new BigDecimal("120.00"))
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        bookingService.save(booking);

        var found = bookingService.findByRoomId(roomId);
        assertNotNull(found);
        assertFalse(found.isEmpty());
        
        // Cleanup
        bookingService.delete(bookingId);
    }

    @Test
    void testFindUpcomingBookings() {
        var upcoming = bookingService.findUpcomingBookings();
        assertNotNull(upcoming);
        // Note: This will include sample data from initialization
    }

    @Test
    void testFindOverlappingBookings() {
        var roomId = UUID.randomUUID().toString();
        var bookingId = UUID.randomUUID().toString();
        var customerId = UUID.randomUUID().toString();
        var checkIn = LocalDate.now().plusDays(30);
        var checkOut = checkIn.plusDays(5);

        var booking = Booking.builder()
            .id(bookingId)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkOut)
            .numberOfGuests(2)
            .totalPrice(new BigDecimal("600.00"))
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        bookingService.save(booking);

        // Test overlapping dates
        var overlapping = bookingService.findOverlappingBookings(
            roomId, 
            checkIn.plusDays(2), 
            checkOut.plusDays(2)
        );
        assertNotNull(overlapping);
        assertEquals(1, overlapping.size());
        assertEquals(bookingId, overlapping.get(0).getId());

        // Test non-overlapping dates
        var nonOverlapping = bookingService.findOverlappingBookings(
            roomId, 
            checkOut.plusDays(1), 
            checkOut.plusDays(5)
        );
        assertTrue(nonOverlapping.isEmpty());
        
        // Cleanup
        bookingService.delete(bookingId);
    }

    @Test
    void testDelete() {
        var id = UUID.randomUUID().toString();
        var roomId = UUID.randomUUID().toString();
        var customerId = UUID.randomUUID().toString();
        var checkIn = LocalDate.now().plusDays(40);

        var booking = Booking.builder()
            .id(id)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkIn.plusDays(2))
            .numberOfGuests(1)
            .totalPrice(new BigDecimal("240.00"))
            .status(Booking.BookingStatus.PENDING)
            .build();

        bookingService.save(booking);
        assertTrue(bookingService.findById(id).isPresent());

        bookingService.delete(id);
        assertFalse(bookingService.findById(id).isPresent());
    }
}
