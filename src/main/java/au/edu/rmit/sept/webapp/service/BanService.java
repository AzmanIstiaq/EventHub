    package au.edu.rmit.sept.webapp.service;

    import au.edu.rmit.sept.webapp.model.Ban;
    import au.edu.rmit.sept.webapp.model.BanType;
    import au.edu.rmit.sept.webapp.model.User;
    import au.edu.rmit.sept.webapp.model.UserType;
    import au.edu.rmit.sept.webapp.repository.BanRepository;
    import jakarta.transaction.Transactional;
    import org.springframework.stereotype.Service;

    @Service
    public class BanService {

        private final BanRepository banRepository;

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


        @Transactional
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

            user.setBan(ban);
            banRepository.save(ban);
        }

        @Transactional
        public void removeBan(User user) {
            if (!isUserBanned(user)) {
                throw new IllegalArgumentException("User is not banned.");
            }
            long banId = findBanIdByUser(user);
            user.setBan(null);
            deleteBanById(banId);
        }
    }
