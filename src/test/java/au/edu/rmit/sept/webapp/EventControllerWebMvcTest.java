package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.EventController;
import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import au.edu.rmit.sept.webapp.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@TestPropertySource(properties = {
        "spring.thymeleaf.enabled=false"
})
class EventControllerWebMvcTest {

    @Autowired MockMvc mvc;

    @MockBean EventService eventService;
    @MockBean OrganiserService organiserService;
    @MockBean CategoryService categoryService;
    @MockBean KeywordService keywordService;

    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("Organiser list events view OK with model")
    void organiserListEventsOk() throws Exception {
        long organiserId = 55L;
        User organiser = new User(); organiser.setId(organiserId);
        Event up = new Event(); up.setDateTime(LocalDateTime.now().plusDays(1));
        Event past = new Event(); past.setDateTime(LocalDateTime.now().minusDays(1));
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.getUpcomingEventsForOrganiser(organiser)).thenReturn(List.of(up));
        when(eventService.getPastEventsForOrganiser(organiser)).thenReturn(List.of(past));
        when(categoryService.findAll()).thenReturn(List.of(new Category("C1")));

        mvc.perform(get("/organiser/{organiserId}/events", organiserId))
                .andExpect(status().isOk())
                .andExpect(view().name("organiser-dashboard"))
                .andExpect(model().attributeExists("upcomingEvents"))
                .andExpect(model().attributeExists("pastEvents"))
                .andExpect(model().attributeExists("organiser"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("Create event redirects to organiser list")
    void createEventRedirects() throws Exception {
        long organiserId = 55L;
        User organiser = new User(); organiser.setId(organiserId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        mvc.perform(post("/organiser/{organiserId}/events", organiserId)
                        .param("title", "Title")
                        .param("location", "Loc")
                        .param("dateTime", LocalDateTime.now().plusDays(2).toString())
                        .param("keywordsText", "tag1, tag2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organiser/" + organiserId + "/events"));
        verify(eventService).save(any(Event.class));
    }

    @Test
    @DisplayName("Update event redirects to organiser list")
    void updateEventRedirects() throws Exception {
        long organiserId = 55L;
        long eventId = 101L;
        User organiser = new User(); organiser.setId(organiserId);
        Event existing = new Event(); existing.setId(eventId);
        when(organiserService.findById(organiserId)).thenReturn(Optional.of(organiser));
        when(eventService.findById(eventId)).thenReturn(Optional.of(existing));
        when(eventService.save(existing)).thenReturn(existing);

        mvc.perform(post("/organiser/{organiserId}/events/{eventId}/edit", organiserId, eventId)
                        .param("title", "NewTitle")
                        .param("location", "NewLoc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organiser/" + organiserId + "/events"));
        verify(eventService).save(existing);
    }

    @Test
    @DisplayName("Delete event redirects")
    void deleteEventRedirects() throws Exception {
        long organiserId = 55L;
        long eventId = 101L;

        mvc.perform(get("/organiser/{organiserId}/events/{eventId}/delete", organiserId, eventId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/organiser/" + organiserId + "/events"));
        verify(eventService).delete(eq(eventId));
    }
}
