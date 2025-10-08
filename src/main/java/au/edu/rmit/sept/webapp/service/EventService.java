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

    // (Keeps your original name too, in case other code calls it)
    public void delete(long id) {
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
        return eventRepository.searchEventsWithEnd(query, from, to, categoryId);
    }

    // Helper used by Admin view for counts
    public int countRegistrationsForEvent(Event event) {
        return registrationService.getRegistrationsForEvent(event).size();
    }
}
