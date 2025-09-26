package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.OrganiserController;
import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrganiserControllerWebMvcTest {

    EventService eventService = mock(EventService.class);
    OrganiserService organiserService = mock(OrganiserService.class);
    CategoryService categoryService = mock(CategoryService.class);
    KeywordService keywordService = mock(KeywordService.class);
    OrganiserController controller = new OrganiserController(eventService, organiserService, categoryService, keywordService);

    @Test
    @DisplayName("Organiser list events view OK with model")
    void organiserListEventsOk() throws Exception {
        long organiserId = 55L;
        User organiser = new User(); organiser.setUserId(organiserId);
        Event up = new Event(); up.setDateTime(LocalDateTime.now().plusDays(1));
        Event past = new Event(); past.setDateTime(LocalDateTime.now().minusDays(1));
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of(up));
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of(past));
        when(categoryService.findAll()).thenReturn(List.of(new Category("C1")));

        var cud = mock(au.edu.rmit.sept.webapp.security.CustomUserDetails.class);
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
    @DisplayName("Create event redirects to organiser list")
    void createEventRedirects() throws Exception {
        long organiserId = 55L;
        User organiser = new User(); organiser.setUserId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        var cud = mock(au.edu.rmit.sept.webapp.security.CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        Event ev = new Event();
        ev.setTitle("Title"); ev.setLocation("Loc"); ev.setDateTime(LocalDateTime.now().plusDays(2));
        String view = controller.createEvent(cud, ev, "tag1, tag2");
        assertThat(view).isEqualTo("redirect:/organiser/events");
        verify(eventService).save(any(Event.class));
    }

    @Test
    @DisplayName("Update event redirects to organiser list")
    void updateEventRedirects() throws Exception {
        long organiserId = 55L;
        long eventId = 101L;
        User organiser = new User(); organiser.setUserId(organiserId);
        Event existing = new Event(); existing.setEventId(eventId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));
        when(eventService.save(existing)).thenReturn(existing);
        var cud = mock(au.edu.rmit.sept.webapp.security.CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        Event updated = new Event(); updated.setTitle("NewTitle"); updated.setLocation("NewLoc");
        String view = controller.updateEvent(cud, eventId, updated, null);
        assertThat(view).isEqualTo("redirect:/organiser/events");
        verify(eventService).save(existing);
    }

    @Test
    @DisplayName("Delete event redirects")
    void deleteEventRedirects() throws Exception {
        long organiserId = 55L;
        long eventId = 101L;

        var cud = mock(au.edu.rmit.sept.webapp.security.CustomUserDetails.class);
        when(cud.getId()).thenReturn(organiserId);
        String view = controller.deleteEvent(cud, eventId);
        assertThat(view).isEqualTo("redirect:/organiser/events");
        verify(eventService).delete(eq(eventId));
    }
}
