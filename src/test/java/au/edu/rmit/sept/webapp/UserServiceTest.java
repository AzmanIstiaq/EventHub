package au.edu.rmit.sept.webapp;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("findById(): returns Optional")
    void findByIdOptional() {
        User u = new User(); u.setUserId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        assertThat(userService.findById(1L)).contains(u);
    }

    @Test
    @DisplayName("save(): delegates to repository")
    void saveDelegates() {
        User u = new User();
        when(userRepository.save(u)).thenReturn(u);
        assertThat(userService.save(u)).isSameAs(u);
        verify(userRepository).save(u);
    }

    @Test
    @DisplayName("getAllUsers(): returns list")
    void getAllUsersReturns() {
        when(userRepository.findAll()).thenReturn(List.of(new User(), new User()));
        assertThat(userService.getAllUsers()).hasSize(2);
    }
}
