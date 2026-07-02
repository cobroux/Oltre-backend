package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:4200")
public class ExpensesController {

    private final ExpensesService expensesService;

    public ExpensesController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/{id}")
    public ExpensesDto getExpense(@PathVariable Long id){
        return expensesService.getExpensesById(id);
    } 

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/all")
    public Iterable<ExpensesDto> getExpenses() {
        return expensesService.getExpensess();
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @GetMapping("/amountPerMonth")
    public Integer getAmountPerMonth() {

        Integer amount = 0;
        Iterable<ExpensesDto> expenses = expensesService.getExpensess();

        LocalDate currentDate = LocalDate.now();
        
        for(ExpensesDto e : expenses){
            if(currentDate.isBefore(e.getStartDate())){
                 switch(e.getRecType()){
                    case RecType.Monthly : 
                        amount+=e.getAmount();
                        break;
                    case RecType.Daily : 
                        amount+=(e.getAmount()*currentDate.lengthOfMonth());
                        break;
                    case RecType.Yearly :
                        if(currentDate.getMonth() == e.getStartDate().getMonth()){
                            amount+=(e.getAmount());
                        } 
                        break;
                    default:
                        break;
                }
            }
        }

        return amount;
    }

        
    @CrossOrigin(origins = "http://localhost:4200")
    @PostMapping("/save")
    public ExpensesDto saveExpenses(@RequestBody ExpensesDto dto) {
        Expenses expenses = new Expenses(
            dto.getExpensesName(),
            dto.getAmount(),
            dto.getRecType(), // recType
            dto.getStartDate(),
            null  // endDate
        );
        return expensesService.saveExpenses(expenses);
    }

    @CrossOrigin(origins = "http://localhost:4200")
    @DeleteMapping( "/{id}")
    public void deleteExpenses( @PathVariable final Long id){
        expensesService.deleteExpenses(id);
    } 

    @PostMapping("/updateExpenses")
    public void updateExpenses(Long id, String expensesName, Integer amount, RecType recType, String  startDate, String endDate){

         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");        
         expensesService.updateExpenses(id, expensesName, amount, recType, LocalDate.parse(startDate, formatter), LocalDate.parse(endDate, formatter));
     }



}
