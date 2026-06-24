package io.oltre_backend.expenses;

import java.time.LocalDate;

public class ExpensesDto {
    
        private String expensesName;

        private Integer amount; 

        private RecType recType;

        private LocalDate startDate;

        private LocalDate endDate;

    public ExpensesDto(String expensesName, Integer amount,  RecType recType, LocalDate startDate, LocalDate endDate) {
        this.amount = amount;
        this.endDate = endDate;
        this.expensesName = expensesName;
        this.recType = recType;
        this.startDate = startDate;
    }

    public String getExpensesName() {
        return expensesName;
    }

    public void setExpensesName(String expensesName) {
        this.expensesName = expensesName;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setRecType(RecType recType) {
        this.recType = recType;
    }

    public RecType getRecType() {
        return recType;
    }
        
}
