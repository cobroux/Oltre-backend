package io.oltre_backend.expenses;

import java.math.BigDecimal;

public class ExpensesYearItemDTO {

    private String expensesName;
    private RecType recType;
    private BigDecimal amountThisYear;

    public ExpensesYearItemDTO(String expensesName, RecType recType, BigDecimal amountThisYear) {
        this.expensesName = expensesName;
        this.recType = recType;
        this.amountThisYear = amountThisYear;
    }

    public String getExpensesName() { return expensesName; }
    public RecType getRecType() { return recType; }
    public BigDecimal getAmountThisYear() { return amountThisYear; }
}
