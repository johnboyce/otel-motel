package com.johnnyb.service;

import com.johnnyb.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerService {

    private static final Logger LOG = Logger.getLogger(CustomerService.class);
    private static final String TABLE_NAME = "customers";

    @Inject
    DynamoDbEnhancedClient dynamoDb;

    private DynamoDbTable<Customer> customerTable;

    @PostConstruct
    void init() {
        customerTable = dynamoDb.table(TABLE_NAME, TableSchema.fromBean(Customer.class));
    }

    public Customer save(Customer customer) {
        LOG.infof("Saving customer: %s", customer.getId());
        customerTable.putItem(customer);
        return customer;
    }

    public Optional<Customer> findById(String id) {
        LOG.infof("Finding customer by ID: %s", id);
        try {
            Customer customer = customerTable.getItem(Key.builder().partitionValue(id).build());
            return Optional.ofNullable(customer);
        } catch (ResourceNotFoundException e) {
            LOG.warnf("Customer not found: %s", id);
            return Optional.empty();
        }
    }

    public Optional<Customer> findByEmail(String email) {
        LOG.infof("Finding customer by email: %s", email);
        List<Customer> customers = findAll();
        return customers.stream()
                .filter(c -> email.equals(c.getEmail()))
                .findFirst();
    }

    public List<Customer> findAll() {
        LOG.info("Finding all customers");
        List<Customer> customers = new ArrayList<>();
        try {
            PageIterable<Customer> pages = customerTable.scan();
            pages.items().forEach(customers::add);
        } catch (ResourceNotFoundException e) {
            LOG.warn("Customer table not found");
        }
        return customers;
    }

    public void delete(String id) {
        LOG.infof("Deleting customer: %s", id);
        customerTable.deleteItem(Key.builder().partitionValue(id).build());
    }

    public long count() {
        return findAll().size();
    }
}
