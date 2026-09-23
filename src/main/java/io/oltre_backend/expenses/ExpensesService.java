package io.oltre_backend.expenses;

import java.time.LocalDate;
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