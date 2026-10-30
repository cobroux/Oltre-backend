package io.oltre_backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistrationClosedTest {

    @Test
    void register_whenRegistrationDisabled_returns403WithoutTouchingTheDatabase() {
        // Dépendances nulles : le refus doit arriver avant tout accès au repository.
        AuthController controller = new AuthController(null, null, null, null, false);

        ResponseEntity<?> response = controller.register(new AuthController.RegisterRequest(
                "intrus", "intrus@example.com", "motdepasse123", null, null, null));

        assertEquals(403, response.getStatusCode().value());
    }
}
