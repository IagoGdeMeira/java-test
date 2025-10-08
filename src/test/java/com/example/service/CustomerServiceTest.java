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
    void getCustomerNameByIdShouldReturnNameWhenCustomerExists() {
        Customer customer = new Customer("1", "João");
        when(repository.findById("1")).thenReturn(customer);

        String name = service.getCustomerNameById("1");

        assertEquals("João", name);
    }

    @Test
    void getCustomerNameByIdShouldThrowExceptionWhenCustomerNotFound() {
        when(repository.findById("1")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            service.getCustomerNameById("1");
        });
    }

    @Test
    void findCustomerByIdShouldReturnCustomerWhenExists() {
        Customer customer = new Customer("2", "Maria");
        when(repository.findById("2")).thenReturn(customer);

        Customer result = service.findCustomerById("2");
        assertNotNull(result);
        assertEquals("Maria", result.getName());
    }

    @Test
    void findCustomerByIdShouldReturnNullWhenNotFound() {
        when(repository.findById("3")).thenReturn(null);

        Customer result = service.findCustomerById("3");
        assertNull(result);
    }

    @Test
    void saveCustomerShouldCallRepositorySave() {
        Customer customer = new Customer("4", "Carlos");
        service.saveCustomer(customer);
        verify(repository, times(1)).save(customer);
    }
}
