package io.oltre_backend.expenses;

import java.math.BigDecimal;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ExpensesDto {

        private Long id;
    
        @Size(max = 255, message = "Le nom est trop long")
        private String expensesName;

        @NotNull(message = "Le montant est requis")
        @Positive(message = "Le montant doit être positif")
        private BigDecimal amount;

        @NotNull(message = "La récurrence est requise")
        private RecType recType;

        @NotNull(message = "La date de début est requise")
        private LocalDate startDate;

        private LocalDate endDate;

        private LocalDate nextPaymentDate;


    public ExpensesDto(Long id, String expensesName, BigDecimal amount,  RecType recType, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.amount = amount;
        this.endDate = endDate;
        this.expensesName = expensesName;
        this.recType = recType;
        this.startDate = startDate;
    }

    public ExpensesDto() {}


    public String getExpensesName() {
        return expensesName;
    }

    public void setExpensesName(String expensesName) {
        this.expensesName = expensesName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    @AssertTrue(message = "La date de fin doit être postérieure à la date de début")
    public boolean isEndDateValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(LocalDate nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }
        
}
