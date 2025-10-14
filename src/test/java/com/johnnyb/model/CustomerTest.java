package com.johnnyb.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void testCustomerBuilder() {
        String id = UUID.randomUUID().toString();
        Customer customer = Customer.builder()
            .id(id)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("+1-555-0101")
            .address("123 Main St")
            .creditCardNumber("4532015112830366")
            .creditCardExpiry("12/25")
            .creditCardCvv("123")
            .bookingIds(new ArrayList<>())
            .build();

        assertNotNull(customer);
        assertEquals(id, customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("+1-555-0101", customer.getPhone());
        assertEquals("123 Main St", customer.getAddress());
        assertEquals("4532015112830366", customer.getCreditCardNumber());
        assertEquals("12/25", customer.getCreditCardExpiry());
        assertEquals("123", customer.getCreditCardCvv());
        assertNotNull(customer.getBookingIds());
        assertTrue(customer.getBookingIds().isEmpty());
    }

    @Test
    void testCustomerSetters() {
        Customer customer = new Customer();
        String id = UUID.randomUUID().toString();
        
        customer.setId(id);
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setEmail("jane.smith@example.com");
        customer.setPhone("+1-555-0102");
        customer.setAddress("456 Oak Ave");
        customer.setCreditCardNumber("5425233430109903");
        customer.setCreditCardExpiry("11/26");
        customer.setCreditCardCvv("456");
        customer.setBookingIds(new ArrayList<>());

        assertEquals(id, customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("jane.smith@example.com", customer.getEmail());
    }

    @Test
    void testCustomerEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        Customer customer1 = Customer.builder()
            .id(id)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        Customer customer2 = Customer.builder()
            .id(id)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();

        assertEquals(customer1, customer2);
        assertEquals(customer1.hashCode(), customer2.hashCode());
    }
}
