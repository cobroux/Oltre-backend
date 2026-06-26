package io.oltre_backend.user;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.oltre_backend.AbstractIntegrationTest;
import jakarta.transaction.Transactional;

@Transactional
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        User user = new User("enzo", 22, LocalDate.of(2003, 1, 1));
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        assertEquals("enzo", saved.getUsername());
    }

    @Test
    void testFindAllUsers() {
        User user = new User("test", 20, LocalDate.of(2005, 5, 5));
        userRepository.save(user);
        List<User> users = userRepository.findAll();
        assertFalse(users.isEmpty());
    }
}