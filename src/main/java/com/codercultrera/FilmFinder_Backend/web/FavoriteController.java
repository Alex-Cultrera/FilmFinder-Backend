package com.codercultrera.FilmFinder_Backend.web;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.*;
import com.codercultrera.FilmFinder_Backend.service.FavoriteService;
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
@RequestMapping("")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // READ
    @GetMapping("/favorites")
    public ResponseEntity<List<MovieResponseDTO>> getFavoriteMovies(@AuthenticationPrincipal User user) {
        List<Movie> favorites = favoriteService.getFavoriteMovies(user);
        List<MovieResponseDTO> favoriteDTOs = favorites.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(favoriteDTOs);
    }

    // CREATE / UPDATE
    @PostMapping("/addFavorite")
    public ResponseEntity<String> addFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody FavoriteRequest addedMovie) {
        System.out.println("Received request to add favorite movie:");
        System.out.println("User: " + user.getEmail());
        System.out.println("Movie: " + addedMovie.getImdbId() + " - " + addedMovie.getTitle());
        String response = favoriteService.addFavoriteMovie(user, addedMovie);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeFavorite")
    public ResponseEntity<String> removeFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody FavoriteRequest movieToRemove) {
        String response = favoriteService.removeFavoriteMovie(user, movieToRemove.getImdbId());
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

}
