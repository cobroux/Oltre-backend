package io.oltre_backend.tasks;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;


public interface TasksRepository extends JpaRepository<Tasks, Long> {

    List<Tasks> findByUser_Id(Long userId);

    Optional<Tasks> findByIdAndUser_Id(Long id, Long userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE tasks t SET t.tasks_status = :tasksStatus WHERE t.id = :id AND t.user_id = :userId", nativeQuery = true)
    void updateTasksStatus(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("tasksStatus") String tasksStatus
                        );



}
