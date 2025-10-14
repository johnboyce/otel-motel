package com.johnnyb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "hotels")
public class Hotel extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String address;

    @Column(nullable = false)
    public String city;

    @Column(nullable = false)
    public String country;

    @Column(length = 1000)
    public String description;

    @Column(nullable = false)
    public Integer starRating;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Room> rooms;

    public Hotel() {
    }

    public Hotel(String name, String address, String city, String country, String description, Integer starRating) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.description = description;
        this.starRating = starRating;
    }
}
