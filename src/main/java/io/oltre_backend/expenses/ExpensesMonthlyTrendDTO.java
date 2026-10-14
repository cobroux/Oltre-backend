package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.util.List;

public class ExpensesMonthlyTrendDTO {

    private Integer year;
    private Integer currentMonth;
    private List<BigDecimal> amounts;

    public ExpensesMonthlyTrendDTO(Integer year, Integer currentMonth, List<BigDecimal> amounts) {
        this.year = year;
        this.currentMonth = currentMonth;
        this.amounts = amounts;
    }

    public Integer getYear() { return year; }
    public Integer getCurrentMonth() { return currentMonth; }
    public List<BigDecimal> getAmounts() { return amounts; }
}
