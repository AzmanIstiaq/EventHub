package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Event;
import au.edu.rmit.sept.webapp.model.EventGallery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventGalleryRepository extends JpaRepository<EventGallery, Long> {
    List<EventGallery> findByEvent(Event event);
}
