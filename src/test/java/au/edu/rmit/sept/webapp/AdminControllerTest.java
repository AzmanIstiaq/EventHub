package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.AdminController;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import au.edu.rmit.sept.webapp.repository.RegistrationRepository;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import au.edu.rmit.sept.webapp.repository.KeywordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {"spring.thymeleaf.enabled=false"})
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;
    @MockBean
    UserService userService;
    @MockBean
    RegistrationService registrationService;

    // Mock repos to satisfy WebappApplication.init(...) seeder
    @MockBean UserRepository userRepository;
    @MockBean EventRepository eventRepository;
    @MockBean RegistrationRepository registrationRepository;
    @MockBean CategoryRepository categoryRepository;
    @MockBean KeywordRepository keywordRepository;

    @Test
    void dashboardLoads() throws Exception {
        // Controller reads lists; return empty lists to avoid NPE in size()
        when(eventService.getAllEvents()).thenReturn(List.of());
        when(userService.getAllUsers()).thenReturn(List.of());

        mvc.perform(get("/admin/dashboard")).andExpect(status().isOk());
    }
}