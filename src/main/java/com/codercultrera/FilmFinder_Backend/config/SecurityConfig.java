package com.codercultrera.FilmFinder_Backend.config;

import com.codercultrera.FilmFinder_Backend.security.JwtAuthFilter;
import com.codercultrera.FilmFinder_Backend.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthFilter jwtAuthFilter) {
                this.customUserDetailsService = customUserDetailsService;
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                                .authorizeHttpRequests((request) -> {
                                        request
                                                        // Debug endpoint to check token
                                                        .requestMatchers("/api/auth/check-token").authenticated()

                                                        .requestMatchers(
                                                                        "/api/auth/check-email",
                                                                        "/api/auth/login",
                                                                        "/api/auth/logout",
                                                                        "/api/auth/register",
                                                                        "/recommended",
                                                                        "/queued",
                                                                        "/watched",
                                                                        "/favorites",
                                                                        "/api/auth/google",
                                                                        "/review/movies/{movieId}/all",
                                                                        "/omdb/api/**")
                                                        .permitAll()

                                                        .requestMatchers(
                                                                        HttpMethod.OPTIONS)
                                                        .permitAll()

                                                        .requestMatchers(
                                                                        HttpMethod.GET,
                                                                        "/user/uploadProfilePhoto",
                                                                        "/review/user/all")
                                                        .hasAnyRole("USER", "ADMIN")

                                                        .requestMatchers(
                                                                        HttpMethod.GET,
                                                                        "/review/all")
                                                        .hasAnyRole("ADMIN")

                                                        .requestMatchers(
                                                                        HttpMethod.POST,
                                                                        "/addQueued",
                                                                        "/addWatched",
                                                                        "/addFavorite",
                                                                        "/removeQueued",
                                                                        "/removeWatched",
                                                                        "/removeFavorite",
                                                                        "/review/movies/{movieId}/new",
                                                                        "/api/photos/upload",
                                                                        "/user/uploadProfilePhoto")
                                                        .hasAnyRole("USER", "ADMIN")

                                                        .requestMatchers(
                                                                        HttpMethod.POST,
                                                                        "/addRecommended",
                                                                        "/removeRecommended")
                                                        .hasAnyRole("ADMIN")

                                                        .requestMatchers(
                                                                        HttpMethod.PUT,
                                                                        "/review/{reviewId}/update",
                                                                        "/user/password/update")
                                                        .hasAnyRole("USER", "ADMIN")

                                                        .requestMatchers(
                                                                        HttpMethod.DELETE,
                                                                        "/review/{reviewId}/delete")
                                                        .hasAnyRole("USER", "ADMIN")

                                                        .anyRequest().authenticated();
                                })
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowCredentials(true);
                configuration.setAllowedOrigins(
                                List.of("https://codercultrera-filmfinder.com",
                                                "http://localhost:3000"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
                configuration.setExposedHeaders(List.of("Authorization", "Access-Control-Allow-Origin",
                                "Access-Control-Allow-Credentials", "Set-Cookie"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

}
