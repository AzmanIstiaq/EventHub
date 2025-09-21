package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.FeedbackController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.FeedbackService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class FeedbackControllerTest {

    FeedbackService feedbackService = mock(FeedbackService.class);
    EventService eventService = mock(EventService.class);
    UserService userService = mock(UserService.class);
    FeedbackController controller = new FeedbackController(eventService, userService);

    @Test
    @DisplayName("POST feedback submits and redirects")
    void submitFeedbackRedirects() {
        long eventId = 9L;
        long userId = 3L;
        Event e = new Event(); e.setId(eventId);
        User u = new User(); u.setId(userId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.of(u));

        // Inject feedbackService via reflection
        try {
            var field = FeedbackController.class.getDeclaredField("feedbackService");
            field.setAccessible(true);
            field.set(controller, feedbackService);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        String view = controller.submitFeedback(eventId, 4, "Nice", userId);
        assertThat(view).isEqualTo("redirect:/student/events");
        verify(feedbackService).submitFeedback(u, e, 4, "Nice");
    }
}
