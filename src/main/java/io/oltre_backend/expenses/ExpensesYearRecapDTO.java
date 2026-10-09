package io.oltre_backend.expenses;

import java.util.List;

public class ExpensesYearRecapDTO {

    private Integer total;
    private Integer monthlyTotal;
    private Integer dailyTotal;
    private Integer yearlyTotal;
    private List<ExpensesYearItemDTO> items;

    public ExpensesYearRecapDTO(Integer total, Integer monthlyTotal, Integer dailyTotal, Integer yearlyTotal,
                                 List<ExpensesYearItemDTO> items) {
        this.total = total;
        this.monthlyTotal = monthlyTotal;
        this.dailyTotal = dailyTotal;
        this.yearlyTotal = yearlyTotal;
        this.items = items;
    }

    public Integer getTotal() { return total; }
    public Integer getMonthlyTotal() { return monthlyTotal; }
    public Integer getDailyTotal() { return dailyTotal; }
    public Integer getYearlyTotal() { return yearlyTotal; }
    public List<ExpensesYearItemDTO> getItems() { return items; }
}
