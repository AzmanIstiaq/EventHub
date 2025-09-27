package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.TestHelpers.WithMockCustomUser;
import au.edu.rmit.sept.webapp.repository.*;
import au.edu.rmit.sept.webapp.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

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

    // --- ADMIN ENDPOINTS ---

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin view all users returns OK and model attributes present")
    void adminViewAllUsers() throws Exception {
        List<User> users = List.of(new User(), new User());
        users.get(0).setRole(UserType.ORGANISER);
        users.get(1).setRole(UserType.STUDENT);
        when(userService.getAllUsers()).thenReturn(users);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("adminCount"))
                .andExpect(model().attributeExists("organiserCount"))
                .andExpect(model().attributeExists("studentCount"));
    }

    @Test
    @WithMockCustomUser(username = "admin", role = UserType.ADMIN)
    @DisplayName("Admin deactivate user success redirects")
    void adminDeactivateUser() throws Exception {
        User u = new User();
        u.setUserId(44L);
        u.setName("X");

        when(userService.findById(44L)).thenReturn(Optional.of(u));

        mvc.perform(post("/users/{userId}/deactivate", 44L).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));
    }

    // --- PROFILE ENDPOINTS (ANY LOGGED-IN USER) ---

    @Test
    @WithMockCustomUser(username = "student", role = UserType.STUDENT)
    @DisplayName("Student profile loads with currentUser in model")
    void profileLoads() throws Exception {
        User u = new User();
        u.setUserId(100L);
        u.setName("Taylor");

        when(userService.findById(100L)).thenReturn(Optional.of(u));

        mvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-profile"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    @DisplayName("Organiser can update profile -> redirects to manage-profile")
    void updateProfile() throws Exception {
        User u = new User();
        u.setUserId(200L);
        u.setName("Jordan");

        when(userService.findById(200L)).thenReturn(Optional.of(u));

        mvc.perform(post("/users/profile").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("manage-profile"));
    }
}
