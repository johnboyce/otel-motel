package com.johnnyb.service;

import com.johnnyb.model.Customer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerServiceTest {

    @Inject
    ICustomerService customerService;

    @Test
    void testSaveAndFindById() {
        var id = UUID.randomUUID().toString();
        var customer = Customer.builder()
            .id(id)
            .firstName("Test")
            .lastName("User")
            .email("test.user@example.com")
            .phone("+1-555-0199")
            .address("999 Test St")
            .creditCardNumber("4111111111111111")
            .creditCardExpiry("12/28")
            .creditCardCvv("999")
            .bookingIds(new ArrayList<>())
            .build();

        customerService.save(customer);

        var found = customerService.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getFirstName());
        assertEquals("User", found.get().getLastName());
        assertEquals("test.user@example.com", found.get().getEmail());
        
        // Cleanup
        customerService.delete(id);
    }

    @Test
    void testFindByEmail() {
        var id = UUID.randomUUID().toString();
        var email = "test.email." + id + "@example.com";
        var customer = Customer.builder()
            .id(id)
            .firstName("Email")
            .lastName("Test")
            .email(email)
            .phone("+1-555-0198")
            .address("888 Test St")
            .creditCardNumber("4111111111111111")
            .creditCardExpiry("12/28")
            .creditCardCvv("888")
            .bookingIds(new ArrayList<>())
            .build();

        customerService.save(customer);

        var found = customerService.findByEmail(email);
        assertTrue(found.isPresent());
        assertEquals("Email", found.get().getFirstName());
        assertEquals(email, found.get().getEmail());
        
        // Cleanup
        customerService.delete(id);
    }

    @Test
    void testFindAll() {
        var customers = customerService.findAll();
        assertNotNull(customers);
        // Note: This will include sample data from initialization
    }

    @Test
    void testDelete() {
        var id = UUID.randomUUID().toString();
        var customer = Customer.builder()
            .id(id)
            .firstName("Delete")
            .lastName("Test")
            .email("delete.test@example.com")
            .phone("+1-555-0197")
            .address("777 Test St")
            .creditCardNumber("4111111111111111")
            .creditCardExpiry("12/28")
            .creditCardCvv("777")
            .bookingIds(new ArrayList<>())
            .build();

        customerService.save(customer);
        assertTrue(customerService.findById(id).isPresent());

        customerService.delete(id);
        assertFalse(customerService.findById(id).isPresent());
    }
}
