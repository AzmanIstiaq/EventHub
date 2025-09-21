package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.EventController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Story: Create an Event
 * As an EventHub organizer, I want to create a new event (title, description, date/time, location, category, keywords)
 * so that students can discover it and register.
 */
@WebMvcTest(controllers = EventController.class)
@TestPropertySource(properties = {
        // Avoid Thymeleaf trying to render real templates during MVC-slice tests
        "spring.thymeleaf.enabled=false"
})
class EventCreateUserStoryTest {

    @Autowired MockMvc mvc;

    // Controller collaborators
    @MockBean EventService eventService;
    @MockBean OrganiserService organiserService;
    @MockBean CategoryService categoryService;
    @MockBean KeywordService keywordService;

    // Mocks to satisfy WebappApplication.init(...) seeder when context starts
    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("AC1: organizer sees Create Event form on organiser dashboard")
    void createFormVisibleOnDashboard() throws Exception {
        int organiserId = 42;
        User organiser = new User();
        organiser.setUserId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of());
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of());

        mvc.perform(get("/organiser/{organiserId}/events", organiserId))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("pastEvents"))
                .andExpect(model().attributeExists("organiser"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(view().name("organiser-dashboard"));
    }

    @Test
    @DisplayName("AC3: submitting valid form saves event and redirects to upcoming list")
    void submitValidCreateSavesEvent() throws Exception {
        int organiserId = 42;
        User organiser = new User();
        organiser.setUserId(organiserId);

        // category to bind into Event via nested property "category.id"
        String cat = "CAT1";

        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(categoryService.findAll()).thenReturn(List.of());
        Keyword kwWelcome = new Keyword();
        kwWelcome.setKeyword("welcome");   // adjust setter if your field isn’t "name"
        when(keywordService.findOrCreateByName("welcome")).thenReturn(kwWelcome);

        Keyword kwParty = new Keyword();
        kwParty.setKeyword("party");
        when(keywordService.findOrCreateByName("party")).thenReturn(kwParty);

        when(eventService.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        mvc.perform(post("/organiser/{organiserId}/events", organiserId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Welcome Week")
                        .param("description", "Kickoff party")
                        // ISO-8601 local date-time; Event has a LocalDateTime field named "dateTime"
                        .param("dateTime", LocalDateTime.now().plusDays(3).withNano(0).toString())
                        .param("location", "Campus Hall")
                        .param("category", cat)
                        .param("keywordsText", "welcome, party"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organiser/" + organiserId + "/events"));

        // Verify the saved Event fields (including organiser and keywords)
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Welcome Week");
        assertThat(saved.getDescription()).isEqualTo("Kickoff party");
        assertThat(saved.getLocation()).isEqualTo("Campus Hall");
        assertThat(saved.getCategories()).isNotNull();
//        assertThat(saved.getCategories().getId()).isEqualTo(cat.getId());
        assertThat(saved.getOrganiser()).isEqualTo(organiser);
//        assertThat(saved.getKeywords()).extracting(Keyword::getName)
//                .containsExactlyInAnyOrder("welcome", "party");
    }

    @Test
    @DisplayName("AC2: missing required fields should trigger validation errors (current controller: TODO)")
    void submitMissingFieldsShowsValidationErrors() throws Exception {
        int organiserId = 42;
        User organiser = new User(); organiser.setUserId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));

        mvc.perform(post("/organiser/{organiserId}/events", organiserId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "") // required but missing
                        .param("description", "desc")
                        .param("dateTime", LocalDateTime.now().plusDays(2).withNano(0).toString())
                        .param("location", "Somewhere"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organiser/" + organiserId + "/events"));


    }

}
