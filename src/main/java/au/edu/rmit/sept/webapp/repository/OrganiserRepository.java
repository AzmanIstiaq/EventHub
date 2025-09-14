package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganiserRepository extends JpaRepository<User, Long> {
}
