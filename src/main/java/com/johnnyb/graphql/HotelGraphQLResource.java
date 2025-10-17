package com.johnnyb.graphql;

import com.johnnyb.model.Hotel;
import com.johnnyb.model.Room;
import com.johnnyb.model.Booking;
import com.johnnyb.model.Customer;
import com.johnnyb.service.IHotelService;
import com.johnnyb.service.IRoomService;
import com.johnnyb.service.IBookingService;
import com.johnnyb.service.ICustomerService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
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

@GraphQLApi
@ApplicationScoped
public class HotelGraphQLResource {

    private static final Logger LOG = Logger.getLogger(HotelGraphQLResource.class);

    @Inject
    IHotelService hotelService;

    @Inject
    IRoomService roomService;

    @Inject
    IBookingService bookingService;

    @Inject
    ICustomerService customerService;

    @Query("hotels")
    @Description("Get all hotels")
    @PermitAll
    public List<Hotel> getAllHotels() {
        LOG.info("Fetching all hotels");
        return hotelService.findAll();
    }

    @Query("hotel")
    @Description("Get a hotel by ID")
    @PermitAll
    public Hotel getHotel(String id) {
        LOG.infof("Fetching hotel with ID: %s", id);
        return hotelService.findById(id).orElse(null);
    }

    @Query("hotelsByCity")
    @Description("Get hotels in a specific city")
    @PermitAll
    public List<Hotel> getHotelsByCity(String city) {
        LOG.infof("Fetching hotels in city: %s", city);
        return hotelService.findByCity(city);
    }

    @Query("hotelsByCountry")
    @Description("Get hotels in a specific country")
    @PermitAll
    public List<Hotel> getHotelsByCountry(String country) {
        LOG.infof("Fetching hotels in country: %s", country);
        return hotelService.findByCountry(country);
    }

    @Query("room")
    @Description("Get a room by ID")
    @PermitAll
    public Room getRoom(String id) {
        LOG.infof("Fetching room with ID: %s", id);
        return roomService.findById(id).orElse(null);
    }

    @Query("roomsByHotel")
    @Description("Get all rooms for a specific hotel")
    @PermitAll
    public List<Room> getRoomsByHotel(String hotelId) {
        LOG.infof("Fetching rooms for hotel ID: %s", hotelId);
        return roomService.findByHotelId(hotelId);
    }

    @Query("availableRooms")
    @Description("Get available rooms for a hotel and date range")
    @PermitAll
    public List<Room> getAvailableRooms(String hotelId, LocalDate checkIn, LocalDate checkOut) {
        LOG.infof("Checking availability for hotel %s from %s to %s", hotelId, checkIn, checkOut);
        
        // Find all rooms for the hotel
        var allRooms = roomService.findByHotelId(hotelId);
        
        // Filter out rooms that have bookings overlapping with the requested dates
        return allRooms.stream()
            .filter(room -> {
                var overlappingBookings = bookingService.findOverlappingBookings(
                    room.getId(), checkIn, checkOut
                );
                return overlappingBookings.isEmpty();
            })
            .toList();
    }

    @Query("booking")
    @Description("Get a booking by ID")
    @RolesAllowed({"user", "admin"})
    public Booking getBooking(String id) {
        LOG.infof("Fetching booking with ID: %s", id);
        return bookingService.findById(id).orElse(null);
    }

    @Query("bookingsByCustomer")
    @Description("Get all bookings for a customer")
    @RolesAllowed({"user", "admin"})
    public List<Booking> getBookingsByCustomer(String customerId) {
        LOG.infof("Fetching bookings for customer ID: %s", customerId);
        return bookingService.findByCustomerId(customerId);
    }

    @Query("upcomingBookings")
    @Description("Get all upcoming bookings")
    @RolesAllowed("admin")
    public List<Booking> getUpcomingBookings() {
        LOG.info("Fetching upcoming bookings");
        return bookingService.findUpcomingBookings();
    }

    @Query("customer")
    @Description("Get a customer by ID")
    @RolesAllowed({"user", "admin"})
    public Customer getCustomer(String id) {
        LOG.infof("Fetching customer with ID: %s", id);
        return customerService.findById(id).orElse(null);
    }

    @Query("customerByEmail")
    @Description("Get a customer by email")
    @RolesAllowed({"user", "admin"})
    public Customer getCustomerByEmail(String email) {
        LOG.infof("Fetching customer with email: %s", email);
        return customerService.findByEmail(email).orElse(null);
    }

    @Mutation("createBooking")
    @Description("Create a new booking")
    @RolesAllowed({"user", "admin"})
    public Booking createBooking(String roomId, String customerId, LocalDate checkInDate, LocalDate checkOutDate,
                                 Integer numberOfGuests, String specialRequests) {
        LOG.infof("Creating booking for room %s, customer %s", roomId, customerId);
        
        var room = roomService.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));
        
        var customer = customerService.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        
        // Check if room is available
        var overlappingBookings = bookingService.findOverlappingBookings(
            roomId, checkInDate, checkOutDate
        );
        
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("Room is not available for the selected dates");
        }
        
        // Calculate total price
        var nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        var totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        
        var booking = Booking.builder()
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
    @RolesAllowed({"user", "admin"})
    public Booking cancelBooking(String bookingId) {
        LOG.infof("Cancelling booking with ID: %s", bookingId);
        
        var booking = bookingService.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingService.save(booking);
        
        LOG.infof("Booking %s cancelled successfully", bookingId);
        return booking;
    }
}
