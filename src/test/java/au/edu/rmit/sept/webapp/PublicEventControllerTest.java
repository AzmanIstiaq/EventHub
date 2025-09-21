package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.PublicEventController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PublicEventControllerTest {

    EventService eventService = mock(EventService.class);
    RegistrationService registrationService = mock(RegistrationService.class);
    UserService userService = mock(UserService.class);
    CategoryService categoryService = mock(CategoryService.class);
    PublicEventController controller = new PublicEventController(eventService, registrationService, userService, categoryService);

    @Test
    @DisplayName("Cancel RSVP: redirects and calls delete")
    void cancelRsvpRedirects() {
        long eventId = 10L;
        long userId = 5L;

        Model model = new ExtendedModelMap();
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);

        String view = controller.cancelEventRegistration(cud, model, eventId);
        assertThat(view).isEqualTo("redirect:/student/events");
        verify(registrationService).deleteRegistrationForEvent(userId, eventId);
    }

    @Test
    @DisplayName("Event detail (logged-in): OK with view and model")
    void eventDetailLoggedInOk() {
        long eventId = 11L;
        long userId = 7L;

        Event e = new Event(); e.setId(eventId); e.setTitle("Tech Talk"); e.setDateTime(LocalDateTime.now().plusDays(4));
        User u = new User(); u.setId(userId); u.setName("Pat");
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.of(u));

        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);

        Model model = new ExtendedModelMap();
        String view = controller.getEventDetailLoggedIn(cud, model, eventId);
        assertThat(view).isEqualTo("event-detail");
        assertThat(model.containsAttribute("event")).isTrue();
        assertThat(model.containsAttribute("currentUser")).isTrue();
    }

    @Test
    @DisplayName("Event detail (public): OK with view")
    void eventDetailPublicOk() {
        long eventId = 12L;
        Event e = new Event(); e.setId(eventId); e.setTitle("Open Day"); e.setDateTime(LocalDateTime.now().plusDays(8));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));

        Model model = new ExtendedModelMap();
        String view = controller.getEventDetail(model, eventId);
        assertThat(view).isEqualTo("event-detail");
        assertThat(model.containsAttribute("event")).isTrue();
    }

    @Test
    @DisplayName("Search with category and dates -> OK")
    void searchWithCategoryAndDatesOk() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.searchEvents(null, "hack", LocalDate.now(), LocalDate.now().plusDays(30), 1L, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("searchResults")).isTrue();
        verify(eventService).searchEvents(any(), any(), any(), any());
    }

    @Test
    @DisplayName("List events for logged-in user -> OK with model")
    void listEventsLoggedInOk() {
        long userId = 9L;
        Event e = new Event(); e.setId(30L); e.setTitle("Seminar"); e.setDateTime(LocalDateTime.now().plusDays(1));
        User u = new User(); u.setId(userId); u.setName("Taylor");
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of(e));
        when(eventService.getPastEvents()).thenReturn(List.of());
        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(categoryService.findAll()).thenReturn(List.of());

        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);

        Model model = new ExtendedModelMap();
        String view = controller.listEventsLoggedIn(cud, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("events")).isTrue();
        assertThat(model.containsAttribute("categories")).isTrue();
        assertThat(model.containsAttribute("pastEvents")).isTrue();
    }
}
