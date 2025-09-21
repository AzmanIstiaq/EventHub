package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
///  Used to create registration entries for specific events by student users
@RestController
@RequestMapping("/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    // Create a new registration
    @PostMapping
    public ResponseEntity<Registration> createRegistration(@RequestParam int userId,
                                                           @RequestParam int eventId
                                                           ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));

        Registration registration = new Registration();
        registration.setStudent(user);
        registration.setEvent(event);

        return ResponseEntity.ok(registrationRepository.save(registration));
    }

    // Get all registrations
    @GetMapping
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    // Get registrations for a specific event
    @GetMapping("/event/{eventId}")
    public String getRegistrationsByEvent(@PathVariable int eventId) {
        return "test";
    }

    // Get registrations for a specific user
    @GetMapping("/user/{userId}")
    public List<Registration> getRegistrationsByUser(@PathVariable int userId) {
        return registrationRepository.findByStudent_UserId(userId);
    }
}
