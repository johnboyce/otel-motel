package com.johnnyb.service;

import com.johnnyb.model.Hotel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class HotelServiceTest {

    @Inject
    IHotelService hotelService;

    @Test
    void testSaveAndFindById() {
        var id = UUID.randomUUID().toString();
        var hotel = Hotel.builder()
            .id(id)
            .name("Test Hotel")
            .address("123 Test Ave")
            .city("Test City")
            .country("Test Country")
            .description("A test hotel")
            .starRating(4)
            .roomIds(new ArrayList<>())
            .build();

        hotelService.save(hotel);

        var found = hotelService.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Test Hotel", found.get().getName());
        assertEquals("Test City", found.get().getCity());
        assertEquals(4, found.get().getStarRating());
        
        // Cleanup
        hotelService.delete(id);
    }

    @Test
    void testFindByCity() {
        var id = UUID.randomUUID().toString();
        var uniqueCity = "UniqueCity" + id.substring(0, 8);
        var hotel = Hotel.builder()
            .id(id)
            .name("City Test Hotel")
            .address("456 Test Blvd")
            .city(uniqueCity)
            .country("Test Country")
            .description("A test hotel in a unique city")
            .starRating(3)
            .roomIds(new ArrayList<>())
            .build();

        hotelService.save(hotel);

        var found = hotelService.findByCity(uniqueCity);
        assertNotNull(found);
        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(h -> h.getName().equals("City Test Hotel")));
        
        // Cleanup
        hotelService.delete(id);
    }

    @Test
    void testFindByCountry() {
        var hotels = hotelService.findByCountry("USA");
        assertNotNull(hotels);
        // Note: This will include sample data from initialization
        assertFalse(hotels.isEmpty());
    }

    @Test
    void testFindAll() {
        var hotels = hotelService.findAll();
        assertNotNull(hotels);
        // Note: This will include sample data from initialization
        assertFalse(hotels.isEmpty());
    }

    @Test
    void testDelete() {
        var id = UUID.randomUUID().toString();
        var hotel = Hotel.builder()
            .id(id)
            .name("Delete Test Hotel")
            .address("789 Delete Rd")
            .city("Delete City")
            .country("Delete Country")
            .description("A hotel to be deleted")
            .starRating(2)
            .roomIds(new ArrayList<>())
            .build();

        hotelService.save(hotel);
        assertTrue(hotelService.findById(id).isPresent());

        hotelService.delete(id);
        assertFalse(hotelService.findById(id).isPresent());
    }
}
