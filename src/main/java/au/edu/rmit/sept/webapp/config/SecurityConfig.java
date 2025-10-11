package au.edu.rmit.sept.webapp.config;

import au.edu.rmit.sept.webapp.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/image/**").permitAll()
                        .requestMatchers("/events/public/**").permitAll()
                        .requestMatchers("/users/profile/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/events/*/gallery/upload").hasRole("ORGANISER")
                        .requestMatchers("/events/*/gallery/**").permitAll()
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/events/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            var auth = authentication.getAuthorities().iterator().next().getAuthority();
                            if (auth.equals("ROLE_ADMIN") ||  auth.equals("ROLE_ORGANISER") || auth.equals("ROLE_STUDENT")) {
                                response.sendRedirect("/events");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .failureHandler((request, response, authentication) -> {
                            String redirectUrl = "/login?error=true";

                            // Check if the exception is due to a locked account
                            if (authentication.getClass().getSimpleName().equals("LockedException")) {
                                // If so, find out what type of ban it is (permanent or temporary)
                                var userDetails = userDetailsService.loadUserByUsername(request.getParameter("username"));
                                if (userDetails != null) {
                                    var customDetails = (au.edu.rmit.sept.webapp.security.CustomUserDetails) userDetails;
                                    var ban = customDetails.getUser().getBan();
                                    if (ban != null && ban.getBanType() != null) {
                                        if (ban.getBanType() == au.edu.rmit.sept.webapp.model.BanType.PERMANENT) {
                                            redirectUrl = "/login?error=permanent_ban";
                                        } else if (ban.getBanType() == au.edu.rmit.sept.webapp.model.BanType.TEMPORARY) {
                                            redirectUrl = "/login?error=temporary_ban";
                                        }
                                    }
                                }
                            }

                            response.sendRedirect(redirectUrl);
                        })
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/error/403")
                        )
                )
                .logout(logout -> logout.permitAll())
                .headers(headers -> headers.frameOptions().sameOrigin());  // <- Allow H2 console frames


        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
