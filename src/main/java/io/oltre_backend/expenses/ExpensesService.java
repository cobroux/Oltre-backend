package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public void updateExpenses(Long id, Long userId, String expensesName, Integer amount, RecType recType, LocalDate startDate, LocalDate endDate) {
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

    // Récap "depuis le 1er janvier" : pour chaque dépense active, combien
    // elle a réellement coûté depuis le début de l'année jusqu'à aujourd'hui
    // (pas une simple projection du mois en cours comme getAmountPerMonth).
    public ExpensesYearRecapDTO getYearRecap(Long userId) {
        return getYearRecap(userId, LocalDate.now());
    }

    public ExpensesYearRecapDTO getYearRecap(Long userId, LocalDate today) {
        LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);

        int monthlyTotal = 0, dailyTotal = 0, yearlyTotal = 0;
        List<ExpensesYearItemDTO> items = new ArrayList<>();

        for (Expenses e : expensesRepository.findByUser_Id(userId)) {
            int amountThisYear = switch (e.getRecType()) {
                case Monthly -> amountForRange(e, yearStart, today, ChronoUnit.MONTHS);
                case Daily   -> amountForRange(e, yearStart, today, ChronoUnit.DAYS);
                case Yearly  -> occursThisYear(e, today) ? e.getAmount() : 0;
            };

            if (amountThisYear <= 0) continue;

            switch (e.getRecType()) {
                case Monthly -> monthlyTotal += amountThisYear;
                case Daily   -> dailyTotal += amountThisYear;
                case Yearly  -> yearlyTotal += amountThisYear;
            }
            items.add(new ExpensesYearItemDTO(e.getExpensesName(), e.getRecType(), amountThisYear));
        }

        items.sort(Comparator.comparing(ExpensesYearItemDTO::getAmountThisYear).reversed());

        return new ExpensesYearRecapDTO(monthlyTotal + dailyTotal + yearlyTotal, monthlyTotal, dailyTotal, yearlyTotal, items);
    }

    // Nombre d'occurrences (mois ou jours, selon `unit`) entre le début de
    // l'année et aujourd'hui pendant lesquelles la dépense était active,
    // bornée par sa propre startDate/endDate.
    private int amountForRange(Expenses e, LocalDate yearStart, LocalDate today, ChronoUnit unit) {
        LocalDate start = e.getStartDate().isAfter(yearStart) ? e.getStartDate() : yearStart;
        LocalDate end = (e.getEndDate() != null && e.getEndDate().isBefore(today)) ? e.getEndDate() : today;
        if (end.isBefore(start)) return 0;

        long count = unit == ChronoUnit.MONTHS
                ? ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)) + 1
                : ChronoUnit.DAYS.between(start, end) + 1;

        return (int) (e.getAmount() * count);
    }

    // Une dépense annuelle ne compte que si son anniversaire (mois/jour de
    // startDate, cette année) est déjà passé et que la dépense était encore
    // active à ce moment-là.
    private boolean occursThisYear(Expenses e, LocalDate today) {
        if (e.getStartDate().getYear() > today.getYear()) return false;
        LocalDate anniversary = LocalDate.of(today.getYear(), e.getStartDate().getMonth(), e.getStartDate().getDayOfMonth());
        if (anniversary.isAfter(today)) return false;
        return e.getEndDate() == null || !e.getEndDate().isBefore(anniversary);
    }
}