package com.example.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.Customer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcCustomerRepositoryTest {
    private Connection connection;
    private JdbcCustomerRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE customer (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255))");
            st.execute("INSERT INTO customer (id, name) VALUES ('1', 'João')");
        }
        repository = new JdbcCustomerRepository(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            try (Statement st = connection.createStatement()) {
                st.execute("DROP TABLE IF EXISTS customer");
            }
            connection.close();
        }
    }

    @Test
    void findByIdShouldReturnCustomerWhenExists() {
        Customer c = repository.findById("1");
        assertNotNull(c);
        assertEquals("João", c.getName());
    }

    @Test
    void findByIdShouldReturnNullWhenNotExists() {
        Customer c = repository.findById("2");
        assertNull(c);
    }
}
