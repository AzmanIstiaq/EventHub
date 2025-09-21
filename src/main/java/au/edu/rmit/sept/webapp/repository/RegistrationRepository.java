package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.Registration;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

    // Registrations for a specific user
    List<Registration> findByStudent(User student);

    // Registrations for a specific event
    List<Registration> findByEvent(Event event);

    List<Registration> findByEvent_EventId(int eventId);

    List<Registration> findByStudent_UserId(int userId);
    // Check if a user is already registered for an event
    boolean existsByStudentAndEvent(User user, Event event);
}
