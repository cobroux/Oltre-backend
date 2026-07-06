package io.oltre_backend.tasks;

import java.time.LocalDate;

import io.oltre_backend.tasks.task_enum.TasksPriority;
import io.oltre_backend.tasks.task_enum.TasksStatus;
import io.oltre_backend.tasks.task_enum.TasksType;


public class TasksDto {
        
   
    private Long id;
    private String tasksName;
    private TasksType tasksType;
    private TasksPriority tasksPriority;
    private TasksStatus tasksStatus;
    private LocalDate tasksDate;

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
}
