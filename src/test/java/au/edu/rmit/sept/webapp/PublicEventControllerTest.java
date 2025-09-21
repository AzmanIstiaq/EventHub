package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.PublicEventController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { PublicEventController.class })
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false"
})
class PublicEventControllerTest {

    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean RegistrationService registrationService;
    @MockBean UserService userService;
    @MockBean CategoryService categoryService;

    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("Cancel RSVP: redirects and calls delete")
    void cancelRsvpRedirects() throws Exception {
        long eventId = 10L;
        long userId = 5L;

        mvc.perform(post("/events/student/cancel/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("userId", Long.toString(userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/student/" + userId));

        verify(registrationService).deleteRegistrationForEvent(userId, eventId);
    }

    @Test
    @DisplayName("Event detail (logged-in): OK with view and model")
    void eventDetailLoggedInOk() throws Exception {
        long eventId = 11L;
        long userId = 7L;

        Event e = new Event(); e.setId(eventId); e.setTitle("Tech Talk"); e.setDateTime(LocalDateTime.now().plusDays(4));
        User u = new User(); u.setId(userId); u.setName("Pat");
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(userId)).thenReturn(Optional.of(u));

        mvc.perform(get("/events/student/detail/{eventId}", eventId)
                        .param("userId", Long.toString(userId)))
                .andExpect(status().isOk())
                .andExpect(view().name("event-detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @DisplayName("Event detail (public): OK with view")
    void eventDetailPublicOk() throws Exception {
        long eventId = 12L;
        Event e = new Event(); e.setId(eventId); e.setTitle("Open Day"); e.setDateTime(LocalDateTime.now().plusDays(8));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));

        mvc.perform(get("/events/student/public/detail/{eventId}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("event-detail"))
                .andExpect(model().attributeExists("event"));
    }

    @Test
    @DisplayName("Search with category and dates -> OK")
    void searchWithCategoryAndDatesOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/student/search")
                        .param("query", "hack")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(30).toString())
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
        verify(eventService).searchEvents(any(), any(), any(), any());
    }

    @Test
    @DisplayName("List events for logged-in user -> OK with model")
    void listEventsLoggedInOk() throws Exception {
        long userId = 9L;
        Event e = new Event(); e.setId(30L); e.setTitle("Seminar"); e.setDateTime(LocalDateTime.now().plusDays(1));
        User u = new User(); u.setId(userId); u.setName("Taylor");
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of(e));
        when(eventService.getPastEvents()).thenReturn(List.of());
        when(userService.findById(userId)).thenReturn(Optional.of(u));
        when(categoryService.findAll()).thenReturn(List.of());

        mvc.perform(get("/events/student/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("pastEvents"));
    }
}
