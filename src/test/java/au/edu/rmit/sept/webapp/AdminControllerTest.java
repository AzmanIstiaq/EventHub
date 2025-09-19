package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.controller.AdminController;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EventService eventService;
    @MockBean
    UserService userService;

    @Test
    void dashboardLoads() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isOk());
    }
}