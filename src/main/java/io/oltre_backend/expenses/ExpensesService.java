package io.oltre_backend.expenses;

import org.springframework.stereotype.Service;

@Service
public class ExpensesService {
    
    public final ExpensesRepository expensesRepository;

    public ExpensesService(ExpensesRepository expensesRepository){
        this.expensesRepository = expensesRepository;
    }

     private ExpensesDto toDto(Expenses expenses) {
            ExpensesDto dto = new ExpensesDto(expenses.getExpensesName(), expenses.getAmount(), expenses.getRecType(), expenses.getStartDate(), expenses.getEndDate());
             return dto;
        }

    public ExpensesDto getExpensesById(Long id){
        Expenses expenses = expensesRepository.findById(id).orElseThrow();
        return toDto(expenses);
    }

     public Iterable<Expenses> getExpensess(){
        return expensesRepository.findAll();
    }

    public void deleteExpenses(final Long id){
        expensesRepository.deleteById(id);
    }

    public ExpensesDto saveExpenses(Expenses expenses){
        Expenses savedExpenses = expensesRepository.save(expenses);
        return toDto(savedExpenses);
    }

}
