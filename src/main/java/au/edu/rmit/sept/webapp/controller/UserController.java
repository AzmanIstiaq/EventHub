package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/// Used to perform user based operations and analytics when needed.
/// CRUD operations for users
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    // Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get a single user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get events this user is attending
    @GetMapping("/{id}/attendingEvents")
    public List<Event> getAttendingEvents(@PathVariable int id) {
        return registrationRepository.findByStudent_UserId(id)
                .stream()
                .map(Registration::getEvent)
                .collect(Collectors.toList());
    }

    // Get events this user has organized
    @GetMapping("/{id}/organizedEvents")
    public ResponseEntity<Set<Event>> getOrganizedEvents(@PathVariable int id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getOrganisedEvents()))
                .orElse(ResponseEntity.notFound().build());
    }
}
