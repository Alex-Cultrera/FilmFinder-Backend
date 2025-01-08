package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.*;
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

import java.util.*;

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
            // need setSecure to be true in Production but must be false in development environment
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

    public ResponseEntity<List<Movie>> favoriteMovies(HttpServletRequest request) {
        User user = jwtUtil.getUserFromToken(request);
        List<Movie> favorites = userService.getFavoriteMovies(user);
        return ResponseEntity.ok(favorites);
    }

    public ResponseEntity<String> favoriteMoviesAdd(FavoriteRequest addedMovie, HttpServletRequest request) {
        User user = jwtUtil.getUserFromToken(request);
        try {
            userService.addMovieToFavorites(user, addedMovie);
            return ResponseEntity.ok("Movie added to favorites.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding movie to favorites.");
        }
    }

    public ResponseEntity<String> favoriteMoviesRemove(String imdbId, HttpServletRequest request) {
        User user = jwtUtil.getUserFromToken(request);
        userService.removeMovieFromFavorites(user, imdbId);
        return ResponseEntity.ok("Movie removed from favorites.");
    }



    public ResponseEntity<?> continueWithFacebook(String code) {
        return null;
    }



//    public ResponseEntity<?> uploadProfilePhoto(MultipartFile file) {
//        try {
//            // Generate a unique filename for the uploaded file
//            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
//            Path path = Paths.get(profilePhotosDirectory + "/" + fileName);
//
//            // Save the file to the server
//            Files.copy(file.getInputStream(), path);
//
//            // Construct the URL of the uploaded image
//            String photoUrl = "/images/profiles/" + fileName; // Adjust the path as needed
//
//            // Update the user profile (assume userService.updateProfilePhoto() is implemented)
//            String userId = "some-logged-in-user-id"; // Get this from the current session or authentication context
//            userService.updateProfilePhoto(userId, photoUrl);
//
//            return ResponseEntity.status(HttpStatus.OK).body(new ProfilePhotoResponse(photoUrl));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
//        }
//    }
//
//    public ResponseEntity<?> getPhoto(String userId) {
//        Optional<User> user = userService.findById(userId);
//        if (user.isPresent()) {
//            return ResponseEntity.ok(new ProfilePhotoResponse(user.get().getPhoto()));
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
//        }
//    }

}
