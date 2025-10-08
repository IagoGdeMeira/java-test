package com.example.repository.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

public class JdbcCustomerRepository implements CustomerRepository {
    private final Connection connection;

    public JdbcCustomerRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Customer findById(String id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM customer WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getString("id"), rs.getString("name"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Customer findByName(String name) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM customer WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(rs.getString("id"), rs.getString("name"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Customer customer) {
        try (PreparedStatement ps = connection
                .prepareStatement("MERGE INTO customer (id, name) KEY(id) VALUES (?, ?)") ) {
            ps.setString(1, customer.getId());
            ps.setString(2, customer.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
