package com.example.repository;

import com.example.model.Customer;

public interface CustomerRepository {
    Customer findById(String id);

    Customer findByName(String name);

    void save(Customer customer);
}
