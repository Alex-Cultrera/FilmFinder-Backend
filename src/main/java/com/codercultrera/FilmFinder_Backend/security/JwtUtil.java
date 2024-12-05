package com.codercultrera.FilmFinder_Backend.security;

import com.codercultrera.FilmFinder_Backend.domain.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.Jwts.*;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.token.expiry}")
    private long tokenExpiry;

    private final long accessTokenValidity = 15L * 60L * 1000L; // 15 minutes
    private final long refreshTokenValidity = 14L * 24L * 60L * 60L * 1000L; // 2 weeks

    public String generateAccessToken(User user) {
        return builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .claim("userId", user.getUserId())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .claim("userId", user.getUserId())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
        } catch (JwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
        }
        return false;
    }

    public Claims extractClaims(String token) {
        return parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

}
