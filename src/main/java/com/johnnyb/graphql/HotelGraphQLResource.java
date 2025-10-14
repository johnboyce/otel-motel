package com.johnnyb.graphql;

import com.johnnyb.model.Hotel;
import com.johnnyb.model.Room;
import com.johnnyb.model.Booking;
import com.johnnyb.model.Customer;
import com.johnnyb.service.HotelService;
import com.johnnyb.service.RoomService;
import com.johnnyb.service.BookingService;
import com.johnnyb.service.CustomerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GraphQLApi
@ApplicationScoped
public class HotelGraphQLResource {

    private static final Logger LOG = Logger.getLogger(HotelGraphQLResource.class);

    @Inject
    HotelService hotelService;

    @Inject
    RoomService roomService;

    @Inject
    BookingService bookingService;

    @Inject
    CustomerService customerService;

    @Query("hotels")
    @Description("Get all hotels")
    public List<Hotel> getAllHotels() {
        LOG.info("Fetching all hotels");
        return hotelService.findAll();
    }

    @Query("hotel")
    @Description("Get a hotel by ID")
    public Hotel getHotel(String id) {
        LOG.infof("Fetching hotel with ID: %s", id);
        return hotelService.findById(id).orElse(null);
    }

    @Query("hotelsByCity")
    @Description("Get hotels in a specific city")
    public List<Hotel> getHotelsByCity(String city) {
        LOG.infof("Fetching hotels in city: %s", city);
        return hotelService.findByCity(city);
    }

    @Query("hotelsByCountry")
    @Description("Get hotels in a specific country")
    public List<Hotel> getHotelsByCountry(String country) {
        LOG.infof("Fetching hotels in country: %s", country);
        return hotelService.findByCountry(country);
    }

    @Query("room")
    @Description("Get a room by ID")
    public Room getRoom(String id) {
        LOG.infof("Fetching room with ID: %s", id);
        return roomService.findById(id).orElse(null);
    }

    @Query("roomsByHotel")
    @Description("Get all rooms for a specific hotel")
    public List<Room> getRoomsByHotel(String hotelId) {
        LOG.infof("Fetching rooms for hotel ID: %s", hotelId);
        return roomService.findByHotelId(hotelId);
    }

    @Query("availableRooms")
    @Description("Get available rooms for a hotel and date range")
    public List<Room> getAvailableRooms(String hotelId, LocalDate checkIn, LocalDate checkOut) {
        LOG.infof("Checking availability for hotel %s from %s to %s", hotelId, checkIn, checkOut);
        
        // Find all rooms for the hotel
        List<Room> allRooms = roomService.findByHotelId(hotelId);
        
        // Filter out rooms that have bookings overlapping with the requested dates
        return allRooms.stream()
            .filter(room -> {
                List<Booking> overlappingBookings = bookingService.findOverlappingBookings(
                    room.getId(), checkIn, checkOut
                );
                return overlappingBookings.isEmpty();
            })
            .collect(Collectors.toList());
    }

    @Query("booking")
    @Description("Get a booking by ID")
    public Booking getBooking(String id) {
        LOG.infof("Fetching booking with ID: %s", id);
        return bookingService.findById(id).orElse(null);
    }

    @Query("bookingsByCustomer")
    @Description("Get all bookings for a customer")
    public List<Booking> getBookingsByCustomer(String customerId) {
        LOG.infof("Fetching bookings for customer ID: %s", customerId);
        return bookingService.findByCustomerId(customerId);
    }

    @Query("upcomingBookings")
    @Description("Get all upcoming bookings")
    public List<Booking> getUpcomingBookings() {
        LOG.info("Fetching upcoming bookings");
        return bookingService.findUpcomingBookings();
    }

    @Query("customer")
    @Description("Get a customer by ID")
    public Customer getCustomer(String id) {
        LOG.infof("Fetching customer with ID: %s", id);
        return customerService.findById(id).orElse(null);
    }

    @Query("customerByEmail")
    @Description("Get a customer by email")
    public Customer getCustomerByEmail(String email) {
        LOG.infof("Fetching customer with email: %s", email);
        return customerService.findByEmail(email).orElse(null);
    }

    @Mutation("createBooking")
    @Description("Create a new booking")
    public Booking createBooking(String roomId, String customerId, LocalDate checkInDate, LocalDate checkOutDate,
                                 Integer numberOfGuests, String specialRequests) {
        LOG.infof("Creating booking for room %s, customer %s", roomId, customerId);
        
        Room room = roomService.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        Customer customer = customerService.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Check if room is available
        List<Booking> overlappingBookings = bookingService.findOverlappingBookings(
            roomId, checkInDate, checkOutDate
        );
        
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("Room is not available for the selected dates");
        }
        
        // Calculate total price
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        
        Booking booking = Booking.builder()
            .id(UUID.randomUUID().toString())
            .roomId(roomId)
            .customerId(customerId)
            .checkInDate(checkInDate)
            .checkOutDate(checkOutDate)
            .numberOfGuests(numberOfGuests)
            .totalPrice(totalPrice)
            .status(Booking.BookingStatus.CONFIRMED)
            .specialRequests(specialRequests)
            .build();
        
        bookingService.save(booking);
        
        LOG.infof("Booking created with ID: %s", booking.getId());
        return booking;
    }

    @Mutation("cancelBooking")
    @Description("Cancel an existing booking")
    public Booking cancelBooking(String bookingId) {
        LOG.infof("Cancelling booking with ID: %s", bookingId);
        
        Booking booking = bookingService.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingService.save(booking);
        
        LOG.infof("Booking %s cancelled successfully", bookingId);
        return booking;
    }
}
