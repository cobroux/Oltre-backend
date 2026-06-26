package io.oltre_backend.expenses;

import java.time.LocalDate;

import io.oltre_backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expenses")
public class Expenses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expenses_name")
    private String expensesName;

    @Column(name = "amount")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "rec_type")
    private RecType recType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Expenses() {}

    public Expenses(String name, Integer amount, RecType recType, LocalDate startDate, LocalDate endDate) {
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
    
    public User getUser() { 
        return user; 

    }
    
    public void setUser(User user) { 
        this.user = user; 
    }
}