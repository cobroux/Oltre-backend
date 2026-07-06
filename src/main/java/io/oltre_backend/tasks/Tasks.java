package io.oltre_backend.tasks;

import java.time.LocalDate;

import io.oltre_backend.tasks.task_enum.TasksPriority;
import io.oltre_backend.tasks.task_enum.TasksStatus;
import io.oltre_backend.tasks.task_enum.TasksType;
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
@Table(name = "tasks")
public class Tasks {
        
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tasks_name")
    private String tasksName;

    @Enumerated(EnumType.STRING)
    @Column(name = "tasks_type")
    private TasksType tasksType;

    @Enumerated(EnumType.STRING)
    @Column(name = "tasks_priority")
    private TasksPriority tasksPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "tasks_status")
    private TasksStatus tasksStatus;

    @Column(name = "tasks_date")
    private LocalDate tasksDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTasksName() {
        return tasksName;
    }

    public void setTasksName(String tasksName) {
        this.tasksName = tasksName;
    }

    public TasksType getTasksType() {
        return tasksType;
    }

    public void setTasksType(TasksType tasksType) {
        this.tasksType = tasksType;
    }

    public TasksPriority getTasksPriority() {
        return tasksPriority;
    }

    public void setTasksPriority(TasksPriority tasksPriority) {
        this.tasksPriority = tasksPriority;
    }

    public TasksStatus getTasksStatus() {
        return tasksStatus;
    }

    public void setTasksStatus(TasksStatus tasksStatus) {
        this.tasksStatus = tasksStatus;
    }

    public LocalDate getTasksDate() {
        return tasksDate;
    }

    public void setTasksDate(LocalDate tasksDate) {
        this.tasksDate = tasksDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
