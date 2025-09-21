package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Integer> {
    Keyword findByKeyword(String name);
}
