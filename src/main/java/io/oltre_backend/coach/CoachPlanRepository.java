package io.oltre_backend.coach;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CoachPlanRepository extends JpaRepository<CoachPlan, Long> {

    Optional<CoachPlan> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}
