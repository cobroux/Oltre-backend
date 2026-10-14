package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import jakarta.transaction.Transactional;

@Transactional
class ExpensesRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ExpensesRepository expensesRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveExpense() {
        User john = new User("JohnDoe", 30, LocalDate.of(1996, 5, 12));
        User savedUser = userRepository.save(john);

        Expenses expense = new Expenses("Netflix", new BigDecimal("13"), RecType.Monthly, LocalDate.now(), null);
        expense.setUser(savedUser);
        Expenses saved = expensesRepository.save(expense);

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser());
        assertEquals(savedUser.getId(), saved.getUser().getId());
        assertEquals("JohnDoe", saved.getUser().getUsername());
    }

    @Test
    void testFindExpensesByUser() {
        User alice = new User("Alice", 25, LocalDate.of(2001, 1, 1));
        Expenses spotify = new Expenses("Spotify", new BigDecimal("10"), RecType.Monthly, LocalDate.now(), null);
        alice.addExpense(spotify);
        userRepository.save(alice);
        expensesRepository.save(spotify);

        User fetched = userRepository.findById(alice.getId()).orElseThrow();
        assertEquals(1, fetched.getExpenses().size());
    }

    @Test
    void testSaveKeepsCents() {
        User user = new User("TestUser", 25, LocalDate.of(2000, 1, 1));
        userRepository.save(user);

        Expenses spotify = new Expenses("Spotify", new BigDecimal("11.99"), RecType.Monthly, LocalDate.now(), null);
        spotify.setUser(user);
        Expenses saved = expensesRepository.saveAndFlush(spotify);

        Expenses reloaded = expensesRepository.findById(saved.getId()).orElseThrow();
        assertAmount(new BigDecimal("11.99"), reloaded.getAmount());
    }

    // BigDecimal.equals tient compte de l'échelle (20 != 20.00) : on compare la valeur.
    private static void assertAmount(Number expected, BigDecimal actual) {
        BigDecimal exp = expected instanceof BigDecimal b ? b : new BigDecimal(expected.toString());
        assertEquals(0, exp.compareTo(actual), () -> "attendu " + exp + " mais obtenu " + actual);
    }
}