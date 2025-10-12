package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.BanRepository;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Test
    @DisplayName("CustomUserDetailsService: loadUserByUsername returns CustomUserDetails")
    void loadsUser() {
        UserRepository repo = mock(UserRepository.class);
        BanService banService = mock(BanService.class);
        CustomUserDetailsService svc = new CustomUserDetailsService(repo, banService);

        User u = new User();
        u.setUserId(5L);
        u.setEmail("user@ex.com");
        u.setPassword("pw");
        u.setRole(UserType.ORGANISER);
        when(repo.findByEmail("user@ex.com")).thenReturn(Optional.of(u));

        UserDetails d = svc.loadUserByUsername("user@ex.com");
        assertThat(d).isInstanceOf(CustomUserDetails.class);
        assertThat(((CustomUserDetails)d).getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("CustomUserDetailsService: throws UsernameNotFound when missing")
    void throwsWhenMissing() {
        UserRepository repo = mock(UserRepository.class);
        BanService banService = mock(BanService.class);
        CustomUserDetailsService svc = new CustomUserDetailsService(repo, banService);
        when(repo.findByEmail("missing@ex.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> svc.loadUserByUsername("missing@ex.com"));
    }
}
