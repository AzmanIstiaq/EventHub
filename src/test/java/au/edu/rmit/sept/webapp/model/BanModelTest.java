package au.edu.rmit.sept.webapp.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BanModelTest {

    @Test
    @DisplayName("Ban getters and setters work correctly")
    void banGettersSetters() {
        Ban ban = new Ban();
        User user = new User();
        User admin = new User();
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(7);
        
        ban.setBanId(1L);
        ban.setUser(user);
        ban.setAdmin(admin);
        ban.setBanType(BanType.TEMPORARY);
        ban.setBanReason("Inappropriate behavior");
        ban.setBanEndDate(endDate);

        assertThat(ban.getBanId()).isEqualTo(1L);
        assertThat(ban.getUser()).isEqualTo(user);
        assertThat(ban.getAdmin()).isEqualTo(admin);
        assertThat(ban.getBanType()).isEqualTo(BanType.TEMPORARY);
        assertThat(ban.getBanReason()).isEqualTo("Inappropriate behavior");
        assertThat(ban.getBanEndDate()).isEqualTo(endDate);
    }

    @Test
    @DisplayName("Ban constructor with parameters works correctly")
    void banConstructorWithParameters() {
        User user = new User();
        User admin = new User();
        
        Ban ban = new Ban(user, admin, BanType.PERMANENT, "Severe violation");
        
        assertThat(ban.getUser()).isEqualTo(user);
        assertThat(ban.getAdmin()).isEqualTo(admin);
        assertThat(ban.getBanType()).isEqualTo(BanType.PERMANENT);
        assertThat(ban.getBanReason()).isEqualTo("Severe violation");
        assertThat(ban.getBanEndDate()).isNull(); // Permanent ban has no end date
    }
}
