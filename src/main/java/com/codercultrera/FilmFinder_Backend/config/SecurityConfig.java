package com.codercultrera.FilmFinder_Backend.config;

import com.codercultrera.FilmFinder_Backend.security.JwtAuthFilter2;
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
    private final JwtAuthFilter2 jwtAuthFilter;
//    private final JwtAuthFilter2 jwtAuthFilter2;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtAuthFilter2 jwtAuthFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
//        this.jwtAuthFilter2 = jwtAuthFilter2;
    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(customUserDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        AuthenticationManagerBuilder authManager = http.getSharedObject(AuthenticationManagerBuilder.class);
//        authManager.authenticationProvider(authProvider);
//        return authManager.build();
//    }

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
//                .cors(Customizer.withDefaults())
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests((request) -> {
                    request
                            .requestMatchers(
//                                    HttpMethod.POST,
                                    "/api/auth/check-email",
                                    "/api/auth/login",
                                    "/api/auth/register",
                                    "/api/auth/google")
                            .permitAll()

                            .requestMatchers(
                                    HttpMethod.OPTIONS)
                            .permitAll()

                            .requestMatchers(
                                    "/admin/**")
                            .hasRole("ADMIN")

                            .requestMatchers(
                                    HttpMethod.GET,
                                    "/api/auth/favorites",
                                    "/api/auth/uploadProfilePhoto")
//                            .permitAll()
                            .hasAnyRole("USER")

                            .requestMatchers(
                                    HttpMethod.POST,
                                    "/api/auth/addFavorite",
                                    "/api/auth/removeFavorite",
                                    "/api/auth/uploadProfilePhoto",
                                    "/api/auth/hello")
                            .hasAnyRole("USER")

                            .requestMatchers(
                                    HttpMethod.DELETE,
                                    "/api/auth/uploadProfilePhoto")
                            .hasRole("ADMIN")

                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//                .logout()
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/home")
//                .invalidateHttpSession(true)
//                .clearAuthentication(true)
//                .deleteCookies("JSESSIONID")

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://codercultrera-filmfinder.netlify.app", "http://localhost:8080"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "Access-Control-Allow-Headers", "Access-Control-Allow-Origin"));
        configuration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
