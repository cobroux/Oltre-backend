package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import io.oltre_backend.user.UserRepository;

class ExpensesMonthlyTrendTest {

    private final ExpensesRepository expensesRepository = org.mockito.Mockito.mock(ExpensesRepository.class);
    private final ExpensesService service = new ExpensesService(expensesRepository, org.mockito.Mockito.mock(UserRepository.class));

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 9);

    @Test
    void monthlyExpense_countsEachActiveMonthOnce() {
        Expenses abo = new Expenses("Abo", 10, RecType.Monthly, LocalDate.of(2026, 7, 15), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        List<Integer> trend = service.getMonthlyTrend(1L, TODAY);

        assertEquals(12, trend.size());
        // Jan-Juin : rien, Juillet à décembre : 10 chaque mois
        for (int m = 0; m < 6; m++) assertEquals(0, trend.get(m));
        for (int m = 6; m < 12; m++) assertEquals(10, trend.get(m));
    }

    @Test
    void dailyExpense_countsRealDaysPerMonth() {
        Expenses cafe = new Expenses("Café", 2, RecType.Daily, LocalDate.of(2026, 10, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(cafe));

        List<Integer> trend = service.getMonthlyTrend(1L, TODAY);

        // Octobre = 31 jours, novembre = 30, décembre = 31
        assertEquals(62, trend.get(9));
        assertEquals(60, trend.get(10));
        assertEquals(62, trend.get(11));
        assertEquals(0, trend.get(0));
    }

    @Test
    void yearlyExpense_countsFullAmountInAnniversaryMonth_evenIfLaterThisYear() {
        // Régression : la dépense "Annuel" doit apparaître dans la tendance
        // mensuelle au mois de son anniversaire, même si cette date est
        // plus tard dans l'année que "aujourd'hui" (même bug que pour le
        // total annuel - une dépense future ne doit pas disparaître).
        Expenses qqq = new Expenses("qqq", 1000, RecType.Yearly, LocalDate.of(2026, 10, 21), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(qqq));

        List<Integer> trend = service.getMonthlyTrend(1L, TODAY);

        assertEquals(1000, trend.get(9)); // octobre, index 9
        assertEquals(0, trend.get(0));
        assertEquals(1000, trend.stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void sumOfMonthlyTrend_matchesYearRecapTotal() {
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(
                new Expenses("Netflix", 15, RecType.Monthly, LocalDate.of(2024, 1, 1), null),
                new Expenses("Café", 2, RecType.Daily, LocalDate.of(2026, 10, 1), null),
                new Expenses("Assurance", 300, RecType.Yearly, LocalDate.of(2023, 2, 1), null)
        ));

        List<Integer> trend = service.getMonthlyTrend(1L, TODAY);
        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        int trendSum = trend.stream().mapToInt(Integer::intValue).sum();
        assertEquals(recap.getTotal(), trendSum);
    }
}
