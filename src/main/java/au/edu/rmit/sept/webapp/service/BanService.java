    package au.edu.rmit.sept.webapp.service;

    import au.edu.rmit.sept.webapp.model.Ban;
    import au.edu.rmit.sept.webapp.model.BanType;
    import au.edu.rmit.sept.webapp.model.User;
    import au.edu.rmit.sept.webapp.model.UserType;
    import au.edu.rmit.sept.webapp.repository.BanRepository;
    import jakarta.persistence.PersistenceContext;
    import org.springframework.stereotype.Service;
    import jakarta.persistence.EntityManager;
    import org.springframework.transaction.annotation.Propagation;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class BanService {

        private final BanRepository banRepository;

        @PersistenceContext
        private EntityManager em;

        public BanService(BanRepository banRepository) {
            this.banRepository = banRepository;
        }

        public boolean isUserBanned(User user) {
            return banRepository.existsByUser(user);
        }

        public long findBanIdByUser(User user) {
            return banRepository.findByUser(user).getBanId();
        }

        public void deleteBanById(long banId) {
            banRepository.deleteById(banId);
        }


        @org.springframework.transaction.annotation.Transactional
        public void banUser(Ban ban) {
            User user = ban.getUser();
            if (isUserBanned(user)) {
                throw new IllegalArgumentException("User is already banned.");
            }
            if (ban.getUser().getRole() == UserType.ADMIN) {
                throw new IllegalArgumentException("Cannot ban an admin user.");
            }
            if (ban.getBanType() == null) {
                throw new IllegalArgumentException("Ban type cannot be null.");
            }
            if (ban.getBanReason() == null || ban.getBanReason().isEmpty()) {
                throw new IllegalArgumentException("Ban reason cannot be null or empty.");
            }
            if (ban.getAdmin() == null || ban.getAdmin().getRole() != UserType.ADMIN) {
                throw new IllegalArgumentException("Only admin users can issue bans.");
            }
            if (ban.getBanType() == BanType.TEMPORARY && ban.getBanEndDate() == null) {
                throw new IllegalArgumentException("Ban length cannot be null when ban type is temporary.");
            }
            if (ban.getBanType() == BanType.TEMPORARY && ban.getBanEndDate().isBefore(LocalDateTime.now().plusHours(1L))) {
                throw new IllegalArgumentException("Ban end date must be at least 1 hour in the future for temporary bans.");
            }

            user.setBan(ban);
            banRepository.save(ban);
        }

        @org.springframework.transaction.annotation.Transactional
        public void removeBan(User user) {
            Ban ban = banRepository.findByUser(user);
            if (ban == null) throw new IllegalArgumentException("User is not banned.");
            // Delete the ban row and set user's ban to null
            banRepository.delete(ban);
            user.setBan(null);
            em.flush();
        }

        public void updateBan(Ban existingBan) {
            banRepository.save(existingBan);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void expireTemporaryBans() {
            // First get the user IDs with expired bans
            List<Long> userIdsWithExpiredBans = em.createQuery(
                            "SELECT b.user.userId FROM Ban b WHERE b.banType = :banType AND b.banEndDate < :now",
                            Long.class)
                    .setParameter("banType", BanType.TEMPORARY)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();

            if (userIdsWithExpiredBans.isEmpty()) return;

            // Then delete the bans directly using JPQL
            em.createQuery("DELETE FROM Ban b WHERE b.user.userId IN :userIds")
                    .setParameter("userIds", userIdsWithExpiredBans)
                    .executeUpdate();

            // Only flush, don't clear - this ensures changes are persisted
            // but doesn't detach entities from the calling transaction
            em.flush();
        }

    }
