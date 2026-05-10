package com.approvalflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource; // ← NEW import

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // ↓ NEW — inject the CorsConfig bean
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ↓ NEW line — wire in CORS before csrf
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/auth/**",
                                "/error",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/requests").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.PUT, "/requests/*/status").hasRole("MANAGER")

                        // ↓ NEW rules for the GET endpoints
                        .requestMatchers(HttpMethod.GET, "/requests/my").hasRole("EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/requests").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/requests/stats").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/reviews/pending").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.GET,  "/reviews/all").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.GET, "/reviews/history").hasRole("REVIEWER")
                        .requestMatchers(HttpMethod.POST, "/reviews/*/decision").hasRole("REVIEWER")

                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}