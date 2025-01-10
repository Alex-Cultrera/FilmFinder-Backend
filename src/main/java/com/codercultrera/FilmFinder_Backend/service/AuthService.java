package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.*;
import com.codercultrera.FilmFinder_Backend.dto.*;
import com.codercultrera.FilmFinder_Backend.security.CookieUtils;
import com.codercultrera.FilmFinder_Backend.security.JwtUtil;
import io.jsonwebtoken.Claims;
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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final CustomUserDetailsService customUserDetailsService;

    public AuthService(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserService userService, RoleService roleService, CustomUserDetailsService customUserDetailsService) {
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

            CookieUtils.setCookie(response, "accessToken", accessToken);
            CookieUtils.setCookie(response, "refreshToken", refreshToken);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", user.getUserId());
            responseBody.put("firstName", user.getFirstName());

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

    public ResponseEntity<?> favoriteMovies(HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be logged in to access your favorite movies.");
        }

        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

        User user = jwtUtil.getUserFromToken(request, userDetails);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Please log in again.");
        }
        List<Movie> favorites = userService.getFavoriteMovies(user);
        return ResponseEntity.ok(favorites);
    }

    public ResponseEntity<String> favoriteMoviesAdd(FavoriteRequest addedMovie, HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
        User user = jwtUtil.getUserFromToken(request, userDetails);
        try {
            userService.addMovieToFavorites(user, addedMovie);
            return ResponseEntity.ok("Movie added to favorites.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding movie to favorites.");
        }
    }

    public ResponseEntity<String> favoriteMoviesRemove(String imdbId, HttpServletRequest request) {
        String token = jwtUtil.extractToken(request);
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
        User user = jwtUtil.getUserFromToken(request, userDetails);
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
