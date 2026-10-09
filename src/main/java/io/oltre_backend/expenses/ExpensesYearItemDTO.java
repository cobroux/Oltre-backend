package io.oltre_backend.expenses;

public class ExpensesYearItemDTO {

    private String expensesName;
    private RecType recType;
    private Integer amountThisYear;

    public ExpensesYearItemDTO(String expensesName, RecType recType, Integer amountThisYear) {
        this.expensesName = expensesName;
        this.recType = recType;
        this.amountThisYear = amountThisYear;
    }

    public String getExpensesName() { return expensesName; }
    public RecType getRecType() { return recType; }
    public Integer getAmountThisYear() { return amountThisYear; }
}
