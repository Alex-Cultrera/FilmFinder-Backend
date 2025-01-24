package com.codercultrera.FilmFinder_Backend.web;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.*;
import com.codercultrera.FilmFinder_Backend.service.AuthService;
import com.codercultrera.FilmFinder_Backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/favorites")
    public ResponseEntity<List<MovieResponseDTO>> getFavoriteMovies(@AuthenticationPrincipal User user) {
        List<Movie> favorites = authService.favoriteMovies(user);
        List<MovieResponseDTO> favoriteDTOs = favorites.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(favoriteDTOs);
    }

    @PostMapping("/addFavorite")
    public ResponseEntity<String> addFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody FavoriteRequest addedMovie) {
        System.out.println("Received request to add favorite movie:");
        System.out.println("User: " + user.getEmail());
        System.out.println("Movie: " + addedMovie.getImdbId() + " - " + addedMovie.getTitle());
        String response = authService.favoriteMoviesAdd(user, addedMovie);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/removeFavorite")
    public ResponseEntity<String> removeFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody String imdbId) {
        String response = authService.favoriteMoviesRemove(user, imdbId);
        System.out.println(response);
        return ResponseEntity.ok(response);
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

    @GetMapping("/profilePhoto/{userId}")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long userId) {
        byte[] profilePhoto = userService.getProfilePhoto(userId);
        if (profilePhoto != null) {
            return ResponseEntity.ok().body(profilePhoto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/hello")
    public ResponseEntity<?> getHello(@RequestBody HelloRequest helloRequest) {
        try {
            boolean foundUser = userService.existsByEmail(helloRequest.getEmail());
            if (foundUser) {
                return ResponseEntity.status(HttpStatus.OK).body("Hello");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file.");
        }
    }
}
