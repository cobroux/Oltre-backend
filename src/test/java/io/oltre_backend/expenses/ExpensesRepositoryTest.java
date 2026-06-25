package io.oltre_backend.expenses;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import jakarta.transaction.Transactional;



@SpringBootTest
@Transactional
class ExpensesRepositoryTest {

    @Autowired
    private ExpensesRepository expensesRepository;
    
    @Autowired
    private UserRepository userRepository;
 
    @Test
    void testSaveExpense() {

        User john = new User("JohnDoe", 30, LocalDate.of(1996, 5, 12));
        User savedUser = userRepository.save(john);

        Expenses expense = new Expenses(
                "Netflix",
                13,
                RecType.Monthly,
                LocalDate.now(),
                null
        );

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

        User fetchedUser = userRepository.findById(alice.getId()).orElseThrow();
        
        assertEquals(1, fetchedUser.getExpenses().size());
    }

    @Test
    void testUpdateExpenses() {
        Expenses Netflix = expensesRepository.findById(1L).orElseThrow();
        
        expensesRepository.updateExpenses(
            1L, 
            "Netflix Premium",
            20,                
            RecType.Monthly, 
            LocalDate.now(), 
            null
        );

        Expenses updated = expensesRepository.findById(1L).orElseThrow();
        assertEquals("Netflix Premium", updated.getExpensesName());
        assertEquals(20, updated.getAmount());
    }
}