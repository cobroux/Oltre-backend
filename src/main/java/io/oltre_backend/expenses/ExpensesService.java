package io.oltre_backend.expenses;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

@Service
public class ExpensesService {

    public final ExpensesRepository expensesRepository;

    public ExpensesService(ExpensesRepository expensesRepository) {
        this.expensesRepository = expensesRepository;
    }

    private ExpensesDto toDto(Expenses expenses) {
        return new ExpensesDto(expenses.getExpensesName(), expenses.getAmount(), expenses.getRecType(), expenses.getStartDate(), expenses.getEndDate());
    }

    public ExpensesDto getExpensesById(Long id) {
        return toDto(expensesRepository.findById(id).orElseThrow());
    }

    public Iterable<Expenses> getExpensess() {
        return expensesRepository.findAll();
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
}