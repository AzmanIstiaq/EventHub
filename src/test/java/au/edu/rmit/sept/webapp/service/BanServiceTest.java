package au.edu.rmit.sept.webapp.service;

import au.edu.rmit.sept.webapp.model.Ban;
import au.edu.rmit.sept.webapp.model.BanType;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.model.UserType;
import au.edu.rmit.sept.webapp.repository.BanRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BanServiceTest {

    @Mock
    private BanRepository banRepository;

    @Mock
    private EntityManager em;

    private BanService banService;

    @BeforeEach
    void setUp() {
        banService = new BanService(banRepository);
        // Inject the mocked EntityManager using reflection
        try {
            var field = BanService.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(banService, em);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("isUserBanned() returns true when user is banned")
    void isUserBannedReturnsTrue() {
        User user = new User();
        when(banRepository.existsByUser(user)).thenReturn(true);

        boolean result = banService.isUserBanned(user);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserBanned() returns false when user is not banned")
    void isUserBannedReturnsFalse() {
        User user = new User();
        when(banRepository.existsByUser(user)).thenReturn(false);

        boolean result = banService.isUserBanned(user);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findBanIdByUser() returns ban ID")
    void findBanIdByUserReturnsBanId() {
        User user = new User();
        Ban ban = new Ban();
        ban.setBanId(123L);
        when(banRepository.findByUser(user)).thenReturn(ban);

        long result = banService.findBanIdByUser(user);

        assertThat(result).isEqualTo(123L);
    }

    @Test
    @DisplayName("deleteBanById() calls repository delete")
    void deleteBanByIdCallsRepository() {
        long banId = 456L;

        banService.deleteBanById(banId);

        verify(banRepository).deleteById(banId);
    }

    @Test
    @DisplayName("banUser() throws exception when user already banned")
    void banUserThrowsWhenAlreadyBanned() {
        User user = new User();
        Ban ban = new Ban();
        ban.setUser(user);
        when(banRepository.existsByUser(user)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when trying to ban admin")
    void banUserThrowsWhenBanningAdmin() {
        User adminUser = new User();
        adminUser.setRole(UserType.ADMIN);
        Ban ban = new Ban();
        ban.setUser(adminUser);
        when(banRepository.existsByUser(adminUser)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when ban type is null")
    void banUserThrowsWhenBanTypeNull() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(null);
        when(banRepository.existsByUser(user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when ban reason is null or empty")
    void banUserThrowsWhenBanReasonEmpty() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(BanType.PERMANENT);
        ban.setBanReason("");
        when(banRepository.existsByUser(user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when admin is null or not admin")
    void banUserThrowsWhenAdminInvalid() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        User nonAdmin = new User();
        nonAdmin.setRole(UserType.STUDENT);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(BanType.PERMANENT);
        ban.setBanReason("Violation");
        ban.setAdmin(nonAdmin);
        when(banRepository.existsByUser(user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when temporary ban has no end date")
    void banUserThrowsWhenTemporaryBanNoEndDate() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        User admin = new User();
        admin.setRole(UserType.ADMIN);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(BanType.TEMPORARY);
        ban.setBanReason("Violation");
        ban.setAdmin(admin);
        ban.setBanEndDate(null);
        when(banRepository.existsByUser(user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() throws exception when temporary ban end date is too soon")
    void banUserThrowsWhenTemporaryBanEndDateTooSoon() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        User admin = new User();
        admin.setRole(UserType.ADMIN);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(BanType.TEMPORARY);
        ban.setBanReason("Violation");
        ban.setAdmin(admin);
        ban.setBanEndDate(LocalDateTime.now().plusMinutes(30)); // Less than 1 hour
        when(banRepository.existsByUser(user)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> banService.banUser(ban));
    }

    @Test
    @DisplayName("banUser() successfully bans user with valid data")
    void banUserSuccessfullyBansUser() {
        User user = new User();
        user.setRole(UserType.STUDENT);
        User admin = new User();
        admin.setRole(UserType.ADMIN);
        Ban ban = new Ban();
        ban.setUser(user);
        ban.setBanType(BanType.PERMANENT);
        ban.setBanReason("Violation");
        ban.setAdmin(admin);
        when(banRepository.existsByUser(user)).thenReturn(false);

        banService.banUser(ban);

        verify(banRepository).save(ban);
        assertThat(user.getBan()).isEqualTo(ban);
    }

    @Test
    @DisplayName("removeBan() throws exception when user is not banned")
    void removeBanThrowsWhenUserNotBanned() {
        User user = new User();
        when(banRepository.findByUser(user)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> banService.removeBan(user));
    }

    @Test
    @DisplayName("removeBan() successfully removes ban")
    void removeBanSuccessfullyRemovesBan() {
        User user = new User();
        Ban ban = new Ban();
        when(banRepository.findByUser(user)).thenReturn(ban);

        banService.removeBan(user);

        verify(banRepository).delete(ban);
        verify(em).flush();
        assertThat(user.getBan()).isNull();
    }

    @Test
    @DisplayName("updateBan() saves updated ban")
    void updateBanSavesUpdatedBan() {
        Ban ban = new Ban();

        banService.updateBan(ban);

        verify(banRepository).save(ban);
    }

    @Test
    @DisplayName("expireTemporaryBans() handles empty list")
    void expireTemporaryBansHandlesEmptyList() {
        jakarta.persistence.TypedQuery<Long> mockQuery = mock(jakarta.persistence.TypedQuery.class);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of());

        banService.expireTemporaryBans();

        verify(em, never()).flush();
    }

    @Test
    @DisplayName("expireTemporaryBans() deletes expired bans")
    void expireTemporaryBansDeletesExpiredBans() {
        jakarta.persistence.TypedQuery<Long> selectQuery = mock(jakarta.persistence.TypedQuery.class);
        Query deleteQuery = mock(Query.class);
        when(em.createQuery(contains("SELECT"), eq(Long.class))).thenReturn(selectQuery);
        when(em.createQuery(contains("DELETE"))).thenReturn(deleteQuery);
        when(selectQuery.setParameter(anyString(), any())).thenReturn(selectQuery);
        when(deleteQuery.setParameter(anyString(), any())).thenReturn(deleteQuery);
        when(selectQuery.getResultList()).thenReturn(List.of(1L, 2L));
        when(deleteQuery.executeUpdate()).thenReturn(2);

        banService.expireTemporaryBans();

        verify(deleteQuery).executeUpdate();
        verify(em).flush();
    }
}
