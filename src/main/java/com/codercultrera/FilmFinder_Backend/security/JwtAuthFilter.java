package com.codercultrera.FilmFinder_Backend.security;

import com.codercultrera.FilmFinder_Backend.domain.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("JwtAuthFilter is processing the request for path: {}", request.getRequestURI());

        String path = request.getRequestURI();
        if (path.matches("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = CookieUtils.getTokenFromCookie(request, "accessToken");
        String refreshToken = CookieUtils.getTokenFromCookie(request, "refreshToken");

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtUtil.isTokenExpired(accessToken) && refreshToken != null && jwtUtil.validateToken(refreshToken)) {
                String email = jwtUtil.extractEmail(refreshToken);
                User user = (User) userDetailsService.loadUserByUsername(email);
                String newAccessToken = jwtUtil.generateAccessToken(user);
                String accessTokenCookieHeader = String.format(
                        "accessToken=%s; Path=/; HttpOnly; Secure; SameSite=None; Domain=codercultrera-filmfinder.com",
                        newAccessToken);
                response.addHeader("Set-Cookie", accessTokenCookieHeader);
                accessToken = newAccessToken;
            }

            if (jwtUtil.validateToken(accessToken)) {
                String email = jwtUtil.extractEmail(accessToken);
                User user = (User) userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
            }
        } catch (Exception e) {
            log.error("Authentication error: ", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error: " + e.getMessage());
        }
    }

    private List<GrantedAuthority> extractAuthoritiesFromJwt(String jwt) {
        Claims claims = jwtUtil.extractClaims(jwt);
        List<String> roles = (List<String>) jwtUtil.extractRoles(jwt);
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

}
