package io.oltre_backend.user;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.coach.CoachPlanRepository;
import io.oltre_backend.coach.SportGoalRepository;
import io.oltre_backend.expenses.ExpensesRepository;
import io.oltre_backend.meal.MealRepository;
import io.oltre_backend.sport.GarminService;
import io.oltre_backend.tasks.TasksRepository;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GarminService garminService;
    private final CoachPlanRepository coachPlanRepository;
    private final SportGoalRepository sportGoalRepository;
    private final TasksRepository tasksRepository;
    private final ExpensesRepository expensesRepository;
    private final MealRepository mealRepository;
    private final appointmentRepository appointmentRepository;

    public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        GarminService garminService,
                        CoachPlanRepository coachPlanRepository,
                        SportGoalRepository sportGoalRepository,
                        TasksRepository tasksRepository,
                        ExpensesRepository expensesRepository,
                        MealRepository mealRepository,
                        appointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.garminService = garminService;
        this.coachPlanRepository = coachPlanRepository;
        this.sportGoalRepository = sportGoalRepository;
        this.tasksRepository = tasksRepository;
        this.expensesRepository = expensesRepository;
        this.mealRepository = mealRepository;
        this.appointmentRepository = appointmentRepository;
    }

    private UserDTO toDto(User user) {
        return new UserDTO(user.getUsername(), user.getBirthDate(), user.getAge());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        return toDto(user);
    }

    public Iterable<User> getUsers(){
        return userRepository.findAll();
    }

    public void deleteUser(final Long id){
        userRepository.deleteById(id);
    }

    public User saveUser(User user){
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    public UserDTO updateProfile(Long id, String username, Integer age, LocalDate birthDate) {
        User user = userRepository.findById(id).orElseThrow();
        if (username != null && !username.isBlank()) {
            user.setUsername(username);
        }
        user.setAge(age);
        user.setBirthDate(birthDate);
        return toDto(userRepository.save(user));
    }

    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id).orElseThrow();
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Mot de passe actuel incorrect.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidPasswordException("Le nouveau mot de passe doit contenir au moins 8 caractères.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Supprime toutes les données rattachées à l'utilisateur avant le compte
    // lui-même (FK sur user_id sur chacune de ces tables - les tâches sont
    // déjà cascadées par JPA mais la suppression explicite ici reste
    // correcte et rend l'intention claire indépendamment du mapping de
    // chaque entité). La déconnexion Garmin est best-effort : une panne du
    // sidecar ne doit pas empêcher de supprimer le compte Oltre.
    @Transactional
    public void deleteAccount(Long id) {
        try {
            garminService.disconnect(id);
        } catch (Exception e) {
            log.warn("Échec de la déconnexion Garmin pendant la suppression du compte {} - on continue quand même", id, e);
        }
        coachPlanRepository.deleteByUser_Id(id);
        sportGoalRepository.deleteByUser_Id(id);
        tasksRepository.deleteByUser_Id(id);
        expensesRepository.deleteByUser_Id(id);
        mealRepository.deleteByUser_Id(id);
        appointmentRepository.deleteByUser_Id(id);
        userRepository.deleteById(id);
    }
}
