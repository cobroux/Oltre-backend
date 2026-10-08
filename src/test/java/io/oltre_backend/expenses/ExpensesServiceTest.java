package io.oltre_backend.expenses;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ExpensesServiceTest {

    private final ExpensesService service = new ExpensesService(null, null);

    private ExpensesDto dailyExpense(LocalDate startDate) {
        ExpensesDto dto = new ExpensesDto();
        dto.setRecType(RecType.Daily);
        dto.setStartDate(startDate);
        return dto;
    }

    @Test
    void dailyExpenseAlreadyStarted_nextPaymentIsTomorrow() {
        LocalDate today = LocalDate.of(2026, 10, 8);
        ExpensesDto expense = dailyExpense(today.minusDays(5));

        LocalDate next = service.computeNextPaymentDate(expense, today);

        assertEquals(today.plusDays(1), next);
    }

    @Test
    void dailyExpenseNotStartedYet_nextPaymentIsStartDate() {
        LocalDate today = LocalDate.of(2026, 10, 8);
        LocalDate startDate = today.plusDays(10);
        ExpensesDto expense = dailyExpense(startDate);

        LocalDate next = service.computeNextPaymentDate(expense, today);

        assertEquals(startDate, next);
    }

    @Test
    void dailyExpenseStartingToday_nextPaymentIsTomorrow() {
        LocalDate today = LocalDate.of(2026, 10, 8);
        ExpensesDto expense = dailyExpense(today);

        LocalDate next = service.computeNextPaymentDate(expense, today);

        assertEquals(today.plusDays(1), next);
    }
}
