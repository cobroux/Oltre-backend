package io.oltre_backend.expenses;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpensesRepository extends JpaRepository<Expenses, Long> {

    List<Expenses> findByUser_Id(Long userId);

    Optional<Expenses> findByIdAndUser_Id(Long id, Long userId);

    void deleteByUser_Id(Long userId);

}