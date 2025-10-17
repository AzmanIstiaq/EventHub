package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final RegistrationService registrationService;

    public EventService(EventRepository eventRepository,
                        RegistrationService registrationService) {
        this.eventRepository = eventRepository;
        this.registrationService = registrationService;
    }

    // Find a single event
    public Optional<Event> findById(long id) {
        return eventRepository.findById(id);
    }

    // Save or update an event
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    // Delete an event (controller uses this name)
    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }


    // Events for a specific organiser
    public List<Event> getUpcomingEventsForOrganiser(User organiser) {
        return eventRepository.findByOrganiserAndDateTimeAfterOrderByDateTimeAsc(
                organiser, LocalDateTime.now());
    }

    public List<Event> getPastEventsForOrganiser(User organiser) {
        return eventRepository.findByOrganiserAndDateTimeBeforeOrderByDateTimeAsc(
                organiser, LocalDateTime.now());
    }

    public List<Event> getPastEvents() {
        return eventRepository.findByDateTimeBeforeOrderByDateTimeAsc(LocalDateTime.now());
    }

    // All upcoming events for public view
    public List<Event> getAllUpcomingEvents() {
        return eventRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());
    }

    public List<Event> getEventsUserRegisteredTo(Long userId) {
        return eventRepository.findEventsByUserId(userId);
    }

    // NEW: Get ALL events (for admin view)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> searchEvents(String query,
                                    LocalDateTime from,
                                    LocalDateTime to,
                                    Long categoryId) {
        if (to == null) {
            return eventRepository.searchEvents(query, from, categoryId);
        }
        if (from == null) {
            from = LocalDateTime.MIN;
        }
        return eventRepository.searchEventsWithEnd(query, from, to, categoryId);
    }

    // Helper used by Admin view for counts
    public int countRegistrationsForEvent(Event event) {
        return registrationService.getRegistrationsForEvent(event).size();
    }

    public List<Event> getSuggestedEventsForUser(Long userId) {
        // Steps to find suggested events:
        // 1. Get the top 2 most popular events from any category, that the user is not registered to - popularity is based on number of registrations
        // 2. Find the categories of the events the user is registered to (past and upcoming)
        // 3. For each category, get the top 2 most popular events in that category that the user is not registered to
        // 4. Combine the results, ensuring no duplicates, and return

        // Get all events the user is registered to
        List<Event> registeredEvents = getEventsUserRegisteredTo(userId);
        // Extract the event IDs to exclude them later
        if (registeredEvents.isEmpty()) {
            // If the user is not registered to any events, just return the top 2 most popular overall
            return eventRepository.findTop2MostPopular(
                    LocalDateTime.now(), List.of(), true).subList(0, 2);
        }
        List<Long> registeredEventIds = registeredEvents.stream().map(Event::getEventId).toList();
        if (registeredEventIds.isEmpty()) {
            registeredEventIds = List.of(-1L);
        }

        // Step 1: Get top 2 most popular events overall (not registered to)
        List<Event> top2 = eventRepository.findTop2MostPopular(
                LocalDateTime.now(), registeredEventIds, true);

        // Filter down to 2 in case there are less than 2 popular events
        if (top2.size() > 2) {
            top2 = top2.subList(0, 2);
        }

        // Step 2: Find categories of events the user is registered to
        List<Long> categoryIds = registeredEvents.stream()
                .filter(event -> event.getCategory() != null) // Ensure category is not null
                .map(event -> event.getCategory().getId())
                .distinct()
                .toList();

        // Step 3: For each category, get top 2 most popular events in that category (not registered to)
        for (Long categoryId : categoryIds) {
            List<Event> top2ByCategory = eventRepository.findTop2MostPopularEventsByCategory(
                    LocalDateTime.now(), registeredEventIds, categoryId, true);
            // Filter down to 2 in case there are less than 2 popular events
            if (top2ByCategory.size() > 2) {
                top2ByCategory = top2ByCategory.subList(0, 2);
            }
            // Add to the main list, avoiding duplicates
            for (Event event : top2ByCategory) {
                if (!top2.contains(event)) {
                    top2.add(event);
                }
            }
        }
        return top2;
    }
}
