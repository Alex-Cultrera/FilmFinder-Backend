package com.codercultrera.FilmFinder_Backend.security;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.jsonwebtoken.Jwts.*;

@Slf4j
@Component
public class JwtUtil {

    private final UserService userService;
    @Value("${jwt.secret}")
    private String secretKey;

    private final SecretKey secretKeyForSigning;

    public JwtUtil(@Value("${jwt.secret}")String secretKey, UserService userService) {
        if (secretKey.length() < 32) {
            throw new IllegalArgumentException("The secret key should be at least 256 bits (32 bytes) long");
        }
        this.secretKeyForSigning = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.userService = userService;
    }

//    private final long accessTokenValidity = 15L * 60L * 1000L; // 15 minutes
    private final long accessTokenValidity = 5000L;
    private final long refreshTokenValidity = 14L * 24L * 60L * 60L * 1000L; // 2 weeks

    public String generateAccessToken(User user) {
        return builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .claim("userId", user.getUserId())
                .claim("roles", user.getRoles().stream().map(Role::getRoleType).collect(Collectors.toList()))
                .signWith(secretKeyForSigning, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .claim("userId", user.getUserId())
                .claim("roles", user.getRoles().stream().map(Role::getRoleType).collect(Collectors.toList()))
                .signWith(secretKeyForSigning, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        return authorizationHeader.substring(7);
    }

    public User getUserFromToken(HttpServletRequest request, UserDetails userDetails) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        if (validateToken(token)) {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKeyForSigning)
                    .parseClaimsJws(token)
                    .getBody();
            return userService.findByEmail(claims.getSubject());
        } else {
            return null;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token) {
        try {
//            final String username = extractUsername(token);
//            return (username.equals(userDetails.getUsername()));

            Jwts.parser()
                    .setSigningKey(secretKeyForSigning)
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.error("Invalid token: {}", e.getMessage());
        }
        return false;
    }

    public Claims extractClaims(String token) {
        return parserBuilder()
                .setSigningKey(secretKeyForSigning)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Set<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        return new HashSet<>(claims.get("roles", Set.class));
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

}
