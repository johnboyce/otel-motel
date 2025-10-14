package com.johnnyb.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void testRoomBuilder() {
        String id = UUID.randomUUID().toString();
        String hotelId = UUID.randomUUID().toString();
        Room room = Room.builder()
            .id(id)
            .hotelId(hotelId)
            .roomNumber("101")
            .roomType("Standard")
            .pricePerNight(new BigDecimal("120.00"))
            .capacity(2)
            .description("Standard room with modern amenities")
            .bookingIds(new ArrayList<>())
            .build();

        assertNotNull(room);
        assertEquals(id, room.getId());
        assertEquals(hotelId, room.getHotelId());
        assertEquals("101", room.getRoomNumber());
        assertEquals("Standard", room.getRoomType());
        assertEquals(new BigDecimal("120.00"), room.getPricePerNight());
        assertEquals(2, room.getCapacity());
        assertEquals("Standard room with modern amenities", room.getDescription());
        assertNotNull(room.getBookingIds());
        assertTrue(room.getBookingIds().isEmpty());
    }

    @Test
    void testRoomSetters() {
        Room room = new Room();
        String id = UUID.randomUUID().toString();
        String hotelId = UUID.randomUUID().toString();
        
        room.setId(id);
        room.setHotelId(hotelId);
        room.setRoomNumber("202");
        room.setRoomType("Deluxe");
        room.setPricePerNight(new BigDecimal("180.00"));
        room.setCapacity(2);
        room.setDescription("Deluxe room");
        room.setBookingIds(new ArrayList<>());

        assertEquals(id, room.getId());
        assertEquals(hotelId, room.getHotelId());
        assertEquals("202", room.getRoomNumber());
        assertEquals("Deluxe", room.getRoomType());
        assertEquals(new BigDecimal("180.00"), room.getPricePerNight());
        assertEquals(2, room.getCapacity());
    }

    @Test
    void testRoomEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        Room room1 = Room.builder()
            .id(id)
            .roomNumber("101")
            .roomType("Standard")
            .build();

        Room room2 = Room.builder()
            .id(id)
            .roomNumber("101")
            .roomType("Standard")
            .build();

        assertEquals(room1, room2);
        assertEquals(room1.hashCode(), room2.hashCode());
    }
}
