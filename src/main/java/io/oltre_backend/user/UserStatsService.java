package io.oltre_backend.user;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.expenses.ExpensesRepository;
import io.oltre_backend.sport.GarminActivityDTO;
import io.oltre_backend.sport.GarminAuthException;
import io.oltre_backend.sport.GarminService;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.tasks.task_enum.TasksStatus;

@Service
public class UserStatsService {

    private final ExpensesRepository expensesRepository;
    private final TasksRepository tasksRepository;
    private final GarminService garminService;

    public UserStatsService(ExpensesRepository expensesRepository, TasksRepository tasksRepository,
                             GarminService garminService) {
        this.expensesRepository = expensesRepository;
        this.tasksRepository = tasksRepository;
        this.garminService = garminService;
    }

    public UserStatsDTO getStats(Long userId) {
        LocalDate today = LocalDate.now();

        int expensesThisMonth = expensesRepository.findByUser_Id(userId).stream()
                .filter(e -> e.getStartDate() != null
                        && e.getStartDate().getMonth() == today.getMonth()
                        && e.getStartDate().getYear() == today.getYear())
                .mapToInt(Expenses::getAmount)
                .sum();

        int tasksInProgress = (int) tasksRepository.findByUser_Id(userId).stream()
                .filter(t -> t.getTasksStatus() == TasksStatus.PENDING)
                .count();

        int sportSessions = 0;
        double sportDistanceKm = 0;
        try {
            LocalDate since = today.minusDays(30);
            List<GarminActivityDTO> activities = garminService.getActivities(userId, since.toString(), 200);
            sportSessions = activities.size();
            sportDistanceKm = activities.stream()
                    .mapToDouble(a -> a.getDistance() != null ? a.getDistance() : 0)
                    .sum() / 1000.0;
        } catch (GarminAuthException e) {
            // User hasn't connected a Garmin account - sport stats stay at zero.
        }

        return new UserStatsDTO(sportSessions, sportDistanceKm, expensesThisMonth, tasksInProgress);
    }
}
