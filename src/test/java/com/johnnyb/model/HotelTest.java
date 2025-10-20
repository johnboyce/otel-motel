package com.johnnyb.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HotelTest {

    @Test
    void testHotelBuilder() {
        String id = UUID.randomUUID().toString();
        Hotel hotel = Hotel.builder()
            .id(id)
            .name("Grand Pacific Resort")
            .address("100 Beachfront Drive")
            .city("Miami Beach")
            .state("FL")
            .country("USA")
            .description("A luxurious beachfront resort")
            .starRating(5)
            .roomIds(new ArrayList<>())
            .build();

        assertNotNull(hotel);
        assertEquals(id, hotel.getId());
        assertEquals("Grand Pacific Resort", hotel.getName());
        assertEquals("100 Beachfront Drive", hotel.getAddress());
        assertEquals("Miami Beach", hotel.getCity());
        assertEquals("FL", hotel.getState());
        assertEquals("USA", hotel.getCountry());
        assertEquals("A luxurious beachfront resort", hotel.getDescription());
        assertEquals(5, hotel.getStarRating());
        assertNotNull(hotel.getRoomIds());
        assertTrue(hotel.getRoomIds().isEmpty());
    }

    @Test
    void testHotelSetters() {
        Hotel hotel = new Hotel();
        String id = UUID.randomUUID().toString();
        
        hotel.setId(id);
        hotel.setName("Metropolitan Business Hotel");
        hotel.setAddress("250 Corporate Plaza");
        hotel.setCity("New York");
        hotel.setState("NY");
        hotel.setCountry("USA");
        hotel.setDescription("Modern business hotel");
        hotel.setStarRating(4);
        hotel.setRoomIds(new ArrayList<>());

        assertEquals(id, hotel.getId());
        assertEquals("Metropolitan Business Hotel", hotel.getName());
        assertEquals("New York", hotel.getCity());
        assertEquals("NY", hotel.getState());
        assertEquals(4, hotel.getStarRating());
    }

    @Test
    void testHotelEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        Hotel hotel1 = Hotel.builder()
            .id(id)
            .name("Test Hotel")
            .city("Test City")
            .build();

        Hotel hotel2 = Hotel.builder()
            .id(id)
            .name("Test Hotel")
            .city("Test City")
            .build();

        assertEquals(hotel1, hotel2);
        assertEquals(hotel1.hashCode(), hotel2.hashCode());
    }
}
