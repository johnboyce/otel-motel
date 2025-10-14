package com.johnnyb.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer extends PanacheEntity {

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false)
    public String phone;

    public String address;

    @Column(nullable = false)
    public String creditCardNumber;

    @Column(nullable = false)
    public String creditCardExpiry;

    @Column(nullable = false)
    public String creditCardCvv;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Booking> bookings;

    public Customer() {
    }

    public Customer(String firstName, String lastName, String email, String phone, String address,
                    String creditCardNumber, String creditCardExpiry, String creditCardCvv) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.creditCardCvv = creditCardCvv;
    }
}
