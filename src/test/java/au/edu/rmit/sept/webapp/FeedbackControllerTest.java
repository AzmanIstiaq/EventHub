package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.FeedbackController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { FeedbackController.class })
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false"
})
class FeedbackControllerTest {

    @Autowired MockMvc mvc;

    @MockBean FeedbackService feedbackService;
    @MockBean EventService eventService;
    @MockBean UserService userService;

    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("POST feedback submits and redirects")
    void submitFeedbackRedirects() throws Exception {
        long eventId = 9L;
        long userId = 3L;
        Event e = new Event(); e.setId(eventId);
        User u = new User(); u.setId(userId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.of(u));

        mvc.perform(post("/events/{eventId}/feedback", eventId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("rating", Integer.toString(4))
                        .param("comment", "Nice")
                        .param("userId", Long.toString(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/student/" + userId));

        verify(feedbackService).submitFeedback(u, e, 4, "Nice");
    }
}
