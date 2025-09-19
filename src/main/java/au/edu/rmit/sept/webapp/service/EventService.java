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

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // Find a single event
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    // Save or update an event
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    // Delete an event
    public void delete(Long id) {
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

    // All upcoming events for public view
    public List<Event> getAllUpcomingEvents() {
        return eventRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());
    }
}
