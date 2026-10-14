package io.oltre_backend.expenses;

import java.math.BigDecimal;

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
        Expenses abo = new Expenses("Abo", new BigDecimal("10"), RecType.Monthly, LocalDate.of(2026, 7, 15), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        List<BigDecimal> trend = service.getMonthlyTrend(1L, TODAY);

        assertEquals(12, trend.size());
        // Jan-Juin : rien, Juillet à décembre : 10 chaque mois
        for (int m = 0; m < 6; m++) assertAmount(0, trend.get(m));
        for (int m = 6; m < 12; m++) assertAmount(10, trend.get(m));
    }

    @Test
    void dailyExpense_countsRealDaysPerMonth() {
        Expenses cafe = new Expenses("Café", new BigDecimal("2"), RecType.Daily, LocalDate.of(2026, 10, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(cafe));

        List<BigDecimal> trend = service.getMonthlyTrend(1L, TODAY);

        // Octobre = 31 jours, novembre = 30, décembre = 31
        assertAmount(62, trend.get(9));
        assertAmount(60, trend.get(10));
        assertAmount(62, trend.get(11));
        assertAmount(0, trend.get(0));
    }

    @Test
    void yearlyExpense_countsFullAmountInAnniversaryMonth_evenIfLaterThisYear() {
        // Régression : la dépense "Annuel" doit apparaître dans la tendance
        // mensuelle au mois de son anniversaire, même si cette date est
        // plus tard dans l'année que "aujourd'hui" (même bug que pour le
        // total annuel - une dépense future ne doit pas disparaître).
        Expenses qqq = new Expenses("qqq", new BigDecimal("1000"), RecType.Yearly, LocalDate.of(2026, 10, 21), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(qqq));

        List<BigDecimal> trend = service.getMonthlyTrend(1L, TODAY);

        assertAmount(1000, trend.get(9)); // octobre, index 9
        assertAmount(0, trend.get(0));
        assertAmount(1000, trend.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void sumOfMonthlyTrend_matchesYearRecapTotal() {
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(
                new Expenses("Netflix", new BigDecimal("15"), RecType.Monthly, LocalDate.of(2024, 1, 1), null),
                new Expenses("Café", new BigDecimal("2"), RecType.Daily, LocalDate.of(2026, 10, 1), null),
                new Expenses("Assurance", new BigDecimal("300"), RecType.Yearly, LocalDate.of(2023, 2, 1), null)
        ));

        List<BigDecimal> trend = service.getMonthlyTrend(1L, TODAY);
        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        BigDecimal trendSum = trend.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertAmount(recap.getTotal(), trendSum);
    }

    // BigDecimal.equals tient compte de l'échelle (20 != 20.00) : on compare la valeur.
    private static void assertAmount(Number expected, BigDecimal actual) {
        BigDecimal exp = expected instanceof BigDecimal b ? b : new BigDecimal(expected.toString());
        assertEquals(0, exp.compareTo(actual), () -> "attendu " + exp + " mais obtenu " + actual);
    }
}
