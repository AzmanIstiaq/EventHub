package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    // Get all users (for admin view)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get users by type
    public List<User> getUsersByType(UserType userType) {
        return userRepository.findByType(userType);
    }

    // Get all organizers
    public List<User> getAllOrganizers() {
        return userRepository.findByType(UserType.ORGANISER);
    }

    // Get all students
    public List<User> getAllStudents() {
        return userRepository.findByType(UserType.STUDENT);
    }

    // Get all admins
    public List<User> getAllAdmins() {
        return userRepository.findByType(UserType.ADMIN);
    }

    // Delete user (for admin functionality)
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Search users by name
    public List<User> searchUsersByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    // Find user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}