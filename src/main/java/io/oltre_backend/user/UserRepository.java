package io.oltre_backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
    User findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByCalendarToken(String calendarToken);

}