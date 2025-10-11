package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.BanRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BanService banService;

    public UserService(UserRepository userRepository, BanService banService) {
        this.userRepository = userRepository;
        this.banService = banService;
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // Get all users (for admin view)
    public List<User> getAllUsers() {
        // ensure expired bans are processed before returning users
        banService.expireTemporaryBans();
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }
}