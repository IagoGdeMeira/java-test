package com.example.service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {
    private CustomerRepository repository;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        repository = mock(CustomerRepository.class);
        service = new CustomerService(repository);
    }

    @Test
    void shouldReturnCustomerNameWhenCustomerExists() {
        Customer customer = new Customer("1", "João");
        when(repository.findById("1")).thenReturn(customer);

        String name = service.getCustomerNameById("1");

        assertEquals("João", name);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(repository.findById("1")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            service.getCustomerNameById("1");
        });
    }
}
