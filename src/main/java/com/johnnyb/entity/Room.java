package com.johnnyb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    public Hotel hotel;

    @Column(nullable = false)
    public String roomNumber;

    @Column(nullable = false)
    public String roomType;

    @Column(nullable = false)
    public BigDecimal pricePerNight;

    @Column(nullable = false)
    public Integer capacity;

    public String description;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Booking> bookings;

    public Room() {
    }

    public Room(Hotel hotel, String roomNumber, String roomType, BigDecimal pricePerNight, Integer capacity, String description) {
        this.hotel = hotel;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.description = description;
    }
}
