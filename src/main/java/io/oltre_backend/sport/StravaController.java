package io.oltre_backend.sport;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/strava")
@CrossOrigin(origins = "http://localhost:4200")
public class StravaController {

    private final StravaService stravaService;

    public StravaController(StravaService stravaService) {
        this.stravaService = stravaService;
    }

    @GetMapping("/activities")
    public List<StravaActivityDTO> getActivities(
            @RequestParam(required = false) String monday,
            @RequestParam(defaultValue = "50") int limit) {
        return stravaService.getActivities(monday, limit);
    }
}
