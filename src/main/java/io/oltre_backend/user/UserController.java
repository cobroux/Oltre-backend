package io.oltre_backend.user;

import io.oltre_backend.auth.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}