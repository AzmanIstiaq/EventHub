package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RegistrationService {

    private final RegistrationRepository registrationRepository;

    public RegistrationService(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    // Register a user for an event
    @Transactional
    public Registration registerUserForEvent(User user, Event event) {
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("User already registered for this event");
        }

        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);

        return registrationRepository.save(registration);
    }

    // Find registrations by user ID
    public List<Registration> findByUserId(long id) {
        return registrationRepository.findByUser_UserId(id);
    }


    public List<Registration> getRegistrationsForEvent(Event event) {
        return registrationRepository.findByEvent(event);
    }

    // Delete a registration for a user and event
    @Transactional
    public void deleteRegistrationForEvent(Long userId, Long eventId) {
        int deleted = registrationRepository.deleteRegistration(userId, eventId);
        if (deleted == 0) {
            throw new IllegalArgumentException(
                    "No registration found for userId=" + userId + " and eventId=" + eventId
            );
        }
        // Clear persistence context to ensure entity is removed immediately
        registrationRepository.flush();
    }

    // Check if a user is registered for an event
    public boolean isUserRegistered(Long eventId, Long userId) {
        return registrationRepository.existsByEvent_EventIdAndUser_UserId(eventId, userId);
    }
}
