package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExpensesRepositoryTest {

    @Autowired
    private ExpensesRepository expensesRepository;

    @Test
    void testSaveExpense() {
        Expenses expense = new Expenses(
                "Netflix",
                13,
                RecType.Monthly,
                LocalDate.now(),
                null
        );

        Expenses saved = expensesRepository.save(expense);

        assertNotNull(saved.getId());
        assertEquals("Netflix", saved.getExpensesName());
        assertEquals(13, saved.getAmount());
    }

    @Test
    void testFindAllExpenses() {
        Expenses expense = new Expenses(
                "Spotify",
                10,
                RecType.Monthly,
                LocalDate.now(),
                null
        );

        expensesRepository.save(expense);

        List<Expenses> expenses = expensesRepository.findAll();

        assertFalse(expenses.isEmpty());
    }
}