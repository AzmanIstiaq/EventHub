package au.edu.rmit.sept.webapp.security;

import au.edu.rmit.sept.webapp.model.User;
import au.edu.rmit.sept.webapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Single role from your enum
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getType().name()));
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Long getId() { return user.getId(); } // custom getter

//    @Service
//    public static class CustomUserDetailsService implements UserDetailsService {
//
//        private final UserRepository userRepository;
//
//        public CustomUserDetailsService(UserRepository userRepository) {
//            this.userRepository = userRepository;
//        }
//
//        @Override
//        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//            User user = userRepository.findByEmail(email)
//                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//            return new CustomUserDetails(user);
//        }
//    }
}
