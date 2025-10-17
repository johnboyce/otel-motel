package com.johnnyb.service;

import com.johnnyb.model.Room;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RoomServiceTest {

    @Inject
    IRoomService roomService;

    @Test
    void testSaveAndFindById() {
        var id = UUID.randomUUID().toString();
        var hotelId = UUID.randomUUID().toString();
        var room = Room.builder()
            .id(id)
            .hotelId(hotelId)
            .roomNumber("999")
            .roomType("Test Suite")
            .pricePerNight(new BigDecimal("199.99"))
            .capacity(2)
            .description("A test room")
            .bookingIds(new ArrayList<>())
            .build();

        roomService.save(room);

        var found = roomService.findById(id);
        assertTrue(found.isPresent());
        assertEquals("999", found.get().getRoomNumber());
        assertEquals("Test Suite", found.get().getRoomType());
        assertEquals(new BigDecimal("199.99"), found.get().getPricePerNight());
        assertEquals(2, found.get().getCapacity());
        
        // Cleanup
        roomService.delete(id);
    }

    @Test
    void testFindByHotelId() {
        var hotelId = UUID.randomUUID().toString();
        var room1Id = UUID.randomUUID().toString();
        var room2Id = UUID.randomUUID().toString();

        var room1 = Room.builder()
            .id(room1Id)
            .hotelId(hotelId)
            .roomNumber("101")
            .roomType("Standard")
            .pricePerNight(new BigDecimal("120.00"))
            .capacity(2)
            .description("Standard room 1")
            .bookingIds(new ArrayList<>())
            .build();

        var room2 = Room.builder()
            .id(room2Id)
            .hotelId(hotelId)
            .roomNumber("102")
            .roomType("Standard")
            .pricePerNight(new BigDecimal("120.00"))
            .capacity(2)
            .description("Standard room 2")
            .bookingIds(new ArrayList<>())
            .build();

        roomService.save(room1);
        roomService.save(room2);

        var found = roomService.findByHotelId(hotelId);
        assertNotNull(found);
        assertEquals(2, found.size());
        
        // Cleanup
        roomService.delete(room1Id);
        roomService.delete(room2Id);
    }

    @Test
    void testFindAll() {
        var rooms = roomService.findAll();
        assertNotNull(rooms);
        // Note: This will include sample data from initialization
    }

    @Test
    void testDelete() {
        var id = UUID.randomUUID().toString();
        var hotelId = UUID.randomUUID().toString();
        var room = Room.builder()
            .id(id)
            .hotelId(hotelId)
            .roomNumber("888")
            .roomType("Delete Test")
            .pricePerNight(new BigDecimal("100.00"))
            .capacity(1)
            .description("A room to be deleted")
            .bookingIds(new ArrayList<>())
            .build();

        roomService.save(room);
        assertTrue(roomService.findById(id).isPresent());

        roomService.delete(id);
        assertFalse(roomService.findById(id).isPresent());
    }
}
