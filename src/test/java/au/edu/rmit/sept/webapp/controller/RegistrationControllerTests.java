package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.TestHelpers.WithMockCustomUser;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.*;
import org.hibernate.validator.constraints.ModCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.config.http.MatcherType.mvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RegistrationController.class)
@AutoConfigureMockMvc
class RegistrationControllerTests {
    @Autowired
    MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean RegistrationService registrationService;
    @MockBean UserService userService;
    @MockBean CategoryService categoryService;
    @MockBean KeywordService keywordService;
    @MockBean FeedbackService feedbackService;

    @MockBean private UserRepository userRepo;
    @MockBean private EventRepository eventRepo;
    @MockBean private RegistrationRepository registrationRepo;
    @MockBean private CategoryRepository categoryRepo;
    @MockBean private KeywordRepository keywordRepo;

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Test creating a registration for an event")
    void createRegistration() throws Exception {
        long studentId = 100L;
        long eventId = 9L;

        Event e = new Event();
        e.setEventId(eventId);
        User u = new User();
        u.setUserId(studentId);

        when(eventRepo.findById(eventId)).thenReturn(Optional.of(e));
        when(userRepo.findById(studentId)).thenReturn(Optional.of(u));

        // Mock save to return the same object
        when(registrationRepo.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mvc.perform(post("/registrations")
                        .with(csrf())
                        .param("userId", String.valueOf(studentId))
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isOk());

        // Verify save called once with correct fields
        verify(registrationRepo).save(argThat(r ->
                Objects.equals(r.getUser(), u) &&
                        Objects.equals(r.getEvent(), e)
        ));
    }

}
