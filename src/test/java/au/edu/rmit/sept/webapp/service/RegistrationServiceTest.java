package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    CategoryRepository categoryRepository;

    private User createAndSaveUser(String name) {
        User user = new User(name, "email", "password", UserType.STUDENT);
        return userRepository.save(user);
    }

    private Event createAndSaveEvent(String title, String email) {
        User user = new User("organiser", email, "password", UserType.ORGANISER);
        userRepository.save(user);
        Category category = new Category("category");
        categoryRepository.save(category);
        Event event = new Event(title, "description", LocalDateTime.now(), "location", user, category);
        return eventRepository.save(event);
    }

    @Test
    @DisplayName("registerUserForEvent(): prevents duplicate registrations (negative)")
    void preventDuplicateRegistration() {
        User user = createAndSaveUser("Alice");
        Event event = createAndSaveEvent("Welcome Week", "email1");

        // First registration succeeds
        registrationService.registerUserForEvent(user, event);

        // Second should fail
        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserForEvent(user, event));
    }

    @Test
    @DisplayName("registerUserForEvent(): registers successfully (positive)")
    void registersSuccessfully() {
        User user = createAndSaveUser("Bob");
        Event event = createAndSaveEvent("Career Fair", "email2");

        Registration r = registrationService.registerUserForEvent(user, event);

        assertThat(r).isNotNull();
        assertThat(r.getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(r.getEvent().getEventId()).isEqualTo(event.getEventId());
        assertThat(registrationRepository.existsById(r.getRegistrationId())).isTrue();
    }

    @Test
    @DisplayName("deleteRegistrationForEvent(): removes registration")
    void testDeleteRegistration() {
        User user = createAndSaveUser("Charlie");
        Event event = createAndSaveEvent("Hackathon", "email3");

        // Register user
        Registration reg = registrationService.registerUserForEvent(user, event);
        assertThat(registrationRepository.existsById(reg.getRegistrationId())).isTrue();

        // Delete registration
        registrationService.deleteRegistrationForEvent(user.getUserId(), event.getEventId());

        // Verify removal
        assertThat(registrationRepository.existsById(reg.getRegistrationId())).isFalse();
    }
}
