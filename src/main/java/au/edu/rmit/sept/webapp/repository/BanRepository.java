package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Ban;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BanRepository extends JpaRepository<Ban, Long> {
    boolean existsByUser(User user);

    Ban findByUser(User user);
}
