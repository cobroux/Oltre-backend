package io.oltre_backend.tasks;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.user.UserRepository;

@Service
public class TasksService {


    public final TasksRepository tasksRepository;
    private final UserRepository userRepository;

    public TasksService(TasksRepository tasksRepository, UserRepository userRepository) {
        this.tasksRepository = tasksRepository;
        this.userRepository = userRepository;
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

    public void deleteTasks(final Long id, Long userId) {
        tasksRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tasksRepository.deleteById(id);
    }

    public TasksDto getTasksById(Long id, Long userId) {
        return toDto(tasksRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }


    public TasksDto saveTasks(Tasks tasks, Long userId) {
        tasks.setUser(userRepository.getReferenceById(userId));
        return toDto(tasksRepository.save(tasks));
    }

    public List<TasksDto> getTasks(Long userId) {
    return tasksRepository.findByUser_Id(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }

     public void updateTasksStatus(Long id, Long userId, String status) {
        tasksRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        tasksRepository.updateTasksStatus(id, userId, status);
    }

}
