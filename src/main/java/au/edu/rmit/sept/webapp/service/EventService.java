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
    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    public List<Event> getUpcomingEventsForOrganiser(User organiser) {
        return repo.findByOrganiserAndDateTimeAfter(organiser, LocalDateTime.now());
    }

    public Event save(Event event) {
        return repo.save(event);
    }

    public Optional<Event> findById(Long id) {
        return repo.findById(id);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public List<Event> findAll() {
        return repo.findAll();
    }
}
