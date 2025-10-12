package au.edu.rmit.sept.webapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserModelTest {

    @Test
    @DisplayName("User getters and setters work correctly")
    void userGettersSetters() {
        User user = new User();
        Ban ban = new Ban();
        Set<Registration> registrations = new HashSet<>();
        Set<Event> organisedEvents = new HashSet<>();
        
        user.setUserId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRole(UserType.STUDENT);
        user.setBan(ban);
        user.setRegistrations(registrations);
        user.setOrganisedEvents(organisedEvents);

        assertThat(user.getUserId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getRole()).isEqualTo(UserType.STUDENT);
        assertThat(user.getBan()).isEqualTo(ban);
        assertThat(user.getRegistrations()).isEqualTo(registrations);
        assertThat(user.getOrganisedEvents()).isEqualTo(organisedEvents);
    }

    @Test
    @DisplayName("User constructor with parameters works correctly")
    void userConstructorWithParameters() {
        User user = new User("Jane Doe", "jane@example.com", "password456", UserType.ORGANISER);
        
        assertThat(user.getName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPassword()).isEqualTo("password456");
        assertThat(user.getRole()).isEqualTo(UserType.ORGANISER);
        assertThat(user.getRegistrations()).isNotNull();
        assertThat(user.getOrganisedEvents()).isNotNull();
    }

    @Test
    @DisplayName("User isAdmin() returns true for admin users")
    void userIsAdminReturnsTrueForAdmin() {
        User admin = new User();
        admin.setRole(UserType.ADMIN);
        
        assertThat(admin.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("User isAdmin() returns false for non-admin users")
    void userIsAdminReturnsFalseForNonAdmin() {
        User student = new User();
        student.setRole(UserType.STUDENT);
        
        User organiser = new User();
        organiser.setRole(UserType.ORGANISER);
        
        assertThat(student.isAdmin()).isFalse();
        assertThat(organiser.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("User helper methods work correctly")
    void userHelperMethods() {
        User student = new User();
        student.setRole(UserType.STUDENT);
        
        User organiser = new User();
        organiser.setRole(UserType.ORGANISER);
        
        assertThat(student.isStudent()).isTrue();
        assertThat(student.isOrganiser()).isFalse();
        assertThat(student.isAdmin()).isFalse();
        
        assertThat(organiser.isOrganiser()).isTrue();
        assertThat(organiser.isStudent()).isFalse();
        assertThat(organiser.isAdmin()).isFalse();
    }
}
