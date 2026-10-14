package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.util.List;

public class ExpensesYearRecapDTO {

    private BigDecimal total;
    private BigDecimal monthlyTotal;
    private BigDecimal dailyTotal;
    private BigDecimal yearlyTotal;
    private List<ExpensesYearItemDTO> items;

    public ExpensesYearRecapDTO(BigDecimal total, BigDecimal monthlyTotal, BigDecimal dailyTotal, BigDecimal yearlyTotal,
                                 List<ExpensesYearItemDTO> items) {
        this.total = total;
        this.monthlyTotal = monthlyTotal;
        this.dailyTotal = dailyTotal;
        this.yearlyTotal = yearlyTotal;
        this.items = items;
    }

    public BigDecimal getTotal() { return total; }
    public BigDecimal getMonthlyTotal() { return monthlyTotal; }
    public BigDecimal getDailyTotal() { return dailyTotal; }
    public BigDecimal getYearlyTotal() { return yearlyTotal; }
    public List<ExpensesYearItemDTO> getItems() { return items; }
}
