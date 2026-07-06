package io.oltre_backend.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.tasks.Tasks;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private Integer age;

    private LocalDate birthDate;

    @OneToMany(mappedBy="user")
    private Set<Expenses> expenses  = new java.util.HashSet<>();;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Tasks> tasks = new ArrayList<>();

    public User() {}

    public User(String username, Integer age, LocalDate birthDate) {
        this.username = username;
        this.age = age;
        this.birthDate = birthDate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Integer getAge() {
        return age;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Set<Expenses> getExpenses() {
        return expenses;
    }

    public void setExpenses(Set<Expenses> expenses) {
        this.expenses = expenses;
    }

    public void addExpense(Expenses expense) {
        this.expenses.add(expense);
        expense.setUser(this);
    }

    public void addTask(Tasks task) {
        tasks.add(task);
        task.setUser(this);
    }

    public List<Tasks> getTasks() {
        return tasks;
    }

    public void setTasks(List<Tasks> tasks) {
        this.tasks = tasks;
    }
}