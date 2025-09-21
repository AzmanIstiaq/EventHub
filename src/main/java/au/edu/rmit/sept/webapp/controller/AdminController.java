package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.UserService;
import au.edu.rmit.sept.webapp.service.RegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for admin-specific operations like viewing all events,
 * managing users, and deleting inappropriate events
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final UserService userService;
    private final RegistrationService registrationService;

    public AdminController(EventService eventService, UserService userService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.userService = userService;
        this.registrationService = registrationService;
    }

    // Admin dashboard - shows all events with statistics
    @GetMapping("/events")
    public String viewAllEvents(Model model) {
        List<Event> allEvents = eventService.getAllEvents();
        model.addAttribute("events", allEvents);

        // Calculate statistics
        LocalDateTime now = LocalDateTime.now();
        int upcomingEventsCount = (int) allEvents.stream()
                .filter(event -> event.getDateTime().isAfter(now))
                .count();
        int pastEventsCount = (int) allEvents.stream()
                .filter(event -> event.getDateTime().isBefore(now))
                .count();

        model.addAttribute("upcomingEventsCount", upcomingEventsCount);
        model.addAttribute("pastEventsCount", pastEventsCount);

        return "admin-events";
    }

    // Delete inappropriate events
    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable int eventId,
                              RedirectAttributes redirectAttributes) {
        try {
            Event event = eventService.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

            String eventTitle = event.getTitle();
            eventService.delete(eventId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Event '" + eventTitle + "' has been deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deleting event: " + e.getMessage());
        }

        return "redirect:/admin/events";
    }

    // View all users (for user management) with statistics
    @GetMapping("/users")
    public String viewAllUsers(Model model) {
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);

        // Calculate user type statistics
        int adminCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.ADMIN)
                .count();
        int organiserCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.ORGANISER)
                .count();
        int studentCount = (int) allUsers.stream()
                .filter(user -> user.getRole() == UserType.STUDENT)
                .count();

        model.addAttribute("adminCount", adminCount);
        model.addAttribute("organiserCount", organiserCount);
        model.addAttribute("studentCount", studentCount);

        return "admin-users";
    }

    // Deactivate/ban user accounts
    @PostMapping("/users/{userId}/deactivate")
    public String deactivateUser(@PathVariable int userId,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

            // Add a status field to User model or implement soft delete
            // For now, we'll just show a message
            redirectAttributes.addFlashAttribute("successMessage",
                    "User '" + user.getName() + "' has been deactivated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error deactivating user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Admin dashboard home
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Get summary statistics
        List<Event> allEvents = eventService.getAllEvents();
        List<User> allUsers = userService.getAllUsers();

        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("recentEvents", allEvents.stream().limit(5).toList());

        return "admin-dashboard";
    }

    // View individual event details (admin perspective)
    @GetMapping("/events/{eventId}")
    public String viewEventDetail(@PathVariable int eventId, Model model) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        // Get registrations for this event
        List<Registration> registrations = registrationService.getRegistrationsForEvent(event);

        model.addAttribute("event", event);
        model.addAttribute("registrations", registrations);
        model.addAttribute("registrationCount", registrations.size());

        return "admin-event-detail";
    }

}