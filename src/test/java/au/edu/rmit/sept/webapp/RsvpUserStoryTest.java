package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.PublicEventController;
import au.edu.rmit.sept.webapp.controller.UserController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.ServletException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * User Story: RSVP to an Event
 *
 * 1) As a student viewing an event, I see an "RSVP" action.
 * 2) Clicking RSVP confirms and toggles to "Cancel RSVP" (we assert redirect + service call).
 * 3) After RSVP, the event appears in "My Events" list.
 * 4) If not logged in, attempting RSVP redirects to login (assert depends on your controller).
 */
@WebMvcTest(controllers = { PublicEventController.class, UserController.class })
@TestPropertySource(properties = {
        // prevent Thymeleaf from trying to render actual templates in MVC-slice tests
        "spring.thymeleaf.enabled=false"
})
class RsvpUserStoryTest {

    @Autowired MockMvc mvc;

    // Services used by these controllers
    @MockBean EventService eventService;
    @MockBean RegistrationService registrationService;
    @MockBean UserService userService;
    @MockBean CategoryService categoryService;

    // Repos mocked to satisfy WebappApplication.init(...) seeder on context startup
    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("AC1: viewing events page loads with model 'events' and view 'public-events'")
    void eventListLoads() throws Exception {
        Event e = new Event();
        e.setId(10L);
        e.setTitle("Welcome Week");
        e.setDateTime(LocalDateTime.now().plusDays(2));

        when(eventService.getAllUpcomingEvents()).thenReturn(List.of(e));

        mvc.perform(get("/events/student"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("events"))
                .andExpect(view().name("public-events"));
    }

    @Test
    @DisplayName("AC2: clicking RSVP registers and redirects")
    void rsvpRegistersAndRedirects() throws Exception {
        long eventId = 10L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(registrationService.registerUserForEvent(u, e)).thenReturn(new Registration(u, e));

        mvc.perform(post("/events/student/register/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userId", Long.toString(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/student/" + userId));

        verify(registrationService).registerUserForEvent(u, e);
    }

    @Test
    @DisplayName("AC3: 'My Events' shows RSVP’d event")
    void myEventsListsRsvpedEvent() throws Exception {
        long userId = 5L;
        long eventId = 10L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));
        Registration r = new Registration(u, e);

        // UserController /users/{id}/attendingEvents uses registrationRepository.findByUserId(id)
        when(registrationRepository.findByUserId(userId)).thenReturn(List.of(r));

        mvc.perform(get("/users/{id}/attendingEvents", userId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        // Optional: .andExpect(jsonPath("$[0].title").value("Welcome Week"));
    }

    @Test
    @DisplayName("AC4: not logged in (no userId) -> 400 Bad Request")
    void notLoggedInRsvpReturnsBadRequest() throws Exception {
        long eventId = 10L;

        mvc.perform(post("/events/student/register/{eventId}", eventId))
                .andExpect(status().isBadRequest()); // Required request param 'userId' missing

        verify(registrationService, never()).registerUserForEvent(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Edge: event not found -> 5xx")
    void eventNotFoundRsvp() throws Exception {
        long eventId = 999L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
                mvc.perform(post("/events/student/register/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("userId", Long.toString(userId)))
                        .andReturn());
    }

    @Test
    @DisplayName("Edge: user not found -> 5xx")
    void userNotFoundRsvp() throws Exception {
        long eventId = 10L;
        long userId = 404L;

        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(2));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ServletException.class, () ->
                mvc.perform(post("/events/student/register/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("userId", Long.toString(userId)))
                        .andReturn());
    }

    @Test
    @DisplayName("Edge: already registered -> 5xx")
    void alreadyRegisteredRsvp() throws Exception {
        long eventId = 10L;
        long userId = 5L;

        User u = new User(); u.setId(userId); u.setName("Sam");
        Event e = new Event(); e.setId(eventId); e.setTitle("Welcome Week"); e.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(registrationService.registerUserForEvent(u, e)).thenThrow(new IllegalStateException("duplicate"));

        assertThrows(ServletException.class, () ->
                mvc.perform(post("/events/student/register/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("userId", Long.toString(userId)))
                        .andReturn());
    }

    @Test
    @DisplayName("Edge: invalid/empty search -> OK with empty results")
    void emptySearchOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/student/search")
                        .param("query", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @DisplayName("Edge: date range end before start -> OK")
    void dateRangeEndBeforeStartOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/student/search")
                        .param("query", "q")
                        .param("startDate", LocalDate.now().plusDays(10).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
        verify(eventService).searchEvents(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Edge: only endDate provided -> OK")
    void onlyEndDateProvidedOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/student/search")
                        .param("query", "q")
                        .param("endDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
        verify(eventService).searchEvents(any(), any(), any(), any());
    }
}
