package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.AdminController;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.KeywordRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.service.UserService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** As an EventHub administrator, I want to view any event so that I can monitor and review event content.
 * */
@WebMvcTest(controllers = AdminController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
// Remove this line if  we want Thymeleaf to render real templates in this test
@TestPropertySource(properties = { "spring.thymeleaf.enabled=false" })
public class AdminUserStoryTest {

    @Autowired MockMvc mvc;

    // Services used by AdminController
    @MockBean EventService eventService;
    @MockBean UserService userService;
    @MockBean RegistrationService registrationService;

    // Seeder bean deps mocked so context starts
    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    @DisplayName("US: Admin can view list of all events")
    void adminCanViewAllEvents() throws Exception {
        Event e = new Event();
        e.setEventId(1);
        e.setTitle("Sample Event");
        e.setDescription("Desc");
        e.setLocation("Hall");
        e.setEventDate(LocalDateTime.now().plusDays(1));

        // Provide organiser + category to match template expectations
        User org = new User();
        org.setUserId(100);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");
        e.setOrganiser(org);

        String cat = "CAT1";
        e.addCategory(cat);

        when(eventService.getAllEvents()).thenReturn(List.of(e));

        mvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("events"))
                .andExpect(view().name("admin-events"));
    }

    @Test
    @DisplayName("US: Admin can view event details")
    void adminCanViewEventDetails() throws Exception {
        Event e = new Event();
        e.setEventId(2);
        e.setTitle("Welcome Week");
        e.setDescription("Big welcome party");
        e.setLocation("Campus Hall");
        e.setEventDate(LocalDateTime.now().plusDays(3));

        //  organiser + category so `${event.organiser.name}` etc. won’t be null
        User org = new User();
        org.setUserId(100);
        org.setName("CSIT Club");
        org.setEmail("csit@uni.edu");
        e.setOrganiser(org);

        String cat = "ORIENTATION";
        e.addCategory(cat);

        when(eventService.findById(2)).thenReturn(Optional.of(e));
        // If the controller adds "registrations" via registrationService, it can also stub it here:
        // when(registrationService.findByEventId(2L)).thenReturn(List.of());

        mvc.perform(get("/admin/events/2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("event"))
                // If the controller always adds "registrations", keep this line; otherwise we remove it:
                .andExpect(model().attributeExists("registrations"))
                .andExpect(view().name("admin-event-detail"));
    }
}
