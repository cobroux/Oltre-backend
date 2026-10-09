package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;


@RestController
@RequestMapping("/api/expenses")
 
public class ExpensesController {

    private final ExpensesService expensesService;

    public ExpensesController(ExpensesService expensesService) {
        this.expensesService = expensesService;
    }

     
    @GetMapping("/{id}")
    public ExpensesDto getExpense(@PathVariable Long id){
        return expensesService.getExpensesById(id, CurrentUser.id());
    }


    @GetMapping("/all")
    public Iterable<ExpensesDto> getExpenses() {
        return expensesService.getExpensess(CurrentUser.id());
    }


    @GetMapping("/amountPerMonth")
    public Integer getAmountPerMonth() {

        Integer amount = 0;
        Iterable<ExpensesDto> expenses = expensesService.getExpensess(CurrentUser.id());

        LocalDate currentDate = LocalDate.now();
        
        for(ExpensesDto e : expenses){
            // Seules les dépenses déjà démarrées (startDate <= aujourd'hui)
            // comptent dans le total du mois - le test était inversé et ne
            // comptait que celles dont la date de début est dans le futur.
            if(!currentDate.isBefore(e.getStartDate())){
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

        
    @PostMapping("/save")
    public ExpensesDto saveExpenses(@RequestBody ExpensesDto dto) {
        Expenses expenses = new Expenses(
            dto.getExpensesName(),
            dto.getAmount(),
            dto.getRecType(), // recType
            dto.getStartDate(),
            dto.getEndDate()
        );
        return expensesService.saveExpenses(expenses, CurrentUser.id());
    }


    @DeleteMapping( "/{id}")
    public void deleteExpenses( @PathVariable final Long id){
        expensesService.deleteExpenses(id, CurrentUser.id());
    }


    @PostMapping("/updateExpenses")
    public void updateExpenses(Long id, String expensesName, Integer amount, RecType recType, String  startDate, String endDate){

         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
         expensesService.updateExpenses(id, CurrentUser.id(), expensesName, amount, recType, LocalDate.parse(startDate, formatter), LocalDate.parse(endDate, formatter));
     }

}
