package io.oltre_backend.expenses;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface ExpensesRepository extends JpaRepository<Expenses, Long> {

    List<Expenses> findByUser_Id(Long userId);

    Optional<Expenses> findByIdAndUser_Id(Long id, Long userId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE expenses e SET e.expenses_name = :expensesName, e.amount = :amount, e.rec_type = :recType, e.start_date = :startDate, e.end_date = :endDate WHERE e.id = :id AND e.user_id = :userId", nativeQuery = true)
    void updateExpenses(@Param("id") Long id,
                        @Param("userId") Long userId,
                        @Param("expensesName") String expensesName,
                        @Param("amount") Integer amount,
                        @Param("recType") String recType,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

}