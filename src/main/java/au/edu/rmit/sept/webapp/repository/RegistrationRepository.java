package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // Registrations for a specific user
    List<Registration> findByUser(User user);

    // Registrations for a specific event
    List<Registration> findByEvent(Event event);

    List<Registration> findByEvent_EventId(Long eventId);

    List<Registration> findByUser_UserId(Long userId);
    // Check if a user is already registered for an event
    boolean existsByUserAndEvent(User user, Event event);

    @Transactional
    @Modifying
    @Query("DELETE FROM Registration r WHERE r.user.userId = :userId AND r.event.eventId = :eventId")
    int deleteRegistration(Long userId, Long eventId);

    boolean existsByEvent_EventIdAndUser_UserId(Long eventId, Long userId);

    Optional<Registration> findByUser_UserIdAndEvent_EventId(Long userId, Long eventId);

}
