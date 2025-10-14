package com.johnnyb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    public Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;

    @Column(nullable = false)
    public LocalDate checkInDate;

    @Column(nullable = false)
    public LocalDate checkOutDate;

    @Column(nullable = false)
    public Integer numberOfGuests;

    @Column(nullable = false)
    public BigDecimal totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public BookingStatus status;

    public String specialRequests;

    public Booking() {
    }

    public Booking(Room room, Customer customer, LocalDate checkInDate, LocalDate checkOutDate,
                   Integer numberOfGuests, BigDecimal totalPrice, BookingStatus status, String specialRequests) {
        this.room = room;
        this.customer = customer;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = totalPrice;
        this.status = status;
        this.specialRequests = specialRequests;
    }

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}
