package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.RoleType;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.AuthResponse;
import com.codercultrera.FilmFinder_Backend.dto.LoginRequest;
import com.codercultrera.FilmFinder_Backend.dto.RegisterRequest;
import com.codercultrera.FilmFinder_Backend.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
            Map<String, Boolean> response = new HashMap<>();
            response.put("This email is already associated with an account", true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        User newUser = new User();
        newUser.setFirstName(registerRequest.getFirstName());
        newUser.setLastName(registerRequest.getLastName());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setUsername(registerRequest.getEmail());
        newUser.setPassword(encryptedPassword);

        Role userRole = roleService.findByRoleType(RoleType.valueOf("USER"))
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        newUser.setRoles(new HashSet<>(Arrays.asList(userRole)));
        userService.save(newUser);
        String token = jwtUtil.generateToken(newUser.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Set-Cookie","token="+token+"; Secure; SameSite:None")
                .body(new AuthResponse(token, newUser.getUserId()));
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest, HttpServletResponse response) {
        User foundUser = userService.findByEmail(loginRequest.getEmail());

        if (foundUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
        } else {
            try {
                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                String token = jwtUtil.generateToken(loginRequest.getEmail());
                Cookie cookie = new Cookie("token" ,token);
                cookie.setHttpOnly(true);
                cookie.setMaxAge(3600);
                cookie.setSecure(true);
                cookie.setPath("/");
                response.addCookie(cookie);
                return ResponseEntity.ok(new AuthResponse(token, foundUser.getUserId()));
            } catch (BadCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        }
    }

    public ResponseEntity<?> authenticateWithGoogle(String code) {
        return null;
    }

    public ResponseEntity<?> authenticateWithFacebook(String code) {
        return null;
    }
}
