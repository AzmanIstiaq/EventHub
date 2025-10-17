package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    private final EventService eventService = mock(EventService.class);
    private final UserService userService = mock(UserService.class);
    private final au.edu.rmit.sept.webapp.repository.EventRepository eventRepository = mock(au.edu.rmit.sept.webapp.repository.EventRepository.class);
    private final HomeController homeController = new HomeController(eventRepository, eventService, userService);

    @Test
    @DisplayName("home() with authenticated user returns home view with events")
    void homeWithAuthenticatedUserReturnsHomeView() {
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getId()).thenReturn(1L);
        
        User user = new User();
        user.setUserId(1L);
        user.setRole(UserType.STUDENT);
        
        List<Event> events = List.of(new Event(), new Event());
        
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findByDateTimeAfterOrderByDateTimeAsc(any())).thenReturn(events);

        Model model = new ExtendedModelMap();
        
        String result = homeController.home(currentUser, model);
        
        assertThat(result).isEqualTo("index");
        assertThat(model.containsAttribute("events")).isTrue();
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.getAttribute("events")).isEqualTo(events);
        assertThat(model.getAttribute("currentUser")).isEqualTo(user);
    }

    @Test
    @DisplayName("home() with null user returns home view with events only")
    void homeWithNullUserReturnsHomeView() {
        List<Event> events = List.of(new Event());
        when(eventRepository.findByDateTimeAfterOrderByDateTimeAsc(any())).thenReturn(events);

        Model model = new ExtendedModelMap();
        
        String result = homeController.home(null, model);
        
        assertThat(result).isEqualTo("index");
        assertThat(model.containsAttribute("events")).isTrue();
        assertThat(model.getAttribute("events")).isEqualTo(events);
        assertThat(model.getAttribute("currentUser")).isNull();
    }

    @Test
    @DisplayName("adminDashboard() returns admin dashboard view")
    void adminDashboardReturnsView() {
        CustomUserDetails currentUser = mock(CustomUserDetails.class);
        when(currentUser.getId()).thenReturn(1L);
        
        User admin = new User();
        admin.setUserId(1L);
        admin.setRole(UserType.ADMIN);
        
        List<Event> events = List.of(new Event(), new Event(), new Event());
        List<User> users = List.of(new User(), new User());
        
        when(userService.findById(1L)).thenReturn(Optional.of(admin));
        when(eventService.getAllEvents()).thenReturn(events);
        when(userService.getAllUsers()).thenReturn(users);
        
        Model model = new ExtendedModelMap();
        
        String result = homeController.adminDashboard(currentUser, model);
        
        assertThat(result).isEqualTo("admin-dashboard");
        assertThat(model.containsAttribute("currentUser")).isTrue();
        assertThat(model.containsAttribute("totalEvents")).isTrue();
        assertThat(model.containsAttribute("totalUsers")).isTrue();
        assertThat(model.containsAttribute("recentEvents")).isTrue();
        assertThat(model.getAttribute("totalEvents")).isEqualTo(3);
        assertThat(model.getAttribute("totalUsers")).isEqualTo(2);
    }
}
