package com.codercultrera.FilmFinder_Backend.web;

import com.codercultrera.FilmFinder_Backend.dto.ApiResponse;
import com.codercultrera.FilmFinder_Backend.dto.LoginRequest;
import com.codercultrera.FilmFinder_Backend.dto.RegisterRequest;
import com.codercultrera.FilmFinder_Backend.service.AuthService;
import com.codercultrera.FilmFinder_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestParam("code") String code) {
        return authService.authenticateWithGoogle(code);
    }

    @GetMapping("/facebook")
    public ResponseEntity<?> authenticateWithFacebook(@RequestParam("code") String code) {
        return authService.authenticateWithFacebook(code);
    }

}
