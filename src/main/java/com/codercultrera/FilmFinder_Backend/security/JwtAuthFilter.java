package com.codercultrera.FilmFinder_Backend.security;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7).trim();
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt)) {
                List<GrantedAuthority> authorities = extractAuthoritiesFromJwt(jwt);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                authenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
                return;
            }
        }
        filterChain.doFilter(request,response);
    }

    private List<GrantedAuthority> extractAuthoritiesFromJwt(String jwt) {
        Claims claims = jwtUtil.extractClaims(jwt);
        List<String> roles = claims.get("roles", List.class);


        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }



//    private final UserDetailsService userDetailsService;
//    private final JwtUtil jwtUtil;
//
//    public JwtAuthFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
//        this.userDetailsService = userDetailsService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//
//        String accessToken = CookieUtils.getTokenFromCookie(request, "accessToken");
//        String refreshToken = CookieUtils.getTokenFromCookie(request, "refreshToken");
//
//        if (accessToken != null) {
//            if (jwtUtil.isTokenExpired(accessToken) && refreshToken != null && jwtUtil.validateToken(refreshToken)) {
//                // Access token is expired but refresh token is valid; generate new access token
//                String username = jwtUtil.extractUsername(refreshToken);
//                User user = (User) userDetailsService.loadUserByUsername(username);
//                String newAccessToken = jwtUtil.generateAccessToken(user);
//                CookieUtils.setCookie(response, "accessToken", newAccessToken);
//
//                accessToken = newAccessToken; // Update to use the new token
//            }
//
//            if (jwtUtil.validateToken(accessToken)) {
//                String username = jwtUtil.extractUsername(accessToken);
//                User user = (User) userDetailsService.loadUserByUsername(username);
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        user, null, null);
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//
//












//        final String authorizationHeader = request.getHeader("Authorization");
//
//        String username = null;
//        String jwt = null;
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                jwt = authorizationHeader.substring(7).trim();
//                username = jwtUtil.extractUsername(jwt);
//        }
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
//
//            if (jwtUtil.validateToken(jwt)) {
//                List<GrantedAuthority> authorities = extractAuthoritiesFromJwt(jwt);
//
//                UsernamePasswordAuthenticationToken authenticationToken =
//                        new UsernamePasswordAuthenticationToken(
//                                username, null, authorities);
//
//                authenticationToken
//                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//
//            } else {
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token");
//                return;
//            }
//        }
//        filterChain.doFilter(request,response);
//    }

//    private List<GrantedAuthority> extractAuthoritiesFromJwt(String jwt) {
//        Claims claims = jwtUtil.extractClaims(jwt);
//        List<String> roles = claims.get("roles", List.class);
//        if (roles == null) {
//            return Collections.emptyList();
//        }
//        return roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }

}
