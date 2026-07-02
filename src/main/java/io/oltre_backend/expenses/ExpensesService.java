package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ExpensesService {

    public final ExpensesRepository expensesRepository;

    public ExpensesService(ExpensesRepository expensesRepository) {
        this.expensesRepository = expensesRepository;
    }

    public ExpensesDto toDto(Expenses e) {

        LocalDate today = LocalDate.now();

        ExpensesDto dto = new ExpensesDto();
        dto.setId(e.getId());
        dto.setExpensesName(e.getExpensesName());
        dto.setAmount(e.getAmount());
        dto.setRecType(e.getRecType());
        dto.setStartDate(e.getStartDate());

        dto.setNextPaymentDate(
            computeNextPaymentDate(dto, today)
        );

        return dto;
    }

    public ExpensesDto getExpensesById(Long id) {
        return toDto(expensesRepository.findById(id).orElseThrow());
    }

    public List<ExpensesDto> getExpensess() {
    return expensesRepository.findAll()
            .stream()
            .map(this::toDto)
            .toList();
    }   

    public void deleteExpenses(final Long id) {
        expensesRepository.deleteById(id);
    }

    public ExpensesDto saveExpenses(Expenses expenses) {
        return toDto(expensesRepository.save(expenses));
    }

    public void updateExpenses(Long id, String expensesName, Integer amount, RecType recType, LocalDate startDate, LocalDate endDate) {
        expensesRepository.updateExpenses(id, expensesName, amount, recType.name(), startDate, endDate);
    }

    
    public LocalDate computeNextPaymentDate(ExpensesDto e, LocalDate today) {

    LocalDate startDate = e.getStartDate();
    LocalDate currentDate  = LocalDate.now();
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
                newDate = currentDate.plusDays(1);
                return newDate;
            }

            default:
                return null;
        }
}
}