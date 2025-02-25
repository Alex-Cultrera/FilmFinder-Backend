package com.codercultrera.FilmFinder_Backend.web;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.codercultrera.FilmFinder_Backend.dto.ApiResponse;
import com.codercultrera.FilmFinder_Backend.dto.GoogleApiRequest;
import com.codercultrera.FilmFinder_Backend.dto.LoginRequest;
import com.codercultrera.FilmFinder_Backend.dto.RegisterRequest;
import com.codercultrera.FilmFinder_Backend.service.AuthService;
import com.codercultrera.FilmFinder_Backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

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

    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "username", authentication.getName(),
                    "authorities", authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList())));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
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
    public ResponseEntity<String> userLogout(HttpServletRequest request, HttpServletResponse response) {
        // Clear security context
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        // Clear cookies
        String accessTokenCookieHeader = "accessToken=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Domain=codercultrera-filmfinder.com";
        String refreshTokenCookieHeader = "refreshToken=; Path=/; HttpOnly; Secure; SameSite=Lax; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Domain=codercultrera-filmfinder.com";

        response.addHeader("Set-Cookie", accessTokenCookieHeader);
        response.addHeader("Set-Cookie", refreshTokenCookieHeader);

        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshTokens(request, response);
    }

    @PostMapping("/uploadProfilePhoto")
    public ResponseEntity<?> postProfilePhoto(@RequestParam("userId") String userId,
            @RequestParam("file") MultipartFile file) {
        try {
            Long myId = Long.parseLong(userId);
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No file uploaded.");
            }
            if (myId > 5) {
                return ResponseEntity.status(HttpStatus.OK).body("Profile photo uploaded successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file.");
        }
    }

}
