package com.example.service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public String getCustomerNameById(String id) {
        Customer c = repository.findById(id);
        if (c == null)
            throw new RuntimeException("Customer not found");
        return c.getName();
    }
}
