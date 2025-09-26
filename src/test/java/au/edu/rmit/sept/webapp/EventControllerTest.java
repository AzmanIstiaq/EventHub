package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.EventController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventControllerTest {

    EventService eventService = mock(EventService.class);
    RegistrationService registrationService = mock(RegistrationService.class);
    UserService userService = mock(UserService.class);
    CategoryService categoryService = mock(CategoryService.class);
    EventController controller = new EventController(eventService, registrationService, userService, categoryService);



    @Test
    @DisplayName("Event detail (public): OK with view")
    void eventDetailPublicOk() {
        long eventId = 12L;
        Event e = new Event(); e.setEventId(eventId); e.setTitle("Open Day"); e.setDateTime(LocalDateTime.now().plusDays(8));
        when(eventService.findById(eventId)).thenReturn(Optional.of(e));

        Model model = new ExtendedModelMap();
        String view = controller.getEventDetail(model, eventId);
        assertThat(view).isEqualTo("event-detail");
        assertThat(model.containsAttribute("event")).isTrue();
    }

    @Test
    @DisplayName("Search with category and dates -> OK")
    void searchWithCategoryAndDatesOk() {
        when(categoryService.findAll()).thenReturn(List.of());
        when(eventService.getAllUpcomingEvents()).thenReturn(List.of());
        when(eventService.searchEvents(any(), any(), any(), any())).thenReturn(List.of());

        Model model = new ExtendedModelMap();
        String view = controller.searchEvents(null, "hack", LocalDate.now(), LocalDate.now().plusDays(30), 1L, model);
        assertThat(view).isEqualTo("public-events");
        assertThat(model.containsAttribute("searchResults")).isTrue();
        verify(eventService).searchEvents(any(), any(), any(), any());
    }


}
