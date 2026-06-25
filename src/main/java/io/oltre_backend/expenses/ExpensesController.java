package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpensesController {

    private final ExpensesService expensesService;

    public ExpensesController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ExpensesDto getExpense(Long id){
        return expensesService.getExpensesById(id);
    } 
        
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/save")
    public ExpensesDto saveExpenses(Expenses expenses){
        return expensesService.saveExpenses(expenses);
    } 

    @DeleteMapping("/{id}")
    public void deleteExpenses(final Long id){
        expensesService.deleteExpenses(id);
    } 

    @PostMapping("/updateExpenses")
    public void updateExpenses(Long id, String expensesName, Integer amount, RecType recType, String  startDate, String endDate){

         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");        
         expensesService.updateExpenses(id, expensesName, amount, recType, LocalDate.parse(startDate, formatter), LocalDate.parse(endDate, formatter));
     }

}
