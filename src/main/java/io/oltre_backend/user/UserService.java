package io.oltre_backend.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    public final UserRepository userRepository;

    private UserDTO toDto(User user) {
            UserDTO dto = new UserDTO(user.getUsername(), user.getBirthDate(), user.getAge());
            return dto;
        }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
