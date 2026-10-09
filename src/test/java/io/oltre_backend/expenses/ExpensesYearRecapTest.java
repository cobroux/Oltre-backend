package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import io.oltre_backend.user.UserRepository;

class ExpensesYearRecapTest {

    private final ExpensesRepository expensesRepository = org.mockito.Mockito.mock(ExpensesRepository.class);
    private final ExpensesService service = new ExpensesService(expensesRepository, org.mockito.Mockito.mock(UserRepository.class));

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 9); // 9 mois écoulés (jan -> oct)

    @Test
    void monthlyExpense_countsOneMonthPerElapsedMonthThisYear() {
        Expenses netflix = new Expenses("Netflix", 15, RecType.Monthly, LocalDate.of(2024, 3, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(netflix));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Actif depuis avant le 1er janvier -> compte jan à oct inclus = 10 mois
        assertEquals(150, recap.getMonthlyTotal());
        assertEquals(150, recap.getTotal());
    }

    @Test
    void monthlyExpense_startedMidYear_countsFromItsStartMonth() {
        Expenses abo = new Expenses("Abo", 10, RecType.Monthly, LocalDate.of(2026, 7, 15), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Juillet, août, septembre, octobre = 4 mois
        assertEquals(40, recap.getMonthlyTotal());
    }

    @Test
    void dailyExpense_countsElapsedDaysThisYear() {
        Expenses cafe = new Expenses("Café", 2, RecType.Daily, LocalDate.of(2026, 10, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(cafe));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // 1er au 9 octobre inclus = 9 jours
        assertEquals(18, recap.getDailyTotal());
    }

    @Test
    void yearlyExpense_alreadyOccurred_countsOnce() {
        Expenses assurance = new Expenses("Assurance", 300, RecType.Yearly, LocalDate.of(2023, 2, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(assurance));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertEquals(300, recap.getYearlyTotal());
    }

    @Test
    void yearlyExpense_notYetOccurredThisYear_countsZero() {
        Expenses assurance = new Expenses("Assurance", 300, RecType.Yearly, LocalDate.of(2023, 12, 25), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(assurance));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertEquals(0, recap.getYearlyTotal());
        assertTrue(recap.getItems().isEmpty());
    }

    @Test
    void monthlyExpense_endedBeforeToday_stopsCountingAfterEndDate() {
        Expenses abo = new Expenses("Abo résilié", 10, RecType.Monthly, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Janvier à mars = 3 mois, rien après la résiliation
        assertEquals(30, recap.getMonthlyTotal());
    }

    @Test
    void total_sumsAcrossAllRecTypes() {
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(
                new Expenses("Netflix", 15, RecType.Monthly, LocalDate.of(2024, 1, 1), null),
                new Expenses("Café", 2, RecType.Daily, LocalDate.of(2026, 10, 1), null),
                new Expenses("Assurance", 300, RecType.Yearly, LocalDate.of(2023, 2, 1), null)
        ));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertEquals(recap.getMonthlyTotal() + recap.getDailyTotal() + recap.getYearlyTotal(), recap.getTotal());
        assertEquals(3, recap.getItems().size());
    }
}
