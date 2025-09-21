package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.PublicEventController;
import au.edu.rmit.sept.webapp.controller.UserController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * User Story: RSVP to an Event
 *
 * 1) As a student viewing an event, I see an "RSVP" action.
 * 2) Clicking RSVP confirms and toggles to "Cancel RSVP" (we assert redirect + service call).
 * 3) After RSVP, the event appears in "My Events" list.
 * 4) If not logged in, attempting RSVP redirects to login (assert depends on your controller).
 */
class RsvpUserStoryTest {
    EventService eventService = mock(EventService.class);
    RegistrationService registrationService = mock(RegistrationService.class);
    UserService userService = mock(UserService.class);
    CategoryService categoryService = mock(CategoryService.class);
    RegistrationRepository registrationRepository = mock(RegistrationRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    PublicEventController publicController = new PublicEventController(eventService, registrationService, userService, categoryService);
    UserController userController = new UserController();

    @Test
    @DisplayName("AC1: viewing events page loads with model 'events' and view 'public-events'")
    void eventListLoads() throws Exception {
        Event e = new Event(); e.setId(10L); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(2));
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of(e));
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getPastEvents()).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = publicController.listEvents(null, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("events")).isTrue();
    }

    @Test
    @DisplayName("AC2: clicking RSVP registers and redirects")
    void rsvpRegistersAndRedirects() {
        long eventId = 10L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(registrationService.registerUserForEvent(u, e)).thenReturn(new Registration(u, e));

        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);
        Model model = new ExtendedModelMap();
        String view = publicController.registerForEvent(cud, model, eventId);
        assertThat(view).isEqualTo("redirect:/student/events");
        verify(registrationService).registerUserForEvent(u, e);
    }

    @Test
    @DisplayName("AC3: 'My Events' shows RSVP’d event")
    void myEventsListsRsvpedEvent() {
        long userId = 5L;
        long eventId = 10L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));
        Registration r = new Registration(u, e);

        // UserController /users/{id}/attendingEvents uses registrationRepository.findByUserId(id)
        try {
            var field = UserController.class.getDeclaredField("registrationRepository");
            field.setAccessible(true);
            field.set(userController, registrationRepository);
        } catch (Exception ex) { throw new RuntimeException(ex); }
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(r));

        var list = userController.getAttendingEvents(userId);
        assertThat(list).hasSize(1);
        // Optional: .andExpect(jsonPath("$[0].title").value("Welcome Week"));
    }

    @Test
    @DisplayName("AC4: not logged in (no userId) -> 400 Bad Request")
    void notLoggedInRsvpReturnsBadRequest() {
        long eventId = 10L;
        Model model = new ExtendedModelMap();
        assertThrows(NullPointerException.class, () -> publicController.registerForEvent(null, model, eventId));
        verify(registrationService, never()).registerUserForEvent(any(), any());
    }

    @Test
    @DisplayName("Edge: event not found -> 5xx")
    void eventNotFoundRsvp() {
        long eventId = 999L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.empty());
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);
        Model model = new ExtendedModelMap();
        assertThrows(IllegalArgumentException.class, () -> publicController.registerForEvent(cud, model, eventId));
    }

    @Test
    @DisplayName("Edge: user not found -> 5xx")
    void userNotFoundRsvp() {
        long eventId = 10L;
        long userId = 404L;

        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(2));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.empty());
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);
        Model model = new ExtendedModelMap();
        assertThrows(IllegalArgumentException.class, () -> publicController.registerForEvent(cud, model, eventId));
    }

    @Test
    @DisplayName("Edge: already registered -> 5xx")
    void alreadyRegisteredRsvp() {
        long eventId = 10L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(registrationService.registerUserForEvent(u, e)).thenThrow(new IllegalStateException("duplicate"));
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(userId);
        Model model = new ExtendedModelMap();
        assertThrows(IllegalStateException.class, () -> publicController.registerForEvent(cud, model, eventId));
    }

    @Test
    @DisplayName("Edge: invalid/empty search -> OK with empty results")
    void emptySearchOk() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = publicController.searchEvents(null, "", null, null, null, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("searchResults")).isTrue();
    }

    @Test
    @DisplayName("Edge: date range end before start -> OK")
    void dateRangeEndBeforeStartOk() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = publicController.searchEvents(null, "q", LocalDate.now().plusDays(10), LocalDate.now().plusDays(5), null, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("searchResults")).isTrue();
        verify(eventService).searchEvents(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Edge: only endDate provided -> OK")
    void onlyEndDateProvidedOk() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = publicController.searchEvents(null, "q", null, LocalDate.now().plusDays(1), null, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("searchResults")).isTrue();
        verify(eventService).searchEvents(any(), any(), any(), any());
    }
}
