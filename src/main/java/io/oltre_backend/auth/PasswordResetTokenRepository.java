package io.oltre_backend.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Transactional
    void deleteByUserId(Long userId);

    // Suppression atomique = usage unique : renvoie 0 si le jeton est inconnu, expiré ou déjà consommé.
    @Transactional
    @Modifying
    @Query("delete from PasswordResetToken t where t.tokenHash = :hash and t.expiresAt > :now")
    int consume(@Param("hash") String hash, @Param("now") LocalDateTime now);

    @Query("select t.userId from PasswordResetToken t where t.tokenHash = :hash and t.expiresAt > :now")
    Optional<Long> findUserId(@Param("hash") String hash, @Param("now") LocalDateTime now);
}
