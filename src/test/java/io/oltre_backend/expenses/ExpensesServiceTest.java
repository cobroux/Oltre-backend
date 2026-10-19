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

    private ExpensesDto expense(RecType type, LocalDate start) {
        ExpensesDto dto = new ExpensesDto();
        dto.setRecType(type);
        dto.setStartDate(start);
        return dto;
    }

    @Test
    void monthlyStartingOn31_isClampedToEndOfMonth() {
        LocalDate next = service.computeNextPaymentDate(
                expense(RecType.Monthly, LocalDate.of(2026, 1, 31)), LocalDate.of(2026, 2, 10));
        assertEquals(LocalDate.of(2026, 2, 28), next);
    }

    @Test
    void monthlyDueLaterThisMonth_isNotPushedToNextMonth() {
        LocalDate next = service.computeNextPaymentDate(
                expense(RecType.Monthly, LocalDate.of(2026, 1, 20)), LocalDate.of(2026, 10, 8));
        assertEquals(LocalDate.of(2026, 10, 20), next);
    }

    @Test
    void monthlyDueToday_isToday_andPassedGoesToNextMonth() {
        ExpensesDto e = expense(RecType.Monthly, LocalDate.of(2026, 1, 8));
        assertEquals(LocalDate.of(2026, 10, 8), service.computeNextPaymentDate(e, LocalDate.of(2026, 10, 8)));
        assertEquals(LocalDate.of(2026, 11, 8), service.computeNextPaymentDate(e, LocalDate.of(2026, 10, 9)));
    }

    @Test
    void monthlyNotStartedYet_isStartDate() {
        LocalDate next = service.computeNextPaymentDate(
                expense(RecType.Monthly, LocalDate.of(2026, 12, 15)), LocalDate.of(2026, 10, 8));
        assertEquals(LocalDate.of(2026, 12, 15), next);
    }

    @Test
    void yearlyOnLeapDay_isClampedInNonLeapYear() {
        LocalDate next = service.computeNextPaymentDate(
                expense(RecType.Yearly, LocalDate.of(2024, 2, 29)), LocalDate.of(2026, 10, 8));
        assertEquals(LocalDate.of(2027, 2, 28), next);
    }

    @Test
    void yearlyNotPassedYet_isThisYear() {
        LocalDate next = service.computeNextPaymentDate(
                expense(RecType.Yearly, LocalDate.of(2020, 12, 25)), LocalDate.of(2026, 10, 8));
        assertEquals(LocalDate.of(2026, 12, 25), next);
    }
}
