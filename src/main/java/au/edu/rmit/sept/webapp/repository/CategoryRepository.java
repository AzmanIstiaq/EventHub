package au.edu.rmit.sept.webapp.repository;

import au.edu.rmit.sept.webapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategory(String category);

    // Find all unique categories
    @Query("SELECT DISTINCT c FROM Category c")
    List<Category> findAllCategories();
}
