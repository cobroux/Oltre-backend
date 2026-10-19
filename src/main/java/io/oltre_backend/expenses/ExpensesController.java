package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;
import jakarta.validation.Valid;


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


    @GetMapping("/yearRecap")
    public ExpensesYearRecapDTO getYearRecap() {
        return expensesService.getYearRecap(CurrentUser.id());
    }

    @GetMapping("/monthlyTrend")
    public ExpensesMonthlyTrendDTO getMonthlyTrend() {
        LocalDate today = LocalDate.now();
        return new ExpensesMonthlyTrendDTO(today.getYear(), today.getMonthValue(), expensesService.getMonthlyTrend(CurrentUser.id(), today));
    }

    @GetMapping("/amountPerMonth")
    public BigDecimal getAmountPerMonth() {

        BigDecimal amount = BigDecimal.ZERO;
        Iterable<ExpensesDto> expenses = expensesService.getExpensess(CurrentUser.id());

        LocalDate currentDate = LocalDate.now();
        
        for(ExpensesDto e : expenses){
            // Seules les dépenses déjà démarrées (startDate <= aujourd'hui)
            // comptent dans le total du mois - le test était inversé et ne
            // comptait que celles dont la date de début est dans le futur.
            if(!currentDate.isBefore(e.getStartDate())){
                 switch(e.getRecType()){
                    case RecType.Monthly :
                        amount = amount.add(e.getAmount());
                        break;
                    case RecType.Daily :
                        amount = amount.add(e.getAmount().multiply(BigDecimal.valueOf(currentDate.lengthOfMonth())));
                        break;
                    case RecType.Yearly :
                        if(currentDate.getMonth() == e.getStartDate().getMonth()){
                            amount = amount.add(e.getAmount());
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
    public ExpensesDto saveExpenses(@Valid @RequestBody ExpensesDto dto) {
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

}
