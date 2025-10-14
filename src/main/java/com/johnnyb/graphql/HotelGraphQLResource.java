package com.johnnyb.graphql;

import com.johnnyb.entity.Hotel;
import com.johnnyb.entity.Room;
import com.johnnyb.entity.Booking;
import com.johnnyb.entity.Customer;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@GraphQLApi
@ApplicationScoped
public class HotelGraphQLResource {

    private static final Logger LOG = Logger.getLogger(HotelGraphQLResource.class);

    @Query("hotels")
    @Description("Get all hotels")
    public List<Hotel> getAllHotels() {
        LOG.info("Fetching all hotels");
        return Hotel.listAll();
    }

    @Query("hotel")
    @Description("Get a hotel by ID")
    public Hotel getHotel(Long id) {
        LOG.infof("Fetching hotel with ID: %d", id);
        return Hotel.findById(id);
    }

    @Query("hotelsByCity")
    @Description("Get hotels in a specific city")
    public List<Hotel> getHotelsByCity(String city) {
        LOG.infof("Fetching hotels in city: %s", city);
        return Hotel.list("city", city);
    }

    @Query("hotelsByCountry")
    @Description("Get hotels in a specific country")
    public List<Hotel> getHotelsByCountry(String country) {
        LOG.infof("Fetching hotels in country: %s", country);
        return Hotel.list("country", country);
    }

    @Query("room")
    @Description("Get a room by ID")
    public Room getRoom(Long id) {
        LOG.infof("Fetching room with ID: %d", id);
        return Room.findById(id);
    }

    @Query("roomsByHotel")
    @Description("Get all rooms for a specific hotel")
    public List<Room> getRoomsByHotel(Long hotelId) {
        LOG.infof("Fetching rooms for hotel ID: %d", hotelId);
        return Room.list("hotel.id", hotelId);
    }

    @Query("availableRooms")
    @Description("Get available rooms for a hotel and date range")
    public List<Room> getAvailableRooms(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        LOG.infof("Checking availability for hotel %d from %s to %s", hotelId, checkIn, checkOut);
        
        // Find all rooms for the hotel
        List<Room> allRooms = Room.list("hotel.id", hotelId);
        
        // Filter out rooms that have bookings overlapping with the requested dates
        return allRooms.stream()
            .filter(room -> {
                List<Booking> overlappingBookings = Booking.list(
                    "room.id = ?1 and status != ?2 and checkInDate < ?3 and checkOutDate > ?4",
                    room.id, Booking.BookingStatus.CANCELLED, checkOut, checkIn
                );
                return overlappingBookings.isEmpty();
            })
            .toList();
    }

    @Query("booking")
    @Description("Get a booking by ID")
    public Booking getBooking(Long id) {
        LOG.infof("Fetching booking with ID: %d", id);
        return Booking.findById(id);
    }

    @Query("bookingsByCustomer")
    @Description("Get all bookings for a customer")
    public List<Booking> getBookingsByCustomer(Long customerId) {
        LOG.infof("Fetching bookings for customer ID: %d", customerId);
        return Booking.list("customer.id", customerId);
    }

    @Query("upcomingBookings")
    @Description("Get all upcoming bookings")
    public List<Booking> getUpcomingBookings() {
        LOG.info("Fetching upcoming bookings");
        LocalDate today = LocalDate.now();
        return Booking.list("checkInDate >= ?1 and status != ?2", today, Booking.BookingStatus.CANCELLED);
    }

    @Query("customer")
    @Description("Get a customer by ID")
    public Customer getCustomer(Long id) {
        LOG.infof("Fetching customer with ID: %d", id);
        return Customer.findById(id);
    }

    @Query("customerByEmail")
    @Description("Get a customer by email")
    public Customer getCustomerByEmail(String email) {
        LOG.infof("Fetching customer with email: %s", email);
        return Customer.find("email", email).firstResult();
    }

    @Mutation("createBooking")
    @Description("Create a new booking")
    @Transactional
    public Booking createBooking(Long roomId, Long customerId, LocalDate checkInDate, LocalDate checkOutDate,
                                 Integer numberOfGuests, String specialRequests) {
        LOG.infof("Creating booking for room %d, customer %d", roomId, customerId);
        
        Room room = Room.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found");
        }
        
        Customer customer = Customer.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        // Check if room is available
        List<Booking> overlappingBookings = Booking.list(
            "room.id = ?1 and status != ?2 and checkInDate < ?3 and checkOutDate > ?4",
            roomId, Booking.BookingStatus.CANCELLED, checkOutDate, checkInDate
        );
        
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalArgumentException("Room is not available for the selected dates");
        }
        
        // Calculate total price
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        BigDecimal totalPrice = room.pricePerNight.multiply(BigDecimal.valueOf(nights));
        
        Booking booking = new Booking(room, customer, checkInDate, checkOutDate, numberOfGuests, 
                                     totalPrice, Booking.BookingStatus.CONFIRMED, specialRequests);
        booking.persist();
        
        LOG.infof("Booking created with ID: %d", booking.id);
        return booking;
    }

    @Mutation("cancelBooking")
    @Description("Cancel an existing booking")
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        LOG.infof("Cancelling booking with ID: %d", bookingId);
        
        Booking booking = Booking.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        
        booking.status = Booking.BookingStatus.CANCELLED;
        booking.persist();
        
        LOG.infof("Booking %d cancelled successfully", bookingId);
        return booking;
    }
}
