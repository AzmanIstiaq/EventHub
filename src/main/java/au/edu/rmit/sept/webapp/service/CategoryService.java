package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllCategories();
    }

    public Optional<Category> findById(int id) {
        return categoryRepository.findById(id);
    }

    public Category findByName(String category) {
        return categoryRepository.findByCategory(category);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }
}
