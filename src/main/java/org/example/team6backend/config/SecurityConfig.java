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
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/", "/index", "/demo", "/error", "/login/**", "/oauth2/**", "/dev/**")
						.permitAll().requestMatchers("/dashboard.html", "/profile.html", "/viewincident.html")
						.authenticated().requestMatchers("/incidents.html/**", "/api/incidents/**")
						.hasAnyRole("RESIDENT", "HANDLER", "ADMIN").requestMatchers("/admin.html", "/api/admin/**")
						.hasRole("ADMIN").requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
						.hasRole("ADMIN").anyRequest().authenticated())
				.oauth2Login(
						oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
								.defaultSuccessUrl("/dashboard.html", true))
				.logout(logout -> logout.logoutSuccessUrl("/").invalidateHttpSession(true).clearAuthentication(true)
						.deleteCookies("JSESSIONID"))
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/admin/**", "/api/incidents/**", "/api/documents/**",
						"/dev/**"));

		return http.build();
	}
}
