package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.OrganiserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrganiserServiceTest {

    private UserRepository repo;
    private OrganiserService service;

    @BeforeEach
    void setup() {
        repo = mock(UserRepository.class);
        service = new OrganiserService(repo);
    }

    @Test
    @DisplayName("save(): delegates to repo")
    void saveDelegates() {
        User u = new User();
        when(repo.save(u)).thenReturn(u);
        assertThat(service.save(u)).isSameAs(u);
        verify(repo).save(u);
    }

    @Test
    @DisplayName("findById(): returns Optional")
    void findByIdOptional() {
        User u = new User(); u.setId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(u));
        assertThat(service.findById(10L)).contains(u);
    }

    @Test
    @DisplayName("findAll(): returns list")
    void findAllReturns() {
        when(repo.findAll()).thenReturn(List.of(new User(), new User(), new User()));
        assertThat(service.findAll()).hasSize(3);
    }
}
