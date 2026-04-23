package org.example.team6backend.config;

import lombok.RequiredArgsConstructor;
import org.example.team6backend.security.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/dashboard.html", "/incidents.html",
                                "/profile.html", "/createincident.html", "/viewincident.html",
                                "/admin.html", "/demo.html", "/error", "/login/**",
                                "/oauth2/**", "/dev/**", "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/api/incidents/**").hasAnyRole("RESIDENT", "HANDLER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers("/comments/**", "/activity/**", "/documents/**").authenticated()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .defaultSuccessUrl("/dashboard.html", true)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/admin/**", "/api/incidents/**", "/comments", "/dev/**")
                );

        return http.build();
    }
}