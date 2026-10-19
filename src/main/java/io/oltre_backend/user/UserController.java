package io.oltre_backend.user;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/users")
public class UserController{

    private final UserService userService;
    private final UserStatsService userStatsService;


    UserController(UserService userService, UserStatsService userStatsService) {
        this.userService = userService;
        this.userStatsService = userStatsService;
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        return userService.getUserById(CurrentUser.id());
    }

    @GetMapping("/me/stats")
    public UserStatsDTO getCurrentUserStats() {
        return userStatsService.getStats(CurrentUser.id());
    }

    @PutMapping("/me")
    public UserDTO updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(CurrentUser.id(), request.username(), request.age(), request.birthDate());
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(CurrentUser.id(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount(CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    record UpdateProfileRequest(
            @Size(max = 255, message = "Nom d'utilisateur trop long") String username,
            Integer age, LocalDate birthDate) {}
    record ChangePasswordRequest(String currentPassword, String newPassword) {}
}
