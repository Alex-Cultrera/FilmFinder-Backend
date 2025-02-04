package com.codercultrera.FilmFinder_Backend.service;

import static org.springframework.web.util.WebUtils.getCookie;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.RoleType;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.ApiResponse;
import com.codercultrera.FilmFinder_Backend.dto.AuthResponse;
import com.codercultrera.FilmFinder_Backend.dto.GoogleApiRequest;
import com.codercultrera.FilmFinder_Backend.dto.LoginRequest;
import com.codercultrera.FilmFinder_Backend.dto.RegisterRequest;
import com.codercultrera.FilmFinder_Backend.security.CookieUtils;
import com.codercultrera.FilmFinder_Backend.security.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthService(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService,
            RoleService roleService, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
        this.customUserDetailsService = customUserDetailsService;
    }

    public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
        String encryptedPassword = new BCryptPasswordEncoder(12).encode(registerRequest.getPassword());
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return new ResponseEntity<>(new ApiResponse("Email is already in use"), HttpStatus.BAD_REQUEST);
        }
        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(encryptedPassword);

        Role userRole = roleService.findByRoleType(RoleType.valueOf("ROLE_USER"))
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        newUser.setRoles(new HashSet<>(Arrays.asList(userRole)));
        userService.save(newUser);

        return new ResponseEntity<>(new ApiResponse("Thank you for registering"), HttpStatus.CREATED);
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userService.findByEmail(loginRequest.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
            }

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            CookieUtils.setCookie(response, "accessToken", accessToken);
            CookieUtils.setCookie(response, "refreshToken", refreshToken);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", user.getUserId());
            responseBody.put("firstName", user.getFirstName());
            responseBody.put("photo", user.getPhoto());

            return ResponseEntity.ok(responseBody);
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    public ResponseEntity<?> continueWithGoogle(GoogleApiRequest googleApiRequest, HttpServletResponse response) {
        if (userService.existsByEmail(googleApiRequest.getUserEmail())) {
            try {
                User existingUser = userService.findByEmail(googleApiRequest.getUserEmail());

                String accessToken = jwtUtil.generateAccessToken(existingUser);
                String refreshToken = jwtUtil.generateRefreshToken(existingUser);

                Cookie cookie = new Cookie("refresh_token", refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setMaxAge(3600 * 24 * 14); // 2 weeks
                cookie.setPath("/");
                response.addCookie(cookie);

                return ResponseEntity.ok(new AuthResponse(existingUser.getUserId(), existingUser.getFirstName()));
            } catch (BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error while authenticating user");
            }
        } else {
            try {

                User newUser = new User();
                newUser.setFirstName(googleApiRequest.getFirstName());
                newUser.setLastName(googleApiRequest.getLastName());
                newUser.setEmail(googleApiRequest.getUserEmail());
                newUser.setPhoto(googleApiRequest.getUserPhoto());

                Role userRole = roleService.findByRoleType(RoleType.valueOf("USER"))
                        .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                newUser.setRoles(new HashSet<>(Arrays.asList(userRole)));
                userService.save(newUser);

                String accessToken = jwtUtil.generateAccessToken(newUser);
                String refreshToken = jwtUtil.generateRefreshToken(newUser);

                Cookie cookie = new Cookie("refresh_token", refreshToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true);
                cookie.setMaxAge(3600 * 24 * 14); // 2 weeks
                cookie.setPath("/");
                response.addCookie(cookie);

                return ResponseEntity.ok(new AuthResponse(newUser.getUserId(), newUser.getFirstName()));
            } catch (BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error while authenticating user");
            }
        }
    }

    public ResponseEntity<?> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = String.valueOf(getCookie(request, "refreshToken"));

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid refresh token.");
        }

        try {
            Claims claims = jwtUtil.extractClaims(refreshToken);

            String userEmail = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (expiration.before(new Date())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token has expired");
            }

            User user = userService.findByEmail(userEmail);
            String newAccessToken = jwtUtil.generateAccessToken(user);

            Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setMaxAge(3600 * 24 * 14); // 2 weeks
            accessTokenCookie.setPath("/");
            response.addCookie(accessTokenCookie);

            return ResponseEntity.ok(new AuthResponse(user.getUserId(), user.getFirstName()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error with token validation");
        }

    }

    public ResponseEntity<?> continueWithFacebook(String code) {
        return null;
    }

}
