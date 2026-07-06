package io.oltre_backend.tasks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;


public interface TasksRepository extends JpaRepository<Tasks, Long> {
    
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tasks t SET t.tasks_status = :tasksStatus WHERE t.id = :id", nativeQuery = true)
    void updateTasksStatus(@Param("id") Long id,
                        @Param("tasksStatus") String tasksStatus
                        );



}
