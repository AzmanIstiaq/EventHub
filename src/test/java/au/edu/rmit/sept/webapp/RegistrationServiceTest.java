package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class RegistrationServiceTest {

    private RegistrationRepository registrationRepository;
    private RegistrationService registrationService;

    @BeforeEach
    void setup() {
        registrationRepository = mock(RegistrationRepository.class);
        registrationService = new RegistrationService(registrationRepository);
    }

    @Test
    @DisplayName("registerUserForEvent(): prevents duplicate registrations (negative)")
    void preventDuplicateRegistration() {
        User user = new User(); user.setUserId(1L);
        Event event = new Event(); event.setEventId(10L);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserForEvent(user, event));
    }

    @Test
    @DisplayName("registerUserForEvent(): registers successfully (positive)")
    void registersSuccessfully() {
        User user = new User(); user.setUserId(2L);
        Event event = new Event(); event.setEventId(20L);

        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Registration r = registrationService.registerUserForEvent(user, event);

        assertThat(r.getUser()).isEqualTo(user);
        assertThat(r.getEvent()).isEqualTo(event);
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    @Transactional
    public void testDeleteRegistration() {
        // Create user and event
        User user = new User();
        user.setUserId(1L); // set manually for test
        Event event = new Event();
        event.setEventId(1L);

        // Register user
        Registration reg = registrationService.registerUserForEvent(user, event);
        assertThat(registrationRepository.existsById(reg.getRegistrationId())).isTrue();

        // Delete registration
        registrationService.deleteRegistrationForEvent(user.getUserId(), event.getEventId());

        // Check deletion
        assertThat(registrationRepository.existsById(reg.getRegistrationId())).isFalse();
    }

}