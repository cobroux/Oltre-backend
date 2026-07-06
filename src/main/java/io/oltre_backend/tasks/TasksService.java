package io.oltre_backend.tasks;

import java.time.LocalDate;
import java.util.List;

import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.expenses.ExpensesDto;
import io.oltre_backend.tasks.task_enum.TasksStatus;

import org.springframework.stereotype.Service;

import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.expenses.ExpensesDto;

@Service
public class TasksService {
    

    public final TasksRepository tasksRepository;

    public TasksService(TasksRepository tasksRepository) {
        this.tasksRepository = tasksRepository;
    }

    public TasksDto toDto(Tasks e) {

            LocalDate today = LocalDate.now();

            TasksDto dto = new TasksDto();
            dto.setId(e.getId());
            dto.setTasksName(e.getTasksName());
            dto.setTasksPriority(e.getTasksPriority());
            dto.setTasksStatus(e.getTasksStatus());
            dto.setTasksType(e.getTasksType());
            dto.setTasksDate(today);

            return dto;
        }

    public void deleteTasks(final Long id) {
        tasksRepository.deleteById(id);
    }

    public TasksDto getTasksById(Long id) {
        return toDto(tasksRepository.findById(id).orElseThrow());
    }


    public TasksDto saveTasks(Tasks expenses) {
        return toDto(tasksRepository.save(expenses));
    }

    public List<TasksDto> getTasks() {
    return tasksRepository.findAll()
            .stream()
            .map(this::toDto)
            .toList();
    }   

     public void updateTasksStatus(Long id, String status) {        
        tasksRepository.updateTasksStatus(id, status);
    }

}
