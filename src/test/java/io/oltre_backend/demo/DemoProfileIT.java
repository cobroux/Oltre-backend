package io.oltre_backend.demo;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.oltre_backend.auth.JwtService;
import io.oltre_backend.expenses.ExpensesRepository;
import io.oltre_backend.sport.GarminService;
import io.oltre_backend.user.UserRepository;

@ActiveProfiles("demo")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef")
class DemoProfileIT {

    // Ne dérive pas d'AbstractIntegrationTest : base dédiée : le seeder vide les tables, ce qui casserait les autres tests qui partagent le conteneur.
    static final org.testcontainers.containers.MySQLContainer<?> demoDb =
            new org.testcontainers.containers.MySQLContainer<>("mysql:8.4").withDatabaseName("oltre");

    @org.springframework.test.context.DynamicPropertySource
    static void demoDb(org.springframework.test.context.DynamicPropertyRegistry r) {
        demoDb.start();
        r.add("spring.datasource.url", demoDb::getJdbcUrl);
        r.add("spring.datasource.username", demoDb::getUsername);
        r.add("spring.datasource.password", demoDb::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @LocalServerPort int port;
    final HttpClient http = HttpClient.newHttpClient();
    @Autowired UserRepository users;
    @Autowired ExpensesRepository expenses;
    @Autowired DemoDataSeeder seeder;
    @Autowired JwtService jwt;
    @Autowired GarminService garmin;

    int call(String method, String path, String auth, String body) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, BodyPublishers.ofString(body));
        if (auth != null) b.header("Authorization", auth);
        return http.send(b.build(), java.net.http.HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    @Test
    void seedIsIdempotentAndAthleteCanLogIn() throws Exception {
        seeder.reset();
        long u = users.count(), e = expenses.count();
        seeder.reset();
        assertThat(users.count()).isEqualTo(1).isEqualTo(u);
        assertThat(expenses.count()).isEqualTo(e).isPositive();
        assertThat(call("POST", "/auth/login", null,
                "{\"email\":\"demo@oltre.dev\",\"password\":\"" + DemoDataSeeder.PASSWORD + "\"}")).isEqualTo(200);
    }

    @Test
    void garminIsStubbedWithoutGps() {
        assertThat(garmin).isInstanceOf(DemoGarminService.class);
        var list = garmin.getActivities(1L, null, 200);
        assertThat(list).hasSize(30);
        assertThat(garmin.getActivityDetails(1L, list.get(0).getId()).getPolyline()).isEmpty();
    }

    @Test
    void accountRoutesAreForbidden() throws Exception {
        seeder.reset();
        var me = users.findByEmail(DemoDataSeeder.EMAIL);
        String auth = "Bearer " + jwt.generateToken(me.getId(), me.getEmail());
        assertThat(call("POST", "/auth/register", null, "{}")).isEqualTo(403);
        assertThat(call("PUT", "/api/users/me/password", auth, "{}")).isEqualTo(403);
        assertThat(call("DELETE", "/api/users/me", auth, "")).isEqualTo(403);
        assertThat(call("POST", "/api/garmin/connect", auth, "{}")).isEqualTo(403);
        assertThat(users.findByEmail(DemoDataSeeder.EMAIL)).isNotNull();
    }
}
