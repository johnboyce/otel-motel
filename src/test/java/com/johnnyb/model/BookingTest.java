package com.johnnyb.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void testBookingBuilder() {
        String id = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now();
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
            .specialRequests("Late check-in please")
            .build();

        assertNotNull(booking);
        assertEquals(id, booking.getId());
        assertEquals(roomId, booking.getRoomId());
        assertEquals(customerId, booking.getCustomerId());
        assertEquals(checkIn, booking.getCheckInDate());
        assertEquals(checkOut, booking.getCheckOutDate());
        assertEquals(2, booking.getNumberOfGuests());
        assertEquals(new BigDecimal("360.00"), booking.getTotalPrice());
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("Late check-in please", booking.getSpecialRequests());
    }

    @Test
    void testBookingSetters() {
        Booking booking = new Booking();
        String id = UUID.randomUUID().toString();
        String roomId = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(7);
        
        booking.setId(id);
        booking.setRoomId(roomId);
        booking.setCustomerId(customerId);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNumberOfGuests(4);
        booking.setTotalPrice(new BigDecimal("840.00"));
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setSpecialRequests("Non-smoking room");

        assertEquals(id, booking.getId());
        assertEquals(roomId, booking.getRoomId());
        assertEquals(customerId, booking.getCustomerId());
        assertEquals(4, booking.getNumberOfGuests());
        assertEquals(Booking.BookingStatus.PENDING, booking.getStatus());
    }

    @Test
    void testBookingStatusEnum() {
        assertEquals(4, Booking.BookingStatus.values().length);
        assertEquals(Booking.BookingStatus.PENDING, Booking.BookingStatus.valueOf("PENDING"));
        assertEquals(Booking.BookingStatus.CONFIRMED, Booking.BookingStatus.valueOf("CONFIRMED"));
        assertEquals(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.valueOf("CANCELLED"));
        assertEquals(Booking.BookingStatus.COMPLETED, Booking.BookingStatus.valueOf("COMPLETED"));
    }

    @Test
    void testBookingEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        Booking booking1 = Booking.builder()
            .id(id)
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        Booking booking2 = Booking.builder()
            .id(id)
            .status(Booking.BookingStatus.CONFIRMED)
            .build();

        assertEquals(booking1, booking2);
        assertEquals(booking1.hashCode(), booking2.hashCode());
    }
}
