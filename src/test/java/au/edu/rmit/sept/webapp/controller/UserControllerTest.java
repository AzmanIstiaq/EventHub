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

import static org.mockito.ArgumentMatchers.anyLong;
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
        User u = new User();
        u.setUserId(1L);
        u.setName("X");
        u.setRole(UserType.ADMIN);

        when(userService.findById(anyLong())).thenReturn(Optional.of(u));


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
        u.setUserId(1L);
        u.setName("X");
        u.setRole(UserType.ADMIN);


        when(userService.findById(1L)).thenReturn(Optional.of(u));

        mvc.perform(post("/users/{userId}/deactivate", 1L).with(csrf()))
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
        u.setRole(UserType.STUDENT);

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
        u.setPassword("password");
        u.setRole(UserType.ORGANISER);

        when(userService.findById(anyLong())).thenReturn(Optional.of(u));
        when(userService.findByEmail("taken@example.com")).thenReturn(null);
        when(userService.save(u)).thenReturn(u);

        mvc.perform(post("/users/profile/save")
                        .with(csrf())
                        .param("name", "Jordan Updated")
                        .param("email", "jordan@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/events"))
                .andExpect(flash().attribute("success", "Updated profile successfully"));

    }

    @Test
    @WithMockCustomUser(username = "organiser", role = UserType.ORGANISER)
    void updateProfile_duplicateEmail_redirectsBack() throws Exception {
        User u = new User();
        u.setUserId(200L);
        u.setName("Jordan");

        when(userService.findById(anyLong())).thenReturn(Optional.of(u));

        User other = new User();
        other.setUserId(201L);
        other.setEmail("taken@example.com");
        when(userService.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        mvc.perform(post("/users/profile/save")
                        .with(csrf())
                        .param("name", "Jordan Updated")
                        .param("email", "taken@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/profile"))
                .andExpect(flash().attribute("error", "Email already in use, please choose another."));
    }

}
