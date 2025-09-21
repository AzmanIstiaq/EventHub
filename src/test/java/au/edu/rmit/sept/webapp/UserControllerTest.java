package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.UserController;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UserControllerTest {
    UserRepository userRepository = mock(UserRepository.class);
    RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
    UserController controller = new UserController();

    @Test
    @DisplayName("GET /users/{id}: returns user details (positive)")
    void getUserById() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setName("Alice");
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        // Inject repository via reflection
        try {
            var f = UserController.class.getDeclaredField("userRepository");
            f.setAccessible(true);
            f.set(controller, userRepository);
        } catch (Exception e) { throw new RuntimeException(e); }

        ResponseEntity<User> resp = controller.getUserById(5L);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
    }
}
