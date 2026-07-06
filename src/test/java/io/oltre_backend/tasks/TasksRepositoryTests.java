package io.oltre_backend.tasks;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.tasks.task_enum.*;
import io.oltre_backend.tasks.Tasks;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import jakarta.transaction.Transactional;

@Transactional
class TasksRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveTask() {

        User user = new User("JohnDoe", 30, LocalDate.of(1996, 5, 12));
        User savedUser = userRepository.save(user);

        Tasks task = new Tasks();
        task.setTasksName("Faire les courses");
        task.setTasksType(TasksType.PERSO);
        task.setTasksPriority(TasksPriority.HIGH);
        task.setTasksStatus(TasksStatus.DONE);
        task.setTasksDate(LocalDate.now());
        task.setUser(savedUser);

        Tasks savedTask = tasksRepository.save(task);

        assertNotNull(savedTask.getId());
        assertNotNull(savedTask.getUser());
        assertEquals(savedUser.getId(), savedTask.getUser().getId());
        assertEquals("Faire les courses", savedTask.getTasksName());
    }

    @Test
    void testFindTasksByUser() {

        User user = new User("Alice", 25, LocalDate.of(2001, 1, 1));

        Tasks task = new Tasks();
        task.setTasksName("Réviser");
        task.setTasksType(TasksType.PERSO);
        task.setTasksPriority(TasksPriority.MEDIUM);
        task.setTasksStatus(TasksStatus.PENDING);
        task.setTasksDate(LocalDate.now());

        user.addTask(task);

        userRepository.save(user);
        tasksRepository.save(task);

        User fetched = userRepository.findById(user.getId()).orElseThrow();

        assertEquals(1, fetched.getTasks().size());
        assertEquals("Réviser", fetched.getTasks().getFirst().getTasksName());
    }
}