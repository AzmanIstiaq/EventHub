package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Ban;
import au.edu.rmit.sept.webapp.model.BanType;
import au.edu.rmit.sept.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BanRepository extends JpaRepository<Ban, Long> {
    boolean existsByUser(User user);

    Ban findByUser(User user);

    // Find all temporary bans that end before right now (i.e. expired)
    List<Ban> findAllByBanTypeAndBanEndDateBefore(BanType banType, LocalDateTime banEndDate);
}
