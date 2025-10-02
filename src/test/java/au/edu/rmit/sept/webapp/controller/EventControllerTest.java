package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.TestHelpers.WithMockCustomUser;
import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.KeywordRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EventController.class)
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean RegistrationService registrationService;
    @MockBean UserService userService;
    @MockBean CategoryService categoryService;
    @MockBean KeywordService keywordService;

    @MockBean private UserRepository userRepo;
    @MockBean private EventRepository eventRepo;
    @MockBean private RegistrationRepository registrationRepo;
    @MockBean private CategoryRepository categoryRepo;
    @MockBean private KeywordRepository keywordRepo;

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Event detail (public): OK with view")
    void eventDetailPublicOk() throws Exception {
        User org = new User();
        org.setUserId(100L);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");

        long eventId = 12L;
        Event e = new Event();
        e.setEventId(eventId);
        e.setTitle("Open Day");
        e.setDateTime(LocalDateTime.now().plusDays(8));
        e.setOrganiser(org);

        when(eventService.findById(eventId)).thenReturn(Optional.of(e));

        mvc.perform(get("/events/public/detail/" + eventId))
            .andExpect(status().isOk())
            .andExpect(view().name("event-detail"))
            .andExpect(model().attributeExists("event"));
    }

    @Test
    @DisplayName("Search with category and dates -> OK")
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    void searchWithCategoryAndDatesOk() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/public/search")
                .param("keyword", "hack")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(30).toString())
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("public-events"))
            .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin can view all events")
    void adminViewAllEvents() throws Exception {
        User org = new User();
        org.setUserId(100L);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");
        

        Event upcoming = new Event();
        upcoming.setEventId(1L);
        upcoming.setTitle("Upcoming Event");
        upcoming.setDateTime(LocalDateTime.now().plusDays(1));
        upcoming.setOrganiser(org);

        Event past = new Event();
        past.setEventId(2L);
        past.setTitle("Past Event");
        past.setDateTime(LocalDateTime.now().minusDays(1));
        past.setOrganiser(org);

        // Set up your mock user ID
        long adminId = 100L;
        User admin = new User();
        admin.setUserId(adminId);

        // Mock userService to return this admin user
        when(userService.findById(adminId)).thenReturn(Optional.of(admin));

        // Mock eventService as usual
        when(eventService.getAllEvents()).thenReturn(List.of(upcoming, past));

        mvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("upcomingEventsCount"))
                .andExpect(model().attributeExists("pastEventsCount"));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("US: Admin can view list of all events")
    void adminCanViewAllEvents() throws Exception {
        Event e = new Event();
        e.setEventId(1L);
        e.setTitle("Sample Event");
        e.setDescription("Desc");
        e.setLocation("Hall");
        e.setDateTime(LocalDateTime.now().plusDays(1));

        // Set up your mock user ID
        long adminId = 100L;
        User admin = new User();
        admin.setUserId(adminId);

        // Mock userService to return this admin user
        when(userService.findById(adminId)).thenReturn(Optional.of(admin));

        // Provide organiser + category to match template expectations
        User org = new User();
        org.setUserId(100L);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");
        e.setOrganiser(org);

        Category cat1 = new Category("ORIENTATION");
        e.setCategory(cat1);

        when(eventService.getAllEvents()).thenReturn(List.of(e));

        mvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("events"))
                .andExpect(view().name("admin-events"));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("US: Admin can view event details")
    void adminCanViewEventDetails() throws Exception {
        Event e = new Event();
        e.setEventId(2L);
        e.setTitle("Welcome Week");
        e.setDescription("Big welcome party");
        e.setLocation("Campus Hall");
        e.setDateTime(LocalDateTime.now().plusDays(3));

        // organiser + category so `${event.organiser.name}` etc. won’t be null
        User org = new User();
        org.setUserId(100L);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");
        e.setOrganiser(org);

        Category cat1 = new Category("ORIENTATION");
        e.setCategory(cat1);

        when(eventService.findById(2L)).thenReturn(Optional.of(e));

        mvc.perform(get("/events/detail/2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("event"))
                // include this only if your controller actually adds "registrations" to the
                // model
                .andExpect(model().attributeExists("registrations"))
                .andExpect(view().name("admin-event-detail"));
    }
    
    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("US: Admin can delete an event successfully")
    void adminDeleteEventSuccess() throws Exception {
        Event e = new Event();
        e.setEventId(100L);
        e.setTitle("Title");

        when(eventService.findById(100L)).thenReturn(Optional.of(e));

        mvc.perform(post("/events/{eventId}/delete", 100L)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).delete(100L);
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("US: Admin delete with invalid eventId still redirects")
    void adminDeleteEventInvalid() throws Exception {
        when(eventService.findById(anyLong())).thenReturn(Optional.empty());

        mvc.perform(post("/events/{eventId}/delete", 999L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Organiser can view their dashboard with upcoming and past events")
    void organiserListEventsOk() throws Exception {
        long organiserId = 100L;
        User organiser = new User();
        organiser.setUserId(organiserId);

        Event up = new Event(); 
        up.setDateTime(LocalDateTime.now().plusDays(1));
        up.setCategory(new Category("cat1"));
        Event past = new Event(); 
        past.setDateTime(LocalDateTime.now().minusDays(1));
        past.setCategory(new Category("cat1"));

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of(up));
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of(past));
        when(categoryService.findAll()).thenReturn(List.of(new Category("C1")));

        mvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("pastEvents"))
                .andExpect(model().attributeExists("organiser"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(view().name("organiser-dashboard"));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Organiser can create a new event and gets redirected to dashboard")
    void createEventRedirects() throws Exception {
        long organiserId = 100L;
        User organiser = new User(); organiser.setUserId(organiserId);

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/events/create")
                        .param("title", "Title")
                        .param("description", "description")
                        .param("location", "Loc")
                        .param("category.id", "1")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("keywordsText", "tag1, tag2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).save(any(Event.class));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Organiser can update an event and gets redirected to dashboard")
    void updateEventRedirects() throws Exception {
        long organiserId = 100L;
        long eventId = 101L;

        User organiser = new User(); organiser.setUserId(organiserId);
        Event existing = new Event(); existing.setEventId(eventId);
        existing.setOrganiser(organiser);

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));
        when(eventService.save(existing)).thenReturn(existing);

        mvc.perform(post("/events/{eventId}/edit", eventId)
                        .param("title", "Title")
                        .param("description", "description")
                        .param("location", "Loc")
                        .param("category.id", "1")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("keywordsText", "tag1, tag2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).save(existing);
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Organiser can delete an event and gets redirected to dashboard")
    void deleteEventRedirects() throws Exception {
        long organiserId = 100L;
        long eventId = 101L;

        // create organiser
        User organiser = new User();
        organiser.setUserId(organiserId);

        // create event and assign organiser
        Event existing = new Event();
        existing.setEventId(eventId);
        existing.setTitle("title");
        existing.setOrganiser(organiser);

        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));

        mvc.perform(post("/events/{eventId}/delete", eventId)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).delete(eq(eventId));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Creating an event with missing fields shows validation errors")
    void createEventValidationErrors() throws Exception {
        long organiserId = 100L;
        User organiser = new User();
        organiser.setUserId(organiserId);
        organiser.setName("bob");

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(categoryService.findAll()).thenReturn(List.of(new Category("CAT1")));

        Event e = new Event();
        e.setTitle("");
        e.setLocation("Campus Hall");
        e.setDateTime(LocalDateTime.now().plusDays(2));
        e.setOrganiser(organiser);

        mvc.perform(post("/events/create")
                        .param("title", "") // missing title
                        .param("location", "Campus Hall")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "title"))
                .andExpect(view().name("organiser-dashboard"));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Organiser sees Create Event form on dashboard")
    void organiserSeesCreateForm() throws Exception {
        long organiserId = 100L;
        User organiser = new User();
        organiser.setUserId(organiserId);

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of());
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of(new Category("CAT1"), new Category("CAT2")));

        mvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("pastEvents"))
                .andExpect(model().attributeExists("organiser"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(view().name("organiser-dashboard"));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Submitting a valid event form saves event and redirects")
    void createEventValidFormRedirects() throws Exception {
        long organiserId = 100L;
        User organiser = new User();
        organiser.setUserId(organiserId);

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoryService.findAll()).thenReturn(List.of(new Category("CAT1")));

        mvc.perform(post("/events/create")
                        .param("title", "Welcome Week")
                        .param("description", "Kickoff party")
                        .param("location", "Campus Hall")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("category.id", "1")
                        .param("keywordsText", "welcome, party")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(eventService).save(any(Event.class));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("US: Creating an event with missing required fields shows validation errors")
    void createEventMissingFieldsShowsErrors() throws Exception {
        long organiserId = 100L;
        User organiser = new User();
        organiser.setUserId(organiserId);

        when(userService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(categoryService.findAll()).thenReturn(List.of(new Category("CAT1")));

        mvc.perform(post("/events/create")
                        .param("title", "") // missing title
                        .param("description", "Some description")
                        .param("location", "Campus Hall")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("event", "title"))
                .andExpect(view().name("organiser-dashboard"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("US: Student RSVPs to event -> redirects and calls registrationService")
    void rsvpRegistersAndRedirects() throws Exception {
        long studentId = 100L;
        long eventId = 10L;

        User student = new User();
        student.setUserId(studentId);
        student.setName("Sam");

        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle("Welcome Week");
        event.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));

        mvc.perform(post("/events/register/{eventId}", eventId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(registrationService).registerUserForEvent(student, event);
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("US: Student cancels RSVP -> redirect and delete registration")
    void cancelRsvpRedirects() throws Exception {
        long eventId = 10L;
        long studentId = 100L;

        User student = new User();
        student.setUserId(studentId);

        when(userService.findById(studentId)).thenReturn(Optional.of(student));

        mvc.perform(post("/events/deleteRegistration")
                .param("userId", String.valueOf(studentId))
                .param("eventId", String.valueOf(eventId))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"));

        verify(registrationService).deleteRegistrationForEvent(studentId, eventId);
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Edge: RSVP by non-existent user -> shows error")
    void userNotFoundRsvp() throws Exception {
        long studentId = 404L;
        long eventId = 10L;

        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle("Welcome Week");
        event.setDateTime(LocalDateTime.now().plusDays(2));

        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        when(userService.findById(studentId)).thenReturn(Optional.empty());

        mvc.perform(post("/events/register/{eventId}", eventId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Edge: RSVP to non-existent event -> shows error")
    void eventNotFoundRsvp() throws Exception {
        long studentId = 100L;
        long eventId = 999L;

        User student = new User();
        student.setUserId(studentId);
        student.setName("Sam");

        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(eventService.findById(eventId)).thenReturn(Optional.empty());

        mvc.perform(post("/events/register/{eventId}", eventId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Edge: already registered -> shows error")
    void alreadyRegisteredRsvp() throws Exception {
        long studentId = 100L;
        long eventId = 10L;

        User student = new User();
        student.setUserId(studentId);
        student.setName("Sam");

        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle("Welcome Week");
        event.setDateTime(LocalDateTime.now().plusDays(3));

        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(eventService.findById(eventId)).thenReturn(Optional.of(event));
        doThrow(new IllegalStateException("duplicate"))
                .when(registrationService).registerUserForEvent(student, event);

        mvc.perform(post("/events/register/{eventId}", eventId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Edge: date range end before start -> OK")
    void dateRangeEndBeforeStartOk() throws Exception {
        long studentId = 100L;

        User student = new User();
        student.setUserId(studentId);
        student.setName("Sam");

        when(categoryService.findAll()).thenReturn(List.of());
        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/search")
                        .param("query", "q")
                        .param("startDate", LocalDate.now().plusDays(10).toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Edge: only endDate provided -> OK")
    void onlyEndDateProvidedOk() throws Exception {
        long studentId = 100L;
        User student = new User();
        student.setUserId(studentId);
        student.setName("Sam");

        when(categoryService.findAll()).thenReturn(List.of());
        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/events/search")
                        .param("query", "q")
                        .param("endDate", LocalDate.now().plusDays(1).toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("US: Student views event details -> OK with model")
    void eventDetailLoggedInOk() throws Exception {
        long eventId = 11L;
        long studentId = 100L;

        long organiserId = 10L;
        User organiser = new User();
        organiser.setUserId(organiserId);
        organiser.setName("Bob");

        Event e = new Event();
        e.setEventId(eventId);
        e.setTitle("Tech Talk");
        e.setDateTime(LocalDateTime.now().plusDays(4));
        e.setOrganiser(organiser);

        User student = new User();
        student.setUserId(studentId);
        student.setName("Pat");

        when(eventService.findById(eventId)).thenReturn(Optional.of(e));
        when(userService.findById(studentId)).thenReturn(Optional.of(student));

        mvc.perform(get("/events/detail/" + eventId)
                       .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("event-detail"))
                .andExpect(model().attributeExists("event"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("US: Student lists events -> OK with model")
    void listEventsLoggedInOk() throws Exception {
        long studentId = 100L;

        Event e = new Event();
        e.setEventId(30L);
        e.setTitle("Seminar");
        e.setDateTime(LocalDateTime.now().plusDays(1));

        User student = new User();
        student.setUserId(studentId);
        student.setName("Taylor");

        when(eventService.getAllUpcomingEvents()).thenReturn(List.of(e));
        when(eventService.getEventsUserRegisteredTo(studentId)).thenReturn(List.of());
        when(userService.findById(studentId)).thenReturn(Optional.of(student));
        when(categoryService.findAll()).thenReturn(List.of());

        mvc.perform(get("/events")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("public-events"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("registrations"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("pastEvents"));
    }

}
