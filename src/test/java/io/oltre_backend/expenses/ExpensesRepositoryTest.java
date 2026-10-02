package io.oltre_backend.expenses;

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

        Expenses expense = new Expenses("Netflix", 13, RecType.Monthly, LocalDate.now(), null);
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
        Expenses spotify = new Expenses("Spotify", 10, RecType.Monthly, LocalDate.now(), null);
        alice.addExpense(spotify);
        userRepository.save(alice);
        expensesRepository.save(spotify);

        User fetched = userRepository.findById(alice.getId()).orElseThrow();
        assertEquals(1, fetched.getExpenses().size());
    }

    @Test
    void testUpdateExpenses() {
        User user = new User("TestUser", 25, LocalDate.of(2000, 1, 1));
        userRepository.save(user);

        Expenses netflix = new Expenses("Netflix", 13, RecType.Monthly, LocalDate.now(), null);
        netflix.setUser(user);
        Expenses saved = expensesRepository.save(netflix);

        expensesRepository.updateExpenses(
            saved.getId(), user.getId(), "Netflix Premium", 20,
            RecType.Monthly.name(), LocalDate.now(), null
        );
        expensesRepository.flush();

        Expenses updated = expensesRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Netflix Premium", updated.getExpensesName());
        assertEquals(20, updated.getAmount());
    }
}