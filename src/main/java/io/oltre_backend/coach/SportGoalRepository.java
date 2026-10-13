package io.oltre_backend.coach;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SportGoalRepository extends JpaRepository<SportGoal, Long> {

    List<SportGoal> findByUser_IdOrderByEventDateAsc(Long userId);

    Optional<SportGoal> findByIdAndUser_Id(Long id, Long userId);

    void deleteByUser_Id(Long userId);
}
