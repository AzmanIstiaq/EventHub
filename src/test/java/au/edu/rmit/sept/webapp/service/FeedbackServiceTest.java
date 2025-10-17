package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Feedback;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.FeedbackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class FeedbackServiceTest {

    private FeedbackRepository feedbackRepository;
    private FeedbackService feedbackService;

    @BeforeEach
    void setup() {
        feedbackRepository = mock(FeedbackRepository.class);
        feedbackService = new FeedbackService();
        // Inject mocked repo via reflection since the field is @Autowired
        try {
            var field = FeedbackService.class.getDeclaredField("feedbackRepository");
            field.setAccessible(true);
            field.set(feedbackService, feedbackRepository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("submitFeedback(): prevents duplicate feedback (negative)")
    void preventsDuplicateFeedback() {
        User user = new User(); user.setUserId(1L);
        Event event = new Event(); event.setEventId(10L);

        when(feedbackRepository.findByEventAndUser(event, user)).thenReturn(Optional.of(new Feedback()));

        assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(user, event, 5, "Great!"));
    }

    @Test
    @DisplayName("submitFeedback(): submits successfully (positive)")
    void submitsSuccessfully() {
        User user = new User(); user.setUserId(2L);
        Event event = new Event(); event.setEventId(20L);

        when(feedbackRepository.findByEventAndUser(event, user)).thenReturn(Optional.empty());
        when(feedbackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Feedback saved = feedbackService.submitFeedback(user, event, 4, "Nice event");

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getEvent()).isEqualTo(event);
        assertThat(saved.getRating()).isEqualTo(4);
        assertThat(saved.getComment()).isEqualTo("Nice event");
        verify(feedbackRepository).save(any(Feedback.class));
    }
}
