package com.johnnyb.service;

import com.johnnyb.model.Booking;
import com.johnnyb.model.Customer;
import com.johnnyb.model.Hotel;
import com.johnnyb.model.Room;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class DataInitializationService implements IDataInitializationService {

    private static final Logger LOG = Logger.getLogger(DataInitializationService.class);
    private static final Random RANDOM = new Random();

    @Inject
    IHotelService hotelService;

    @Inject
    IRoomService roomService;

    @Inject
    ICustomerService customerService;

    @Inject
    IBookingService bookingService;

    @Override
    public void onStart(@Observes StartupEvent ev) {
        // Only initialize if database is empty
        if (hotelService.count() > 0) {
            LOG.info("Database already initialized, skipping data initialization");
            return;
        }

        LOG.info("Initializing database with sample data...");

        // Create customers first
        var customers = createCustomers();
        LOG.infof("Created %d customers", customers.size());

        // Create hotels with rooms
        var hotels = createHotels();
        LOG.infof("Created %d hotels", hotels.size());

        // Create bookings (targeting ~50% capacity for next 3 months)
        createBookings(hotels, customers);
        LOG.info("Database initialization completed");
    }

    private List<Customer> createCustomers() {
        var customers = new ArrayList<Customer>();

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("+1-555-0101")
            .address("123 Main St, New York, NY 10001")
            .creditCardNumber("4532015112830366")
            .creditCardExpiry("12/25")
            .creditCardCvv("123")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Jane")
            .lastName("Smith")
            .email("jane.smith@example.com")
            .phone("+1-555-0102")
            .address("456 Oak Ave, Los Angeles, CA 90001")
            .creditCardNumber("5425233430109903")
            .creditCardExpiry("11/26")
            .creditCardCvv("456")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Michael")
            .lastName("Johnson")
            .email("michael.johnson@example.com")
            .phone("+1-555-0103")
            .address("789 Pine Rd, Chicago, IL 60601")
            .creditCardNumber("2221000010003695")
            .creditCardExpiry("10/24")
            .creditCardCvv("789")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Emily")
            .lastName("Williams")
            .email("emily.williams@example.com")
            .phone("+1-555-0104")
            .address("321 Elm St, Houston, TX 77001")
            .creditCardNumber("378282246310005")
            .creditCardExpiry("09/25")
            .creditCardCvv("321")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("David")
            .lastName("Brown")
            .email("david.brown@example.com")
            .phone("+1-555-0105")
            .address("654 Maple Dr, Phoenix, AZ 85001")
            .creditCardNumber("371449635398431")
            .creditCardExpiry("08/26")
            .creditCardCvv("654")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Sarah")
            .lastName("Davis")
            .email("sarah.davis@example.com")
            .phone("+1-555-0106")
            .address("987 Cedar Ln, Philadelphia, PA 19101")
            .creditCardNumber("6011111111111117")
            .creditCardExpiry("07/25")
            .creditCardCvv("987")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("James")
            .lastName("Miller")
            .email("james.miller@example.com")
            .phone("+1-555-0107")
            .address("147 Birch Ct, San Antonio, TX 78201")
            .creditCardNumber("3530111333300000")
            .creditCardExpiry("06/24")
            .creditCardCvv("147")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Lisa")
            .lastName("Wilson")
            .email("lisa.wilson@example.com")
            .phone("+1-555-0108")
            .address("258 Spruce Way, San Diego, CA 92101")
            .creditCardNumber("5555555555554444")
            .creditCardExpiry("05/26")
            .creditCardCvv("258")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Robert")
            .lastName("Moore")
            .email("robert.moore@example.com")
            .phone("+1-555-0109")
            .address("369 Walnut Blvd, Dallas, TX 75201")
            .creditCardNumber("4111111111111111")
            .creditCardExpiry("04/25")
            .creditCardCvv("369")
            .bookingIds(new ArrayList<>())
            .build());

        customers.add(Customer.builder()
            .id(UUID.randomUUID().toString())
            .firstName("Jennifer")
            .lastName("Taylor")
            .email("jennifer.taylor@example.com")
            .phone("+1-555-0110")
            .address("753 Ash Ave, San Jose, CA 95101")
            .creditCardNumber("4012888888881881")
            .creditCardExpiry("03/26")
            .creditCardCvv("753")
            .bookingIds(new ArrayList<>())
            .build());

        for (Customer customer : customers) {
            customerService.save(customer);
        }

        return customers;
    }

    private List<Hotel> createHotels() {
        var hotels = new ArrayList<Hotel>();

        // Hotel 1: Luxury Resort
        Hotel hotel1 = Hotel.builder()
            .id(UUID.randomUUID().toString())
            .name("Grand Pacific Resort")
            .address("100 Beachfront Drive")
            .city("Miami Beach")
            .country("USA")
            .description("A luxurious beachfront resort featuring world-class amenities, spa services, and fine dining.")
            .starRating(5)
            .roomIds(new ArrayList<>())
            .build();
        hotelService.save(hotel1);
        createRoomsForHotel(hotel1, 20);
        hotels.add(hotel1);

        // Hotel 2: Business Hotel
        Hotel hotel2 = Hotel.builder()
            .id(UUID.randomUUID().toString())
            .name("Metropolitan Business Hotel")
            .address("250 Corporate Plaza")
            .city("New York")
            .country("USA")
            .description("Modern business hotel in the heart of Manhattan with state-of-the-art conference facilities.")
            .starRating(4)
            .roomIds(new ArrayList<>())
            .build();
        hotelService.save(hotel2);
        createRoomsForHotel(hotel2, 25);
        hotels.add(hotel2);

        // Hotel 3: Boutique Hotel
        Hotel hotel3 = Hotel.builder()
            .id(UUID.randomUUID().toString())
            .name("The Vintage Inn")
            .address("75 Historic District")
            .city("Charleston")
            .country("USA")
            .description("Charming boutique hotel in a restored 19th-century building with unique character.")
            .starRating(4)
            .roomIds(new ArrayList<>())
            .build();
        hotelService.save(hotel3);
        createRoomsForHotel(hotel3, 15);
        hotels.add(hotel3);

        // Hotel 4: Mountain Lodge
        Hotel hotel4 = Hotel.builder()
            .id(UUID.randomUUID().toString())
            .name("Alpine Mountain Lodge")
            .address("500 Summit Road")
            .city("Aspen")
            .country("USA")
            .description("Cozy mountain lodge offering breathtaking views and easy access to ski slopes.")
            .starRating(4)
            .roomIds(new ArrayList<>())
            .build();
        hotelService.save(hotel4);
        createRoomsForHotel(hotel4, 18);
        hotels.add(hotel4);

        // Hotel 5: Airport Hotel
        Hotel hotel5 = Hotel.builder()
            .id(UUID.randomUUID().toString())
            .name("Sky Harbor Hotel")
            .address("1000 Airport Boulevard")
            .city("Los Angeles")
            .country("USA")
            .description("Convenient airport hotel with complimentary shuttle service and comfortable accommodations.")
            .starRating(3)
            .roomIds(new ArrayList<>())
            .build();
        hotelService.save(hotel5);
        createRoomsForHotel(hotel5, 30);
        hotels.add(hotel5);

        return hotels;
    }

    private void createRoomsForHotel(Hotel hotel, int numberOfRooms) {
        var roomTypes = new String[]{"Standard", "Deluxe", "Suite", "Executive Suite"};
        var basePrices = new BigDecimal[]{
            new BigDecimal("120.00"),
            new BigDecimal("180.00"),
            new BigDecimal("250.00"),
            new BigDecimal("350.00")
        };
        var capacities = new Integer[]{2, 2, 4, 4};

        for (int i = 1; i <= numberOfRooms; i++) {
            var floor = (i - 1) / 10 + 1;
            var roomNum = ((i - 1) % 10) + 1;
            var roomNumber = String.format("%d%02d", floor, roomNum);
            
            var typeIndex = i % roomTypes.length;
            
            var room = Room.builder()
                .id(UUID.randomUUID().toString())
                .hotelId(hotel.getId())
                .roomNumber(roomNumber)
                .roomType(roomTypes[typeIndex])
                .pricePerNight(basePrices[typeIndex])
                .capacity(capacities[typeIndex])
                .description(String.format("%s room with modern amenities", roomTypes[typeIndex]))
                .bookingIds(new ArrayList<>())
                .build();
            roomService.save(room);
        }
    }

    private void createBookings(List<Hotel> hotels, List<Customer> customers) {
        var today = LocalDate.now();
        var endDate = today.plusMonths(3);
        
        var totalRooms = (int) hotels.stream()
            .mapToLong(hotel -> roomService.findByHotelId(hotel.getId()).size())
            .sum();
        
        // Target ~50% occupancy, so create bookings for about half the room-nights
        var targetBookings = (int) (totalRooms * 45 * 0.5 / 7); // Assuming average 7-day stays
        
        LOG.infof("Creating approximately %d bookings for %d total rooms", targetBookings, totalRooms);
        
        var bookingsCreated = 0;
        for (int i = 0; i < targetBookings; i++) {
            try {
                // Random hotel and room
                var hotel = hotels.get(RANDOM.nextInt(hotels.size()));
                var hotelRooms = roomService.findByHotelId(hotel.getId());
                if (hotelRooms.isEmpty()) continue;
                
                var room = hotelRooms.get(RANDOM.nextInt(hotelRooms.size()));
                
                // Random customer
                var customer = customers.get(RANDOM.nextInt(customers.size()));
                
                // Random dates within next 3 months
                var daysUntilEnd = endDate.toEpochDay() - today.toEpochDay();
                var startOffset = RANDOM.nextInt((int) daysUntilEnd - 7);
                var checkIn = today.plusDays(startOffset);
                
                // Stay duration: 1-14 nights
                var stayDuration = 1 + RANDOM.nextInt(14);
                var checkOut = checkIn.plusDays(stayDuration);
                
                if (checkOut.isAfter(endDate)) {
                    checkOut = endDate;
                }
                
                // Check if room is available
                var overlappingBookings = bookingService.findOverlappingBookings(
                    room.getId(), checkIn, checkOut
                );
                
                if (!overlappingBookings.isEmpty()) {
                    continue; // Skip if room is already booked
                }
                
                // Calculate total price
                var nights = checkOut.toEpochDay() - checkIn.toEpochDay();
                var totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
                
                // Random number of guests (1 to room capacity)
                var numberOfGuests = 1 + RANDOM.nextInt(room.getCapacity());
                
                // Random booking status (mostly confirmed)
                var status = RANDOM.nextDouble() < 0.9 
                    ? Booking.BookingStatus.CONFIRMED 
                    : Booking.BookingStatus.PENDING;
                
                var specialRequests = new String[]{
                    null,
                    "Late check-in please",
                    "High floor preferred",
                    "Non-smoking room",
                    "Extra towels needed",
                    "Quiet room please"
                };
                var specialRequest = specialRequests[RANDOM.nextInt(specialRequests.length)];
                
                var booking = Booking.builder()
                    .id(UUID.randomUUID().toString())
                    .roomId(room.getId())
                    .customerId(customer.getId())
                    .checkInDate(checkIn)
                    .checkOutDate(checkOut)
                    .numberOfGuests(numberOfGuests)
                    .totalPrice(totalPrice)
                    .status(status)
                    .specialRequests(specialRequest)
                    .build();
                bookingService.save(booking);
                bookingsCreated++;
                
            } catch (Exception e) {
                LOG.warnf("Failed to create booking: %s", e.getMessage());
            }
        }
        
        LOG.infof("Successfully created %d bookings", bookingsCreated);
    }
}
