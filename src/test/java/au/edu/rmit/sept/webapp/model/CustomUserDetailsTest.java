package au.edu.rmit.sept.webapp.model;

import au.edu.rmit.sept.webapp.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("CustomUserDetails: authorities and getters")
    void authoritiesAndGetters() {
        User u = new User();
        u.setUserId(99L);
        u.setEmail("user@ex.com");
        u.setPassword("pw");
        u.setRole(UserType.STUDENT);

        CustomUserDetails cud = new CustomUserDetails(u);
        assertThat(cud.getId()).isEqualTo(99L);
        assertThat(cud.getUsername()).isEqualTo("user@ex.com");
        assertThat(cud.getPassword()).isEqualTo("pw");
        Collection<? extends GrantedAuthority> auths = cud.getAuthorities();
        assertThat(auths).hasSize(1);
        assertThat(auths.iterator().next().getAuthority()).isEqualTo("ROLE_STUDENT");
        assertThat(cud.isAccountNonExpired()).isTrue();
        assertThat(cud.isAccountNonLocked()).isTrue();
        assertThat(cud.isCredentialsNonExpired()).isTrue();
        assertThat(cud.isEnabled()).isTrue();
    }
}
