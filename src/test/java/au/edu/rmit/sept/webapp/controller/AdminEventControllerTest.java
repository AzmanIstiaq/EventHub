package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.TestHelpers.WithMockCustomUser;
import au.edu.rmit.sept.webapp.model.*;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.*;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminEventController.class)
@AutoConfigureMockMvc
class AdminEventControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean private UserRepository userRepository;
    @MockBean private EventService eventService;
    @MockBean private UserService userService;
    @MockBean private RegistrationService registrationService;
    @MockBean private CategoryService categoryService;
    @MockBean private KeywordService keywordService;
    @MockBean private EventRepository eventRepository;
    @MockBean private CategoryRepository categoryRepository;
    @MockBean private KeywordRepository keywordRepository;
    @MockBean private AuditLogService auditLogService;
    @MockBean private RegistrationRepository registrationRepository;

    private User mockAdmin() {
        User u = new User();
        u.setUserId(1L);
        u.setRole(UserType.ADMIN);
        return u;
    }

    private Event mockEvent(long id) {
        Event event = new Event();
        event.setEventId(id);
        event.setTitle("Sample Event");
        event.setDateTime(java.time.LocalDateTime.now().plusDays(1));
        User organiser = new User();
        organiser.setUserId(99L);
        organiser.setName("Organiser");
        event.setOrganiser(organiser);
        return event;
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can open event edit page successfully")
    void showAdminEditForm() throws Exception {
        long eventId = 1L;
        Event event = mockEvent(eventId);

        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(categoryService.findAll()).thenReturn(List.of());
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        mockMvc.perform(get("/admin/events/{id}/edit", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-event-edit"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can access event edit form")
    void adminCanAccessEditForm() throws Exception {
        long eventId = 1L;
        Event event = mockEvent(eventId);

        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        mockMvc.perform(get("/admin/events/{id}/edit", eventId))
                .andExpect(status().isOk()) .andExpect(view().name("admin-event-edit"))
                .andExpect(model().attributeExists("event"));
        verify(eventService).findById(eventId);
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Non-admin is denied access to edit form")
    void nonAdminCannotAccessEditForm() throws Exception {
        long eventId = 1L;
        mockMvc.perform(get("/admin/events/{id}/edit", eventId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can view event detail page with registrations")
    void adminCanViewEventDetail() throws Exception {
        long eventId = 2L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        User regUser1 = new User();
        regUser1.setUserId(101L);
        regUser1.setName("Alice");

        User regUser2 = new User();
        regUser2.setUserId(102L);
        regUser2.setName("Bob");

        Registration registration1 = new Registration();
        registration1.setUser(regUser1);

        Registration registration2 = new Registration();
        registration2.setUser(regUser2);

        when(registrationService.getRegistrationsForEvent(event)).thenReturn(List.of(registration1, registration2));


        mockMvc.perform(get("/admin/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-event-detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("registrations"))
                .andExpect(model().attributeExists("registrationCount"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @DisplayName("Null current user is handled gracefully on event detail page")
    void nullCannotViewEventDetail() throws Exception {
        long eventId = 2L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        User regUser1 = new User();
        regUser1.setUserId(101L);
        regUser1.setName("Alice");

        User regUser2 = new User();
        regUser2.setUserId(102L);
        regUser2.setName("Bob");

        Registration registration1 = new Registration();
        registration1.setUser(regUser1);

        Registration registration2 = new Registration();
        registration2.setUser(regUser2);

        when(registrationService.getRegistrationsForEvent(event)).thenReturn(List.of(registration1, registration2));

        // No @WithMockCustomUser annotation here, so current user is null
        // Should throw IllegalArgumentException("Invalid user ID"));

        mockMvc.perform(get("/admin/events/{id}", eventId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can update event successfully")
    void adminCanUpdateEvent() throws Exception {
        long eventId = 10L;
        Event existing = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));
        when(keywordService.findOrCreateByName(anyString()))
                .thenAnswer(invocation -> {
                    String name = invocation.getArgument(0, String.class);
                    Keyword k = new Keyword();
                    k.setKeyword(name);
                    return k;
                });

        mockMvc.perform(post("/admin/events/{id}/edit", eventId)
                        .param("title", "Updated Event")
                        .param("keywordsText", "Music, Festival")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/detail/" + eventId));

        verify(eventService).save(argThat(e ->
                e.getTitle().equals("Updated Event") &&
                        e.getKeywords().stream().map(Keyword::getKeyword).toList().contains("Music")
        ));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can update event successfully without keywords")
    void adminCanUpdateEventWithoutKeywords() throws Exception {
        long eventId = 10L;
        Event existing = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));
        when(keywordService.findOrCreateByName(anyString()))
                .thenAnswer(invocation -> {
                    String name = invocation.getArgument(0, String.class);
                    Keyword k = new Keyword();
                    k.setKeyword(name);
                    return k;
                });

        mockMvc.perform(post("/admin/events/{id}/edit", eventId)
                        .param("title", "Updated Event")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events/detail/" + eventId));

        verify(eventService).save(argThat(e ->
                e.getTitle().equals("Updated Event") &&
                        e.getKeywords().isEmpty()
        ));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can hide event successfully")
    void adminCanHideEvent() throws Exception {
        long eventId = 20L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));

        mockMvc.perform(post("/admin/events/{id}/hide", eventId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events/" + eventId));

        verify(eventService).save(argThat(e -> e.isHidden()));
        verify(auditLogService).record(anyLong(),
                eq(AdminAction.EVENT_HIDE),
                eq(AdminTargetType.EVENT),
                eq(eventId),
                anyString());
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can unhide event successfully")
    void adminCanUnhideEvent() throws Exception {
        long eventId = 21L;
        Event event = mockEvent(eventId);
        event.setHidden(true);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));

        mockMvc.perform(post("/admin/events/{id}/unhide", eventId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/events/" + eventId));

        verify(eventService).save(argThat(e -> !e.isHidden()));
        verify(auditLogService).record(anyLong(),
                eq(AdminAction.EVENT_UNHIDE),
                eq(AdminTargetType.EVENT),
                eq(eventId),
                anyString());
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can delete event successfully")
    void adminCanDeleteEvent() throws Exception {
        long eventId = 22L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));

        mockMvc.perform(post("/admin/events/{id}/delete", eventId).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).deleteById(eventId);
        verify(auditLogService).record(anyLong(),
                eq(AdminAction.EVENT_DELETE),
                eq(AdminTargetType.EVENT),
                eq(eventId),
                anyString());
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Invalid event ID throws exception")
    void eventNotFoundThrowsException() throws Exception {
        long eventId = 99L;
        when(eventService.findById(eventId)).thenReturn(Optional.empty());

        try {
            mockMvc.perform(get("/admin/events/{id}/edit", eventId))
                    .andExpect(status().isInternalServerError());
        } catch (ServletException exception) {
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("Organiser cannot update events via admin controller")
    void organiserCannotUpdateEvent() throws Exception {
        long eventId = 10L;

        mockMvc.perform(post("/admin/events/{id}/edit", eventId)
                        .param("title", "Illegal Update")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("Organiser can download csv of attendance for single event")
    void organiserCanDownloadCsv() throws Exception {
        long eventId = 30L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        User regUser1 = new User();
        regUser1.setUserId(201L);
        regUser1.setName("Charlie");

        User regUser2 = new User();
        regUser2.setUserId(202L);
        regUser2.setName("Dana");

        Registration registration1 = new Registration();
        registration1.setUser(regUser1);
        registration1.setEvent(event);

        Registration registration2 = new Registration();
        registration2.setUser(regUser2);
        registration2.setEvent(event);

        when(registrationService.getRegistrationsForEvent(event)).thenReturn(List.of(registration1, registration2));

        mockMvc.perform(get("/admin/events/{eventId}/attendees.csv", eventId))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"event-#"+ eventId + "-attendees.csv\""
                ))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Charlie")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dana")));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("Organiser tries to download csv of attendance for single event that does not exist")
    void organiserCantDownloadCsvDoesNotExist() throws Exception {
        long eventId = 30L;
        Event event = mockEvent(eventId);
        when(eventService.findById(eventId)).thenReturn(Optional.empty());
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        User regUser1 = new User();
        regUser1.setUserId(201L);
        regUser1.setName("Charlie");

        User regUser2 = new User();
        regUser2.setUserId(202L);
        regUser2.setName("Dana");

        Registration registration1 = new Registration();
        registration1.setUser(regUser1);
        registration1.setEvent(event);

        Registration registration2 = new Registration();
        registration2.setUser(regUser2);
        registration2.setEvent(event);

        when(registrationService.getRegistrationsForEvent(event)).thenReturn(List.of(registration1, registration2));
        // Should throw have response of response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        mockMvc.perform(get("/admin/events/{eventId}/attendees.csv", eventId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.ORGANISER)
    @DisplayName("Organsier can download csv of attendance for all events")
    void organiserCanDownloadCsvForAllEvents() throws Exception {
        Event event1 = mockEvent(40L);
        Event event2 = mockEvent(41L);
        when(eventService.getAllEvents()).thenReturn(List.of(event1, event2));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));

        User regUser1 = new User();
        regUser1.setUserId(301L);
        regUser1.setName("Eve");

        User regUser2 = new User();
        regUser2.setUserId(302L);
        regUser2.setName("Frank");

        Registration registration1 = new Registration();
        registration1.setUser(regUser1);
        registration1.setEvent(event1);

        Registration registration2 = new Registration();
        registration2.setUser(regUser2);
        registration2.setEvent(event2);

        when(registrationService.getRegistrationsForEvent(event1)).thenReturn(List.of(registration1));
        when(registrationService.getRegistrationsForEvent(event2)).thenReturn(List.of(registration2));

        mockMvc.perform(get("/admin/events/all/attendees.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"all-events-with-attendees.csv\""
                ))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Eve")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Frank")));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.ORGANISER)
    @DisplayName("Organsier can download csv of attendance for all events with no attendees")
    void organiserCanDownloadCsvForAllEventsNoAttendees() throws Exception {
        Event event1 = mockEvent(40L);
        Event event2 = mockEvent(41L);
        when(eventService.getAllEvents()).thenReturn(List.of(event1, event2));
        when(userService.findById(anyLong())).thenReturn(Optional.of(mockAdmin()));


        when(registrationService.getRegistrationsForEvent(event1)).thenReturn(List.of());
        when(registrationService.getRegistrationsForEvent(event2)).thenReturn(List.of());

        mockMvc.perform(get("/admin/events/all/attendees.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Disposition",
                        "attachment; filename=\"all-events-with-attendees.csv\""
                ))
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                // The string ,,,,No,Upcoming,,,, indicates no attendees for the events, but events exist and are listed
                .andExpect(content().string(org.hamcrest.Matchers.containsString(",,,,No,Upcoming,,,,")));
    }

}
