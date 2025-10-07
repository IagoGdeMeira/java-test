package com.example.controller;

import com.example.repository.JdbcCustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class HttpCustomerControllerTest {
    private Connection connection;
    private JdbcCustomerRepository repository;
    private HttpCustomerController controller;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:e2edb;DB_CLOSE_DELAY=-1");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE customer (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255))");
        }
        repository = new JdbcCustomerRepository(connection);
        controller = new HttpCustomerController(repository, 8001); // 8001 => specific port
        controller.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (controller != null)
            controller.stop();
        if (connection != null && !connection.isClosed()) {
            try (Statement st = connection.createStatement()) {
                st.execute("DROP TABLE IF EXISTS customer");
            }
            connection.close();
        }
    }

    @Test
    void createAndGetCustomer() throws IOException, InterruptedException {
        int port = controller.getPort();
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"id\":\"42\",\"name\":\"Alice\"}";
        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/customers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResp = client.send(post, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResp.statusCode());

        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/customers/42"))
                .GET()
                .build();

        HttpResponse<String> getResp = client.send(get, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResp.statusCode());
        assertTrue(getResp.body().contains("Alice"));

        HttpRequest getMissing = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/customers/missing"))
                .GET()
                .build();

        HttpResponse<String> getMissingResp = client.send(getMissing, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getMissingResp.statusCode());
    }
}
