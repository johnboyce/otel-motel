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
    BookingService bookingService;

    @Test
    void testSaveAndFindById() {
        String id = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
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

        Optional<Booking> found = bookingService.findById(id);
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
        String customerId = UUID.randomUUID().toString();
        String booking1Id = UUID.randomUUID().toString();
        String booking2Id = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now().plusDays(15);

        Booking booking1 = Booking.builder()
            .id(booking1Id)
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkIn)
            .checkOutDate(checkIn.plusDays(2))
            .numberOfGuests(2)
            .totalPrice(new BigDecimal("240.00"))
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        Booking booking2 = Booking.builder()
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

        List<Booking> found = bookingService.findByCustomerId(customerId);
        assertNotNull(found);
        assertEquals(2, found.size());
        
        // Cleanup
        bookingService.delete(booking1Id);
        bookingService.delete(booking2Id);
    }

    @Test
    void testFindByRoomId() {
        String roomId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now().plusDays(20);

        Booking booking = Booking.builder()
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

        List<Booking> found = bookingService.findByRoomId(roomId);
        assertNotNull(found);
        assertFalse(found.isEmpty());
        
        // Cleanup
        bookingService.delete(bookingId);
    }

    @Test
    void testFindUpcomingBookings() {
        List<Booking> upcoming = bookingService.findUpcomingBookings();
        assertNotNull(upcoming);
        // Note: This will include sample data from initialization
    }

    @Test
    void testFindOverlappingBookings() {
        String roomId = UUID.randomUUID().toString();
        String bookingId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(5);

        Booking booking = Booking.builder()
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
        List<Booking> overlapping = bookingService.findOverlappingBookings(
            roomId, 
            checkIn.plusDays(2), 
            checkOut.plusDays(2)
        );
        assertNotNull(overlapping);
        assertEquals(1, overlapping.size());
        assertEquals(bookingId, overlapping.get(0).getId());

        // Test non-overlapping dates
        List<Booking> nonOverlapping = bookingService.findOverlappingBookings(
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
        String id = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now().plusDays(40);

        Booking booking = Booking.builder()
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
