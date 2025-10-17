package com.johnnyb.service;

import com.johnnyb.model.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IBookingService {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findAll();
    List<Booking> findByCustomerId(String customerId);
    List<Booking> findByRoomId(String roomId);
    List<Booking> findUpcomingBookings();
    List<Booking> findOverlappingBookings(String roomId, LocalDate checkIn, LocalDate checkOut);
    void delete(String id);
    long count();
}
