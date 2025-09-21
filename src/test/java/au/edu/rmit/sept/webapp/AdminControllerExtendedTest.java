package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.AdminController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false"
})
class AdminControllerExtendedTest {

    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean UserService userService;
    @MockBean RegistrationService registrationService;

    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("Admin view all events returns OK and model attributes present")
    void adminViewAllEvents() throws Exception {
        Event past = new Event(); past.setDateTime(LocalDateTime.now().minusDays(1));
        Event upcoming = new Event(); upcoming.setDateTime(LocalDateTime.now().plusDays(1));
        when(eventService.getAllEvents()).thenReturn(List.of(past, upcoming));

        mvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("upcomingEventsCount"))
                .andExpect(model().attributeExists("pastEventsCount"));
    }

    @Test
    @DisplayName("Admin delete event success redirects")
    void adminDeleteEventSuccess() throws Exception {
        Event e = new Event(); e.setId(100L); e.setTitle("Title");
        when(eventService.findById(100L)).thenReturn(Optional.of(e));

        mvc.perform(post("/admin/events/{eventId}/delete", 100L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));
        verify(eventService).delete(100L);
    }

    @Test
    @DisplayName("Admin delete event invalid id still redirects")
    void adminDeleteEventInvalid() throws Exception {
        when(eventService.findById(anyLong())).thenReturn(Optional.empty());

        mvc.perform(post("/admin/events/{eventId}/delete", 999L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events"));
    }

    @Test
    @DisplayName("Admin view all users returns OK and model attributes present")
    void adminViewAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(new User(), new User()));

        mvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("adminCount"))
                .andExpect(model().attributeExists("organiserCount"))
                .andExpect(model().attributeExists("studentCount"));
    }

    @Test
    @DisplayName("Admin deactivate user success redirects")
    void adminDeactivateUser() throws Exception {
        User u = new User(); u.setId(44L); u.setName("X");
        when(userService.findById(44L)).thenReturn(Optional.of(u));

        mvc.perform(post("/admin/users/{userId}/deactivate", 44L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    @DisplayName("Admin view event detail shows model")
    void adminViewEventDetail() throws Exception {
        Event e = new Event(); e.setId(77L);
        when(eventService.findById(77L)).thenReturn(Optional.of(e));
        when(registrationService.getRegistrationsForEvent(e)).thenReturn(List.of());

        mvc.perform(get("/admin/events/{eventId}", 77L))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("registrations"))
                .andExpect(model().attributeExists("registrationCount"));
    }
}
