package com.johnnyb.service;

import com.johnnyb.entity.Booking;
import com.johnnyb.entity.Customer;
import com.johnnyb.entity.Hotel;
import com.johnnyb.entity.Room;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class DataInitializationService {

    private static final Logger LOG = Logger.getLogger(DataInitializationService.class);
    private static final Random RANDOM = new Random();

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        // Only initialize if database is empty
        if (Hotel.count() > 0) {
            LOG.info("Database already initialized, skipping data initialization");
            return;
        }

        LOG.info("Initializing database with sample data...");

        // Create customers first
        List<Customer> customers = createCustomers();
        LOG.infof("Created %d customers", customers.size());

        // Create hotels with rooms
        List<Hotel> hotels = createHotels();
        LOG.infof("Created %d hotels", hotels.size());

        // Create bookings (targeting ~50% capacity for next 3 months)
        createBookings(hotels, customers);
        LOG.info("Database initialization completed");
    }

    private List<Customer> createCustomers() {
        List<Customer> customers = new ArrayList<>();

        customers.add(new Customer(
            "John", "Doe", "john.doe@example.com", "+1-555-0101",
            "123 Main St, New York, NY 10001",
            "4532015112830366", "12/25", "123"
        ));

        customers.add(new Customer(
            "Jane", "Smith", "jane.smith@example.com", "+1-555-0102",
            "456 Oak Ave, Los Angeles, CA 90001",
            "5425233430109903", "11/26", "456"
        ));

        customers.add(new Customer(
            "Michael", "Johnson", "michael.johnson@example.com", "+1-555-0103",
            "789 Pine Rd, Chicago, IL 60601",
            "2221000010003695", "10/24", "789"
        ));

        customers.add(new Customer(
            "Emily", "Williams", "emily.williams@example.com", "+1-555-0104",
            "321 Elm St, Houston, TX 77001",
            "378282246310005", "09/25", "321"
        ));

        customers.add(new Customer(
            "David", "Brown", "david.brown@example.com", "+1-555-0105",
            "654 Maple Dr, Phoenix, AZ 85001",
            "371449635398431", "08/26", "654"
        ));

        customers.add(new Customer(
            "Sarah", "Davis", "sarah.davis@example.com", "+1-555-0106",
            "987 Cedar Ln, Philadelphia, PA 19101",
            "6011111111111117", "07/25", "987"
        ));

        customers.add(new Customer(
            "James", "Miller", "james.miller@example.com", "+1-555-0107",
            "147 Birch Ct, San Antonio, TX 78201",
            "3530111333300000", "06/24", "147"
        ));

        customers.add(new Customer(
            "Lisa", "Wilson", "lisa.wilson@example.com", "+1-555-0108",
            "258 Spruce Way, San Diego, CA 92101",
            "5555555555554444", "05/26", "258"
        ));

        customers.add(new Customer(
            "Robert", "Moore", "robert.moore@example.com", "+1-555-0109",
            "369 Walnut Blvd, Dallas, TX 75201",
            "4111111111111111", "04/25", "369"
        ));

        customers.add(new Customer(
            "Jennifer", "Taylor", "jennifer.taylor@example.com", "+1-555-0110",
            "753 Ash Ave, San Jose, CA 95101",
            "4012888888881881", "03/26", "753"
        ));

        for (Customer customer : customers) {
            customer.persist();
        }

        return customers;
    }

    private List<Hotel> createHotels() {
        List<Hotel> hotels = new ArrayList<>();

        // Hotel 1: Luxury Resort
        Hotel hotel1 = new Hotel(
            "Grand Pacific Resort",
            "100 Beachfront Drive",
            "Miami Beach",
            "USA",
            "A luxurious beachfront resort featuring world-class amenities, spa services, and fine dining.",
            5
        );
        hotel1.persist();
        createRoomsForHotel(hotel1, 20);
        hotels.add(hotel1);

        // Hotel 2: Business Hotel
        Hotel hotel2 = new Hotel(
            "Metropolitan Business Hotel",
            "250 Corporate Plaza",
            "New York",
            "USA",
            "Modern business hotel in the heart of Manhattan with state-of-the-art conference facilities.",
            4
        );
        hotel2.persist();
        createRoomsForHotel(hotel2, 25);
        hotels.add(hotel2);

        // Hotel 3: Boutique Hotel
        Hotel hotel3 = new Hotel(
            "The Vintage Inn",
            "75 Historic District",
            "Charleston",
            "USA",
            "Charming boutique hotel in a restored 19th-century building with unique character.",
            4
        );
        hotel3.persist();
        createRoomsForHotel(hotel3, 15);
        hotels.add(hotel3);

        // Hotel 4: Mountain Lodge
        Hotel hotel4 = new Hotel(
            "Alpine Mountain Lodge",
            "500 Summit Road",
            "Aspen",
            "USA",
            "Cozy mountain lodge offering breathtaking views and easy access to ski slopes.",
            4
        );
        hotel4.persist();
        createRoomsForHotel(hotel4, 18);
        hotels.add(hotel4);

        // Hotel 5: Airport Hotel
        Hotel hotel5 = new Hotel(
            "Sky Harbor Hotel",
            "1000 Airport Boulevard",
            "Los Angeles",
            "USA",
            "Convenient airport hotel with complimentary shuttle service and comfortable accommodations.",
            3
        );
        hotel5.persist();
        createRoomsForHotel(hotel5, 30);
        hotels.add(hotel5);

        return hotels;
    }

    private void createRoomsForHotel(Hotel hotel, int numberOfRooms) {
        String[] roomTypes = {"Standard", "Deluxe", "Suite", "Executive Suite"};
        BigDecimal[] basePrices = {
            new BigDecimal("120.00"),
            new BigDecimal("180.00"),
            new BigDecimal("250.00"),
            new BigDecimal("350.00")
        };
        Integer[] capacities = {2, 2, 4, 4};

        for (int i = 1; i <= numberOfRooms; i++) {
            int floor = (i - 1) / 10 + 1;
            int roomNum = ((i - 1) % 10) + 1;
            String roomNumber = String.format("%d%02d", floor, roomNum);
            
            int typeIndex = i % roomTypes.length;
            
            Room room = new Room(
                hotel,
                roomNumber,
                roomTypes[typeIndex],
                basePrices[typeIndex],
                capacities[typeIndex],
                String.format("%s room with modern amenities", roomTypes[typeIndex])
            );
            room.persist();
        }
    }

    private void createBookings(List<Hotel> hotels, List<Customer> customers) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusMonths(3);
        
        int totalRooms = hotels.stream()
            .mapToInt(hotel -> Room.list("hotel", hotel).size())
            .sum();
        
        // Target ~50% occupancy, so create bookings for about half the room-nights
        int targetBookings = (int) (totalRooms * 45 * 0.5 / 7); // Assuming average 7-day stays
        
        LOG.infof("Creating approximately %d bookings for %d total rooms", targetBookings, totalRooms);
        
        int bookingsCreated = 0;
        for (int i = 0; i < targetBookings; i++) {
            try {
                // Random hotel and room
                Hotel hotel = hotels.get(RANDOM.nextInt(hotels.size()));
                List<Room> hotelRooms = Room.list("hotel", hotel);
                if (hotelRooms.isEmpty()) continue;
                
                Room room = hotelRooms.get(RANDOM.nextInt(hotelRooms.size()));
                
                // Random customer
                Customer customer = customers.get(RANDOM.nextInt(customers.size()));
                
                // Random dates within next 3 months
                long daysUntilEnd = endDate.toEpochDay() - today.toEpochDay();
                int startOffset = RANDOM.nextInt((int) daysUntilEnd - 7);
                LocalDate checkIn = today.plusDays(startOffset);
                
                // Stay duration: 1-14 nights
                int stayDuration = 1 + RANDOM.nextInt(14);
                LocalDate checkOut = checkIn.plusDays(stayDuration);
                
                if (checkOut.isAfter(endDate)) {
                    checkOut = endDate;
                }
                
                // Check if room is available
                List<Booking> overlappingBookings = Booking.list(
                    "room.id = ?1 and status != ?2 and checkInDate < ?3 and checkOutDate > ?4",
                    room.id, Booking.BookingStatus.CANCELLED, checkOut, checkIn
                );
                
                if (!overlappingBookings.isEmpty()) {
                    continue; // Skip if room is already booked
                }
                
                // Calculate total price
                long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
                BigDecimal totalPrice = room.pricePerNight.multiply(BigDecimal.valueOf(nights));
                
                // Random number of guests (1 to room capacity)
                int numberOfGuests = 1 + RANDOM.nextInt(room.capacity);
                
                // Random booking status (mostly confirmed)
                Booking.BookingStatus status = RANDOM.nextDouble() < 0.9 
                    ? Booking.BookingStatus.CONFIRMED 
                    : Booking.BookingStatus.PENDING;
                
                String[] specialRequests = {
                    null,
                    "Late check-in please",
                    "High floor preferred",
                    "Non-smoking room",
                    "Extra towels needed",
                    "Quiet room please"
                };
                String specialRequest = specialRequests[RANDOM.nextInt(specialRequests.length)];
                
                Booking booking = new Booking(
                    room, customer, checkIn, checkOut, numberOfGuests,
                    totalPrice, status, specialRequest
                );
                booking.persist();
                bookingsCreated++;
                
            } catch (Exception e) {
                LOG.warnf("Failed to create booking: %s", e.getMessage());
            }
        }
        
        LOG.infof("Successfully created %d bookings", bookingsCreated);
    }
}
