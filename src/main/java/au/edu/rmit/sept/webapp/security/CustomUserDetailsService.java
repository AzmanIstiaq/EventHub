package au.edu.rmit.sept.webapp.security;

import au.edu.rmit.sept.webapp.model.Ban;
import au.edu.rmit.sept.webapp.model.BanType;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import au.edu.rmit.sept.webapp.service.BanService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BanService banService;

    public CustomUserDetailsService(UserRepository userRepository, BanService banService) {
        this.userRepository = userRepository;
        this.banService = banService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Expire any active temporary bans if necessary
        Ban ban = user.getBan();
        if (ban != null
            && ban.getBanType() == BanType.TEMPORARY
            && ban.getBanEndDate() != null
            && ban.getBanEndDate().isBefore(java.time.LocalDateTime.now())) {
            banService.removeBan(user);
        }
        return new CustomUserDetails(user);
    }

}
