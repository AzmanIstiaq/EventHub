package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Keyword findByName(String name);
}
