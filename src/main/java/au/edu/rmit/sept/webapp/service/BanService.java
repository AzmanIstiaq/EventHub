    package au.edu.rmit.sept.webapp.service;

    import au.edu.rmit.sept.webapp.model.Ban;
    import au.edu.rmit.sept.webapp.model.User;
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
