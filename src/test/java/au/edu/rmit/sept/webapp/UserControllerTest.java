package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.UserController;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;

    // Controller collaborators
    @MockBean UserRepository userRepository;
    @MockBean RegistrationRepository registrationRepository;

    // Needed to satisfy WebappApplication.init(...) seeder during context start
    @MockBean EventRepository eventRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("GET /users/{id}: returns user details (positive)")
    void getUserById() throws Exception {
        User u = new User();
        u.setId(5L);
        u.setName("Alice");

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        mvc.perform(get("/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

    }
}
