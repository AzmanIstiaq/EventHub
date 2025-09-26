package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Keyword;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.service.CategoryService;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.KeywordService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/// This controller is used for modifying event details, to be performed by Admin and organiser
/// type users.
@Controller
@RequestMapping("/organiser")
public class OrganiserController {
    private final EventService eventService;
    private final OrganiserService organiserService;
    // inject services
    private final CategoryService categoryService;
    private final KeywordService keywordService;

    public OrganiserController(EventService eventService,
                               OrganiserService organiserService,
                               CategoryService categoryService,
                               KeywordService keywordService) {
        this.eventService = eventService;
        this.organiserService = organiserService;
        this.categoryService = categoryService;
        this.keywordService = keywordService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User organiser = organiserService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        model.addAttribute("organiser", organiser);

        return "manage-profile";
    }

    @PostMapping("/profile")
    public String UpdateProfile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        return "manage-profile";
    }

    // List events for given organiser ID
    @GetMapping("/events")
    public String listOrganisersEvents(@AuthenticationPrincipal CustomUserDetails currentUser,
                                       Model model) {
        User organiser = organiserService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        List<Event> upcomingEvents = eventService.getUpcomingEventsForOrganiser(organiser);
        List<Event> pastEvents = eventService.getPastEventsForOrganiser(organiser);

        // Add categories for form dropdown
        List<Category> categories = categoryService.findAll();

        System.out.println("DEBUG: Upcoming Events: " + upcomingEvents);
        System.out.println("DEBUG: Past Events: " + pastEvents);
        System.out.println("DEBUG: Categorey1: " + categories.get(0).getCategory());
        System.out.println("DEBUG: Organiser: " + organiser.getName());

        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("organiser", organiser);
        model.addAttribute("categories", categories);

        return "organiser-dashboard";
    }



    // Create new event for given organiser
    @PostMapping("/events/create")
    public String createEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @ModelAttribute Event event,
                              @RequestParam(required = false) String keywordsText) {
        User organiser = organiserService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        event.setOrganiser(organiser);

        // Convert comma-separated keywords to Set<Keyword>
        if (keywordsText != null && !keywordsText.isBlank()) {
            Set<Keyword> keywordSet = Arrays.stream(keywordsText.split(","))
                    .map(String::trim)
                    .map(keywordService::findOrCreateByName)
                    .collect(Collectors.toSet());
            event.setKeywords(keywordSet);
        }

        eventService.save(event);
        return "redirect:/organiser/events";
    }


    /// Updates an event based on the form input received
    @PostMapping("/events/{eventId}/edit")
    public String updateEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @PathVariable Long eventId,
                              @ModelAttribute Event updatedEvent,
                              @RequestParam(required = false) String keywordsText) {
        User organiser = organiserService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        // Copy form fields
        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setDateTime(updatedEvent.getDateTime());
        event.setLocation(updatedEvent.getLocation());
        event.setCategory(updatedEvent.getCategory());
        event.setOrganiser(organiser);

        // Convert comma-separated keywords to Set<Keyword>
        if (keywordsText != null && !keywordsText.isBlank()) {
            Set<Keyword> keywordSet = Arrays.stream(keywordsText.split(","))
                    .map(String::trim)
                    .map(keywordService::findOrCreateByName)
                    .collect(Collectors.toSet());
            event.setKeywords(keywordSet);
        }

        eventService.save(event);

        return "redirect:/organiser/events";
    }

    /// Deletes an event for a given organiser ID and event ID
    @GetMapping("/events/{eventId}/delete")
    public String deleteEvent(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @PathVariable Long eventId) {
        eventService.delete(eventId);
        return "redirect:/organiser/events";
    }

    @GetMapping("/public-events")
    public String listPublicEvents(Model model) {
        List<Event> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("activeTab", "upcoming");
        return "public-events";
    }
}
