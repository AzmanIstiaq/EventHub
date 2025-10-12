package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategory(String category);

}
