package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // All upcoming events for a given organiser
    List<Event> findByOrganiserAndDateTimeAfterOrderByDateTimeAsc(User organiser, LocalDateTime dateTime);
    // All past events for a given organiser
    List<Event> findByOrganiserAndDateTimeBeforeOrderByDateTimeAsc(User organiser, LocalDateTime dateTime);
    // all past events
    List<Event> findByDateTimeBeforeOrderByDateTimeAsc(LocalDateTime dateTime);

    // All upcoming events (public view)
    List<Event> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    @Query("SELECT e FROM Event e " +
            "WHERE e.dateTime >= :from " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:query IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR EXISTS (SELECT k FROM e.keywords k WHERE LOWER(k.name) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<Event> searchEvents(@Param("query") String query,
                             @Param("from") LocalDateTime from,
                             @Param("categoryId") Long categoryId);

    @Query("SELECT e FROM Event e " +
            "WHERE e.dateTime BETWEEN :from AND :to " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
            "AND (:query IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "     OR EXISTS (SELECT k FROM e.keywords k WHERE LOWER(k.name) LIKE LOWER(CONCAT('%', :query, '%'))))")
    List<Event> searchEventsWithEnd(@Param("query") String query,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to,
                                    @Param("categoryId") Long categoryId);
}
