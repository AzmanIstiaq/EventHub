package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    // All upcoming events for a given organiser
    List<Event> findByOrganiserAndEventDateAfterOrderByEventDateAsc(User organiser, LocalDateTime EventDate);
    // All past events for a given organiser
    List<Event> findByOrganiserAndEventDateBeforeOrderByEventDateAsc(User organiser, LocalDateTime EventDate);

    // All upcoming events (public view)
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime EventDate);
}
