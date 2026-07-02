package io.oltre_backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    // static + démarré immédiatement → disponible avant que Spring démarre
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("oltre")
            .withUsername("oltre")
            .withPassword("oltre");

    static {
        mysql.start(); // démarre AVANT Spring et HikariPool
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");        registry.add("spring.flyway.enabled", () -> "false");
    }
}