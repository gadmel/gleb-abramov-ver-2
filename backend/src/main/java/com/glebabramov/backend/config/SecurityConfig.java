package com.glebabramov.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	static final String ADMIN_ROLE = "ADMIN";

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
		requestHandler.setCsrfRequestAttributeName(null);

		return http
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(requestHandler))
				.httpBasic()
				.authenticationEntryPoint((request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase()))
				.and()
				.sessionManagement(config ->
						config.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
				.authorizeHttpRequests()
				.requestMatchers(HttpMethod.GET, "/api/csrf/").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/users/login/").permitAll()
				.requestMatchers("/api/**").authenticated()
				.requestMatchers("/api/admin/**").hasRole(ADMIN_ROLE)
				.anyRequest().permitAll()
				.and()
				.logout(logout -> logout
						.logoutUrl("/api/users/logout/")
						.clearAuthentication(true)
						.invalidateHttpSession(true)
						.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
						.permitAll())
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
	}
}
