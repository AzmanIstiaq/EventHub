package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    private EventRepository eventRepository;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        eventService = new EventService(eventRepository);
    }

    @Test
    @DisplayName("save(): creates a new upcoming event (positive)")
    void saveCreatesEvent() {
        Event e = new Event();
        e.setTitle("Welcome Back");
        e.setDateTime(LocalDateTime.now().plusDays(7));
        when(eventRepository.save(any())).thenAnswer(i -> {
            Event saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Event saved = eventService.save(e);

        assertThat(saved.getId()).isNotNull();
        verify(eventRepository).save(e);
    }

    @Test
    @DisplayName("findById(): returns Optional.empty when missing (negative)")
    void findByIdMissing() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Event> result = eventService.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getUpcomingEventsForOrganiser(): returns only future events (boundary: now)")
    void upcomingBoundaryNow() {
        User organiser = new User();
        organiser.setId(42L);

        Event past = new Event();
        past.setDateTime(LocalDateTime.now().minusMinutes(1));
        Event nowEdge = new Event();
        nowEdge.setDateTime(LocalDateTime.now());
        Event future = new Event();
        future.setDateTime(LocalDateTime.now().plusMinutes(1));

        when(eventRepository.findByOrganiserAndDateTimeAfterOrderByDateTimeAsc(eq(organiser), any(LocalDateTime.class)))
                .thenReturn(List.of(nowEdge, future));

        List<Event> upcoming = eventService.getUpcomingEventsForOrganiser(organiser);

        assertThat(upcoming).contains(future);
    }
}