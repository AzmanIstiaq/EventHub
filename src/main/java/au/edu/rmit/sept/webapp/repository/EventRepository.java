package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // All upcoming events for a given organiser
    List<Event> findByOrganiserAndDateTimeAfterOrderByDateTimeAsc(User organiser, LocalDateTime dateTime);
    // All past events for a given organiser
    List<Event> findByOrganiserAndDateTimeBeforeOrderByDateTimeAsc(User organiser, LocalDateTime dateTime);

    // All upcoming events (public view)
    List<Event> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    // All events ordered by date descending (most recent first)
    List<Event> findAllByOrderByDateTimeDesc();

    // All events for a given organiser ordered by date descending
    List<Event> findByOrganiserOrderByDateTimeDesc(User organiser);

    // All past events
    List<Event> findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime dateTime);

    // Search events by title (case-insensitive)
    List<Event> findByTitleContainingIgnoreCase(String title);

    // Find events by location
    List<Event> findByLocationContainingIgnoreCase(String location);

    // Find events between dates
    List<Event> findByDateTimeBetweenOrderByDateTimeAsc(LocalDateTime startDate, LocalDateTime endDate);
}
