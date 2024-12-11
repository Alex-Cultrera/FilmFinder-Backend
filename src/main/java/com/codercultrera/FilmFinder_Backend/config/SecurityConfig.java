package com.codercultrera.FilmFinder_Backend.config;

import com.codercultrera.FilmFinder_Backend.security.JwtAuthFilter;
import com.codercultrera.FilmFinder_Backend.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000")); // will need to add domains in future
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "Access-Control-Allow-Headers", "Access-Control-Allow-Origin"));
        configuration.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests((request) -> {
                    request
                        .requestMatchers("/home","/register","/login", "/api/auth/check-email","/api/auth/login", "/api/auth/register","/movies/**").permitAll()
                        .requestMatchers("/dashboard/**", "/api/auth/name").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,"/reviews/**", "/api/auth/name","/api/auth/name/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/reviews/**", "/api/auth/name","/api/auth/name/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS,"/reviews/**", "/api/auth/name","/api/auth/name/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**", "/reviews/**").hasRole("ADMIN")
                        .anyRequest().authenticated();
            })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .formLogin()
//                .loginPage("/api/authenticate/login")
//                .permitAll()
//                .defaultSuccessUrl("/dashboard", true)
//            .and()
//                .oauth2Login()
//                .loginPage("/login")
//                .permitAll()
//            .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        AuthenticationManagerBuilder authManager = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManager.authenticationProvider(authProvider);
        return authManager.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


}
