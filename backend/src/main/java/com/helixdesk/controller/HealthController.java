package com.helixdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", "helixdesk-api");
        body.put("phase", 16);
        body.put("timestamp", Instant.now().toString());
        body.put("database", databaseStatus());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "HelixDesk");
        body.put("application", "helixdesk-api");
        body.put("description", "AI IT Help Desk / AI Ticket Resolution System");
        body.put("phase", 16);
        body.put("phaseName", "Complete integration");
        body.put("javaVersion", System.getProperty("java.version"));
        body.put("timestamp", Instant.now().toString());
        body.put("stack", List.of("Java 21", "Spring Boot", "Spring Data JPA", "Hibernate", "MariaDB", "Angular"));
        return ResponseEntity.ok(body);
    }

    private Map<String, Object> databaseStatus() {
        Map<String, Object> db = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            db.put("status", "UP");
            db.put("product", connection.getMetaData().getDatabaseProductName());
            db.put("version", connection.getMetaData().getDatabaseProductVersion());
            db.put("valid", connection.isValid(2));
        } catch (Exception ex) {
            db.put("status", "DOWN");
            db.put("error", ex.getMessage());
        }
        return db;
    }
}
