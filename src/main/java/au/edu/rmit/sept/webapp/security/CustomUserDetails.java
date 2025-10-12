package au.edu.rmit.sept.webapp.security;

import au.edu.rmit.sept.webapp.model.Ban;
import au.edu.rmit.sept.webapp.model.BanType;
import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    private boolean isBanActive() {
        Ban ban = user.getBan();
        // Check ban exists
        if (ban == null) return false;
        // Check if permanent ban
        if (ban.getBanType() == BanType.PERMANENT) return true;
        // Check if temporary ban is still active
        return ban.getBanEndDate() != null && ban.getBanEndDate().isAfter(LocalDateTime.now());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Single role from your enum
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !isBanActive(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Long getId() { return user.getUserId(); } // custom getter

}
