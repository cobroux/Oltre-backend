package io.oltre_backend.expenses;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

    @Entity
    public class Expenses {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String expensesName;

        private Integer amount; 

        private RecType recType;

        private LocalDate startDate;

        private LocalDate endDate;

        public Expenses (){}

        public Expenses(String name, Integer amount, RecType recType, LocalDate startDate, LocalDate endDate){
            this.expensesName = name;
            this.amount = amount;
            this.recType = recType;
            this.startDate = startDate;
            this.endDate = endDate;
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

    public RecType getRecType() {
        return recType;
    }

    public void setRecType(RecType recType) {
        this.recType = recType;
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

    public Long getId() {
        return id;
    }
}