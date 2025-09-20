package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    // Register a user for an event
    public Registration registerUserForEvent(User user, Event event) {
        // Prevent duplicate registration
        boolean alreadyRegistered = registrationRepository.existsByUserAndEvent(user, event);
        if (alreadyRegistered) {
            throw new IllegalStateException("User already registered for this event");
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);

        return registrationRepository.save(registration);
    }

    // Get all registrations for a user
    public List<Registration> getRegistrationsForUser(User user) {
        return registrationRepository.findByUser(user);
    }

    // Get all registrations for an event
    public List<Registration> getRegistrationsForEvent(Event event) {
        return registrationRepository.findByEvent(event);
    }

    public void deleteRegistrationForEvent(Long userId, Long eventId) {
        registrationRepository.deleteByUserIdAndEventId(userId, eventId);
    }
}

