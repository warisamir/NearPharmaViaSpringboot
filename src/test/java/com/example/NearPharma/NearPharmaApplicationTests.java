package com.example.NearPharma;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class NearPharmaApplicationTests {

    /**
     * Testcontainers spins up a real postgres:15 Docker container.
     * @ServiceConnection auto-wires spring.datasource.* from the container —
     * no manual URL/username/password config needed.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Test
    void contextLoads() {
    }
}
