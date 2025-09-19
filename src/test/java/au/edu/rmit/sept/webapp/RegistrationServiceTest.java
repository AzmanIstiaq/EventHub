package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        User user = new User(); user.setId(1L);
        Event event = new Event(); event.setId(10L);
        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> registrationService.registerUserForEvent(user, event));
    }

    @Test
    @DisplayName("registerUserForEvent(): registers successfully (positive)")
    void registersSuccessfully() {
        User user = new User(); user.setId(2L);
        Event event = new Event(); event.setId(20L);

        when(registrationRepository.existsByUserAndEvent(user, event)).thenReturn(false);
        when(registrationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Registration r = registrationService.registerUserForEvent(user, event);

        assertThat(r.getUser()).isEqualTo(user);
        assertThat(r.getEvent()).isEqualTo(event);
        verify(registrationRepository).save(any(Registration.class));
    }
}