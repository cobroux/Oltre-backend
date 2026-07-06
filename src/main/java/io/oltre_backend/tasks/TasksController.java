package io.oltre_backend.tasks;

import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/tasks")
public class TasksController {
    private final TasksService tasksService;

    public TasksController(TasksService tasksService) {
        this.tasksService = tasksService;
    }

    @GetMapping("/{id}")
    public TasksDto getTasksById(@PathVariable Long id){
        return tasksService.getTasksById(id);
    } 

    @GetMapping("/all")
    public Iterable<TasksDto> getTasks() {
        return tasksService.getTasks();
    }

    @PostMapping("/save")
    public TasksDto saveTasks(@RequestBody TasksDto dto) {
       
            Tasks tasks = new Tasks();
            tasks.setTasksName(dto.getTasksName());
            tasks.setTasksPriority(dto.getTasksPriority());
            tasks.setTasksStatus(dto.getTasksStatus());
            tasks.setTasksType(dto.getTasksType());
            tasks.setTasksDate(dto.getTasksDate());
        return tasksService.saveTasks(tasks);
    }

    @DeleteMapping( "/{id}")
    public void deleteTasks( @PathVariable final Long id){
        tasksService.deleteTasks(id);
    } 

    @PutMapping("/update")
    public void updateTasksStatus(@RequestBody Map<String, String> body) {

        Long id = Long.valueOf(body.get("id"));
        String status = body.get("status");
        tasksService.updateTasksStatus(id, status);
    }
}
