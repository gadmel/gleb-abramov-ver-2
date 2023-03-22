package com.glebabramov.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf().disable()
				.httpBasic().and()
				.authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/api/**").authenticated()
				.requestMatchers(HttpMethod.PUT, "/api/**").authenticated()
				.requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
				.anyRequest().permitAll()
				.and()
				.formLogin()
				.and().build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
