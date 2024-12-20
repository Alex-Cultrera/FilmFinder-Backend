package com.codercultrera.FilmFinder_Backend.web;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.*;
import com.codercultrera.FilmFinder_Backend.security.JwtUtil;
import com.codercultrera.FilmFinder_Backend.service.AuthService;
import com.codercultrera.FilmFinder_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();
        boolean isEmailTaken = userService.existsByEmail(email);
        if (isEmailTaken) {
            return new ResponseEntity<>(new ApiResponse("Email is already in use"), HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(new ApiResponse("Email is available"), HttpStatus.OK);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        return authService.registerUser(registerRequest);
    }

    @PostMapping("/google")
    public ResponseEntity<?> register(@RequestBody GoogleApiRequest googleApiRequest, HttpServletResponse response) {
        return authService.continueWithGoogle(googleApiRequest, response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.authenticateUser(loginRequest, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> userLogout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshTokens(request, response);
    }

    @PostMapping("/postProfilePhoto")
    public ResponseEntity<?> postProfilePhoto(@RequestParam("file") MultipartFile file) {
        return authService.uploadProfilePhoto(file);
    }

    @GetMapping("/{userId}/profilePhoto")
    public ResponseEntity<?> getProfilePhoto(@PathVariable String userId) {
        return authService.getPhoto(userId);
    }
}
