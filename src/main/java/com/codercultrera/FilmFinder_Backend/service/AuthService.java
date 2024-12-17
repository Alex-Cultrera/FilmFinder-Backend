package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.RoleType;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.*;
import com.codercultrera.FilmFinder_Backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.web.util.WebUtils.getCookie;

@Slf4j
@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RoleService roleService;

    public AuthService(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService, RoleService roleService) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.roleService = roleService;
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

        Role userRole = roleService.findByRoleType(RoleType.valueOf("USER"))
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
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userService.findByEmail(loginRequest.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
            }

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            Cookie cookie = new Cookie("refresh_token", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(3600 * 24 * 14); // 2 weeks
            cookie.setPath("/");
            response.addCookie(cookie);
            return ResponseEntity.ok(new AuthResponse(accessToken, user.getUserId(), user.getFirstName()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error while authenticating user");
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

                return ResponseEntity.ok(new AuthResponse(accessToken, existingUser.getUserId(), existingUser.getFirstName()));
                }
            catch (BadCredentialsException ex) {
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

                return ResponseEntity.ok(new AuthResponse(accessToken, newUser.getUserId(), newUser.getFirstName()));
            } catch (BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error while authenticating user");
            }
        }
    }

    public ResponseEntity<?> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = String.valueOf(getCookie(request, "refresh_token"));

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token.");
        }

        String userEmail = jwtUtil.extractUsername(refreshToken);
        User user = userService.findByEmail(userEmail);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setMaxAge(3600 * 24 * 14); // 2 weeks
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, user.getUserId(), user.getFirstName()));
    }

    public ResponseEntity<?> continueWithFacebook(String code) {
        return null;
    }


}
