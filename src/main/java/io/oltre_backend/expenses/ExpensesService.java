package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.user.UserRepository;

@Service
public class ExpensesService {

    public final ExpensesRepository expensesRepository;
    private final UserRepository userRepository;

    public ExpensesService(ExpensesRepository expensesRepository, UserRepository userRepository) {
        this.expensesRepository = expensesRepository;
        this.userRepository = userRepository;
    }

    public ExpensesDto toDto(Expenses e) {

        LocalDate today = LocalDate.now();

        ExpensesDto dto = new ExpensesDto();
        dto.setId(e.getId());
        dto.setExpensesName(e.getExpensesName());
        dto.setAmount(e.getAmount());
        dto.setRecType(e.getRecType());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());

        dto.setNextPaymentDate(
            computeNextPaymentDate(dto, today)
        );

        return dto;
    }

    public ExpensesDto getExpensesById(Long id, Long userId) {
        return toDto(expensesRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    public List<ExpensesDto> getExpensess(Long userId) {
    return expensesRepository.findByUser_Id(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public void deleteExpenses(final Long id, Long userId) {
        expensesRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        expensesRepository.deleteById(id);
    }

    public ExpensesDto saveExpenses(Expenses expenses, Long userId) {
        expenses.setUser(userRepository.getReferenceById(userId));
        return toDto(expensesRepository.save(expenses));
    }

    public void updateExpenses(Long id, Long userId, String expensesName, BigDecimal amount, RecType recType, LocalDate startDate, LocalDate endDate) {
        expensesRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        expensesRepository.updateExpenses(id, userId, expensesName, amount, recType.name(), startDate, endDate);
    }

    
    public LocalDate computeNextPaymentDate(ExpensesDto e, LocalDate today) {

    LocalDate startDate = e.getStartDate();
    LocalDate currentDate  = today;
    LocalDate newDate;

        switch (e.getRecType()) {

            
            case Monthly: {

                newDate = LocalDate.of(
                    currentDate.getYear(),
                    currentDate.getMonth(),
                    startDate.getDayOfMonth()
                ).plusMonths(1);
                
                return newDate;
            }

            case Yearly: {
               newDate = LocalDate.of(
                    currentDate.getYear()+1,
                    startDate.getMonth(),
                    startDate.getDayOfMonth()
                );
                
                return newDate;
            }

            case Daily: {
                // Une dépense quotidienne qui n'a pas encore démarré doit
                // afficher son vrai premier prélèvement (startDate), pas
                // "demain" - sinon on annonce un prélèvement avant même que
                // la dépense n'ait commencé.
                newDate = currentDate.isBefore(startDate) ? startDate : currentDate.plusDays(1);
                return newDate;
            }

            default:
                return null;
        }
}

    // Récap "pour l'année en cours" : pour chaque dépense active, son coût
    // projeté sur l'année entière (1er janvier -> 31 décembre), bornée par
    // sa propre startDate/endDate si elles tombent dans l'année. On ne
    // borne PAS par "aujourd'hui" - une dépense annuelle dont la date de
    // départ tombe plus tard dans l'année compte quand même, sinon elle
    // disparaît purement et simplement du total tant que son anniversaire
    // n'est pas encore passé (bug réel : une dépense "Annuel" démarrant en
    // novembre n'apparaissait nulle part avant octobre).
    public ExpensesYearRecapDTO getYearRecap(Long userId) {
        return getYearRecap(userId, LocalDate.now());
    }

    public ExpensesYearRecapDTO getYearRecap(Long userId, LocalDate today) {
        LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(today.getYear(), 12, 31);

        BigDecimal monthlyTotal = BigDecimal.ZERO, dailyTotal = BigDecimal.ZERO, yearlyTotal = BigDecimal.ZERO;
        List<ExpensesYearItemDTO> items = new ArrayList<>();

        for (Expenses e : expensesRepository.findByUser_Id(userId)) {
            BigDecimal amountThisYear = switch (e.getRecType()) {
                case Monthly -> amountForRange(e, yearStart, yearEnd, ChronoUnit.MONTHS);
                case Daily   -> amountForRange(e, yearStart, yearEnd, ChronoUnit.DAYS);
                case Yearly  -> occursThisYear(e, yearEnd) ? e.getAmount() : BigDecimal.ZERO;
            };

            if (amountThisYear.signum() <= 0) continue;

            switch (e.getRecType()) {
                case Monthly -> monthlyTotal = monthlyTotal.add(amountThisYear);
                case Daily   -> dailyTotal = dailyTotal.add(amountThisYear);
                case Yearly  -> yearlyTotal = yearlyTotal.add(amountThisYear);
            }
            items.add(new ExpensesYearItemDTO(e.getExpensesName(), e.getRecType(), amountThisYear));
        }

        items.sort(Comparator.comparing(ExpensesYearItemDTO::getAmountThisYear).reversed());

        return new ExpensesYearRecapDTO(monthlyTotal.add(dailyTotal).add(yearlyTotal), monthlyTotal, dailyTotal, yearlyTotal, items);
    }

    // Nombre d'occurrences (mois ou jours, selon `unit`) entre le début et
    // la fin de l'année pendant lesquelles la dépense était/sera active,
    // bornée par sa propre startDate/endDate.
    private BigDecimal amountForRange(Expenses e, LocalDate yearStart, LocalDate yearEnd, ChronoUnit unit) {
        LocalDate start = e.getStartDate().isAfter(yearStart) ? e.getStartDate() : yearStart;
        LocalDate end = (e.getEndDate() != null && e.getEndDate().isBefore(yearEnd)) ? e.getEndDate() : yearEnd;
        if (end.isBefore(start)) return BigDecimal.ZERO;

        long count = unit == ChronoUnit.MONTHS
                ? ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)) + 1
                : ChronoUnit.DAYS.between(start, end) + 1;

        return e.getAmount().multiply(BigDecimal.valueOf(count));
    }

    // Une dépense annuelle compte dès lors que son anniversaire (mois/jour
    // de startDate) tombe dans l'année en cours, qu'il soit déjà passé ou
    // encore à venir - c'est une projection sur l'année, pas un "déjà payé".
    private boolean occursThisYear(Expenses e, LocalDate yearEnd) {
        if (e.getStartDate().getYear() > yearEnd.getYear()) return false;
        LocalDate anniversary = LocalDate.of(yearEnd.getYear(), e.getStartDate().getMonth(), e.getStartDate().getDayOfMonth());
        return e.getEndDate() == null || !e.getEndDate().isBefore(anniversary);
    }

    // Tendance mensuelle : même projection que getYearRecap, mais répartie
    // mois par mois (Monthly/Daily comptés dans chaque mois où ils sont
    // actifs, Yearly compté en bloc dans son mois anniversaire).
    public List<BigDecimal> getMonthlyTrend(Long userId) {
        return getMonthlyTrend(userId, LocalDate.now());
    }

    public List<BigDecimal> getMonthlyTrend(Long userId, LocalDate today) {
        int year = today.getYear();
        BigDecimal[] totals = new BigDecimal[12];
        Arrays.fill(totals, BigDecimal.ZERO);
        LocalDate yearEnd = LocalDate.of(year, 12, 31);

        for (Expenses e : expensesRepository.findByUser_Id(userId)) {
            switch (e.getRecType()) {
                case Monthly, Daily -> {
                    ChronoUnit unit = e.getRecType() == RecType.Monthly ? ChronoUnit.MONTHS : ChronoUnit.DAYS;
                    for (int m = 1; m <= 12; m++) {
                        LocalDate monthStart = LocalDate.of(year, m, 1);
                        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                        totals[m - 1] = totals[m - 1].add(amountForRange(e, monthStart, monthEnd, unit));
                    }
                }
                case Yearly -> {
                    if (occursThisYear(e, yearEnd)) {
                        int month = e.getStartDate().getMonthValue() - 1;
                        totals[month] = totals[month].add(e.getAmount());
                    }
                }
            }
        }

        return List.of(totals);
    }
}