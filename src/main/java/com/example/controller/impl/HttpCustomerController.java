package com.example.controller.impl;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpCustomerController implements com.example.controller.CustomerController {
    private final HttpServer server;
    private final CustomerRepository repository;

    public HttpCustomerController(CustomerRepository repository, int port) throws IOException {
        this.repository = repository;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/customers", new CustomersHandler());
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    class CustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            try {
                if ("POST".equalsIgnoreCase(method) && "/customers".equals(uri.getPath())) {
                    this.handlePost(exchange);
                } else if ("GET".equalsIgnoreCase(method) && uri.getPath().startsWith("/customers/")) {
                    this.handleGet(exchange, uri.getPath().substring("/customers/".length()));
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } catch (Exception e) {
                byte[] bytes = e.getMessage().getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Customer c = this.parseCustomer(body);
            if (c == null || c.getId() == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            repository.save(c);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(201, 0);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(this.toJson(c).getBytes(StandardCharsets.UTF_8));
            }
        }

        private void handleGet(HttpExchange exchange, String id) throws IOException {
            Customer c = repository.findById(id);
            if (c == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String json = this.toJson(c);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Customer parseCustomer(String json) {
            // very small ad-hoc parser for tests only: expects {"id":"...","name":"..."}
            if (json == null)
                return null;
            String s = json.trim();
            if (!s.startsWith("{") || !s.endsWith("}"))
                return null;
            String id = null, name = null;
            s = s.substring(1, s.length() - 1);
            String[] parts = s.split(",");
            for (String p : parts) {
                String[] kv = p.split(":", 2);
                if (kv.length != 2)
                    continue;
                String key = kv[0].trim().replaceAll("\"", "");
                String val = kv[1].trim().replaceAll("\"", "");
                if ("id".equals(key))
                    id = val;
                if ("name".equals(key))
                    name = val;
            }
            return new Customer(id, name);
        }

        private String toJson(Customer c) {
            return "{\"id\":\"" + this.escape(c.getId()) + "\",\"name\":\"" + this.escape(c.getName()) + "\"}";
        }

        private String escape(String s) {
            if (s == null)
                return "";
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}