package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.EventController;
import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventCreateUserStoryTest {
    EventService eventService = org.mockito.Mockito.mock(EventService.class);
    OrganiserService organiserService = org.mockito.Mockito.mock(OrganiserService.class);
    CategoryService categoryService = org.mockito.Mockito.mock(CategoryService.class);
    KeywordService keywordService = org.mockito.Mockito.mock(KeywordService.class);
    EventController controller = new EventController(eventService, organiserService, categoryService, keywordService);

    @Test
    @DisplayName("AC1: organizer sees Create Event form on organiser dashboard")
    void createFormVisibleOnDashboard() throws Exception {
        long organiserId = 42L;
        User organiser = new User();
        organiser.setId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of());
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of(new Category("CAT1"), new Category("CAT2")));
        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        Model model = new ExtendedModelMap();
        String view = controller.listOrganisersEvents(cud, model);
        assertThat(view).isEqualTo("organiser-dashboard");
        assertThat(model.containsAttribute("upcomingEvents")).isTrue();
        assertThat(model.containsAttribute("pastEvents")).isTrue();
        assertThat(model.containsAttribute("organiser")).isTrue();
        assertThat(model.containsAttribute("categories")).isTrue();
    }

    @Test
    @DisplayName("AC3: submitting valid form saves event and redirects to upcoming list")
    void submitValidCreateSavesEvent() throws Exception {
        long organiserId = 42L;
        User organiser = new User();
        organiser.setId(organiserId);

        // category to bind into Event via nested property "category.id"
        Category cat = new Category("CAT1");
        cat.setId(7L);

        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(categoryService.findAll()).thenReturn(List.of(cat));
        Keyword kwWelcome = new Keyword();
        kwWelcome.setName("welcome");   // adjust setter if your field isn’t "name"
        when(keywordService.findOrCreateByName("welcome")).thenReturn(kwWelcome);

        Keyword kwParty = new Keyword();
        kwParty.setName("party");
        when(keywordService.findOrCreateByName("party")).thenReturn(kwParty);

        when(eventService.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        Event ev = new Event();
        ev.setTitle("Welcome Week");
        ev.setDescription("Kickoff party");
        ev.setDateTime(LocalDateTime.now().plusDays(3).withNano(0));
        ev.setLocation("Campus Hall");
        ev.setCategory(cat);
        String view = controller.createEvent(cud, ev, "welcome, party");
        assertThat(view).isEqualTo("redirect:/organiser/events");

        // Verify the saved Event fields (including organiser and keywords)
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventService).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Welcome Week");
        assertThat(saved.getDescription()).isEqualTo("Kickoff party");
        assertThat(saved.getLocation()).isEqualTo("Campus Hall");
        assertThat(saved.getCategory()).isNotNull();
        assertThat(saved.getCategory().getId()).isEqualTo(cat.getId());
        assertThat(saved.getOrganiser()).isEqualTo(organiser);
        assertThat(saved.getKeywords()).extracting(Keyword::getName)
                .containsExactlyInAnyOrder("welcome", "party");
    }

    @Test
    @DisplayName("AC2: missing required fields should trigger validation errors (current controller: TODO)")
    void submitMissingFieldsShowsValidationErrors() throws Exception {
        long organiserId = 42L;
        User organiser = new User(); organiser.setId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        CustomUserDetails cud = org.mockito.Mockito.mock(CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        Event ev = new Event();
        ev.setTitle("");
        ev.setDescription("desc");
        ev.setDateTime(LocalDateTime.now().plusDays(2).withNano(0));
        ev.setLocation("Somewhere");
        String view = controller.createEvent(cud, ev, null);
        assertThat(view).isEqualTo("redirect:/organiser/events");
    }

}
