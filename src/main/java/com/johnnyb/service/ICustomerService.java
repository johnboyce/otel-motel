package com.johnnyb.service;

import com.johnnyb.model.Customer;

import java.util.List;
import java.util.Optional;

public interface ICustomerService {
    Customer save(Customer customer);
    Optional<Customer> findById(String id);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    void delete(String id);
    long count();
}
