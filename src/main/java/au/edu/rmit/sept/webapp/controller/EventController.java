package au.edu.rmit.sept.webapp.controller;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.service.EventService;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/// This controller is used for modifying event details, to be performed by Admin and organiser
/// type users.
@Controller
@RequestMapping("/organiser")
public class EventController {
    private final EventService eventService;
    private final OrganiserService organiserService;

    public EventController(EventService eventService, OrganiserService organiserService) {
        this.eventService = eventService;
        this.organiserService = organiserService;
    }

    // List events for given organiser ID
    @GetMapping("/{organiserId}/events")
    public String listUpcomingEvents(@PathVariable Long organiserId, Model model) {
        User organiser = organiserService.findById(organiserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        List<Event> upcomingEvents = eventService.getUpcomingEventsForOrganiser(organiser);
        List<Event> pastEvents = eventService.getPastEventsForOrganiser(organiser);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("pastEvents", pastEvents);
        model.addAttribute("organiser", organiser);
        return "organiser-dashboard";
    }


    // Create new event for given organiser
    @PostMapping("/{organiserId}/events")
    public String createEvent(@PathVariable Long organiserId, @ModelAttribute Event event) {
        User organiser = organiserService.findById(organiserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));
        event.setOrganiser(organiser);
        eventService.save(event);
        return "redirect:/organiser/" + organiserId + "/events";
    }

    /// Updates an event based on the form input received
    @PostMapping("/{organiserId}/events/{eventId}/edit")
    public String updateEvent(@PathVariable Long organiserId,
                              @PathVariable Long eventId,
                              @ModelAttribute Event updatedEvent) {
        User organiser = organiserService.findById(organiserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid organiser ID"));

        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event ID"));

        // Copy form fields
        event.setTitle(updatedEvent.getTitle());
        event.setDescription(updatedEvent.getDescription());
        event.setDateTime(updatedEvent.getDateTime());
        event.setLocation(updatedEvent.getLocation());
        event.setOrganiser(organiser);

        eventService.save(event);

        return "redirect:/organiser/" + organiserId + "/events";
    }

    /// Deletes an event for a given organiser ID and event ID
    @GetMapping("/{organiserId}/events/{eventId}/delete")
    public String deleteEvent(@PathVariable Long organiserId, @PathVariable Long eventId) {
        eventService.delete(eventId);
        return "redirect:/organiser/" + organiserId + "/events";
    }
}
