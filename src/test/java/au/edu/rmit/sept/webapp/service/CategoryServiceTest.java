package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Category;
import au.edu.rmit.sept.webapp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryRepository repo;
    private CategoryService service;

    @BeforeEach
    void setup() {
        repo = mock(CategoryRepository.class);
        service = new CategoryService(repo);
    }

    @Test
    @DisplayName("findAll(): returns categories")
    void findAllReturns() {
        when(repo.findAll()).thenReturn(List.of(new Category("A"), new Category("B")));
        assertThat(service.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("findById(): returns Optional")
    void findByIdOptional() {
        Category c = new Category("X"); c.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        assertThat(service.findById(1L)).contains(c);
    }

    @Test
    @DisplayName("findByName(): delegates to repo")
    void findByNameDelegates() {
        Category c = new Category("Y");
        when(repo.findByCategory("Y")).thenReturn(c);
        assertThat(service.findByName("Y")).isSameAs(c);
    }

    @Test
    @DisplayName("save(): delegates to repo.save")
    void saveDelegates() {
        Category c = new Category("Z");
        when(repo.save(c)).thenReturn(c);
        assertThat(service.save(c)).isSameAs(c);
        verify(repo).save(c);
    }
}
