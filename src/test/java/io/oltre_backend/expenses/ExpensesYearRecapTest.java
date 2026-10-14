package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import io.oltre_backend.user.UserRepository;

class ExpensesYearRecapTest {

    private final ExpensesRepository expensesRepository = org.mockito.Mockito.mock(ExpensesRepository.class);
    private final ExpensesService service = new ExpensesService(expensesRepository, org.mockito.Mockito.mock(UserRepository.class));

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 9);

    @Test
    void monthlyExpense_activeAllYear_countsAllTwelveMonths() {
        Expenses netflix = new Expenses("Netflix", new BigDecimal("15"), RecType.Monthly, LocalDate.of(2024, 3, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(netflix));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Actif depuis avant le 1er janvier -> compte les 12 mois de l'année
        assertAmount(180, recap.getMonthlyTotal());
        assertAmount(180, recap.getTotal());
    }

    @Test
    void monthlyExpense_startedMidYear_countsFromItsStartMonthToDecember() {
        Expenses abo = new Expenses("Abo", new BigDecimal("10"), RecType.Monthly, LocalDate.of(2026, 7, 15), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Juillet à décembre = 6 mois
        assertAmount(60, recap.getMonthlyTotal());
    }

    @Test
    void dailyExpense_countsDaysFromStartToYearEnd() {
        Expenses cafe = new Expenses("Café", new BigDecimal("2"), RecType.Daily, LocalDate.of(2026, 10, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(cafe));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // 1er octobre au 31 décembre inclus = 92 jours - pas juste jusqu'à
        // "aujourd'hui", c'est une projection sur l'année entière
        assertAmount(184, recap.getDailyTotal());
    }

    @Test
    void monthlyExpense_keepsCents() {
        Expenses spotify = new Expenses("Spotify", new BigDecimal("11.99"), RecType.Monthly, LocalDate.of(2024, 1, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(spotify));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // 12 × 11,99 € : les centimes ne doivent plus être tronqués
        assertAmount(new BigDecimal("143.88"), recap.getMonthlyTotal());
    }

    @Test
    void yearlyExpense_anniversaryAlreadyPassed_countsOnce() {
        Expenses assurance = new Expenses("Assurance", new BigDecimal("300"), RecType.Yearly, LocalDate.of(2023, 2, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(assurance));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertAmount(300, recap.getYearlyTotal());
    }

    @Test
    void yearlyExpense_anniversaryLaterThisYear_stillCounts() {
        // Régression : une dépense annuelle dont l'anniversaire tombe PLUS
        // TARD dans l'année (après "aujourd'hui") disparaissait entièrement
        // du total et du graphique - alors qu'elle doit bien être comptée
        // dans la projection de l'année en cours.
        Expenses assurance = new Expenses("Assurance", new BigDecimal("300"), RecType.Yearly, LocalDate.of(2023, 12, 25), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(assurance));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertAmount(300, recap.getYearlyTotal());
        assertFalse(recap.getItems().isEmpty());
    }

    @Test
    void yearlyExpense_startingLaterThisYear_countsInProjection() {
        // Cas exact signalé : dépense "Annuel" créée avec une date de début
        // future dans l'année en cours (ex. le 21/10 alors qu'on est le
        // 9/10) - doit apparaître dans le total, pas disparaître.
        Expenses qqq = new Expenses("qqq", new BigDecimal("1000"), RecType.Yearly, LocalDate.of(2026, 10, 21), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(qqq));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertAmount(1000, recap.getYearlyTotal());
        assertAmount(1000, recap.getTotal());
    }

    @Test
    void yearlyExpense_startingNextYear_doesNotCount() {
        Expenses future = new Expenses("Futur", new BigDecimal("500"), RecType.Yearly, LocalDate.of(2027, 3, 1), null);
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(future));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertAmount(0, recap.getYearlyTotal());
        assertTrue(recap.getItems().isEmpty());
    }

    @Test
    void monthlyExpense_endedMidYear_stopsCountingAfterEndDate() {
        Expenses abo = new Expenses("Abo résilié", new BigDecimal("10"), RecType.Monthly, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(abo));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        // Janvier à mars = 3 mois, rien après la résiliation
        assertAmount(30, recap.getMonthlyTotal());
    }

    @Test
    void total_sumsAcrossAllRecTypes() {
        when(expensesRepository.findByUser_Id(1L)).thenReturn(List.of(
                new Expenses("Netflix", new BigDecimal("15"), RecType.Monthly, LocalDate.of(2024, 1, 1), null),
                new Expenses("Café", new BigDecimal("2"), RecType.Daily, LocalDate.of(2026, 10, 1), null),
                new Expenses("Assurance", new BigDecimal("300"), RecType.Yearly, LocalDate.of(2023, 2, 1), null)
        ));

        ExpensesYearRecapDTO recap = service.getYearRecap(1L, TODAY);

        assertAmount(recap.getMonthlyTotal().add(recap.getDailyTotal()).add(recap.getYearlyTotal()), recap.getTotal());
        assertEquals(3, recap.getItems().size());
    }

    // BigDecimal.equals tient compte de l'échelle (20 != 20.00) : on compare la valeur.
    private static void assertAmount(Number expected, BigDecimal actual) {
        BigDecimal exp = expected instanceof BigDecimal b ? b : new BigDecimal(expected.toString());
        assertEquals(0, exp.compareTo(actual), () -> "attendu " + exp + " mais obtenu " + actual);
    }
}
