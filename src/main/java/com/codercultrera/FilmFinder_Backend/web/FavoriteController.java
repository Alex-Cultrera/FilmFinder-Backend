package com.codercultrera.FilmFinder_Backend.web;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.MovieRemoveRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieAddRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieResponseDTO;
import com.codercultrera.FilmFinder_Backend.service.FavoriteService;

import lombok.extern.slf4j.Slf4j;

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
    public ResponseEntity<?> getFavoriteMovies(@AuthenticationPrincipal User user) {
        if (user == null) {
            // Return empty list with 200 status for unauthenticated users
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Movie> favorites = favoriteService.getFavoriteMovies(user);
        List<MovieResponseDTO> favoriteDTOs = favorites.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(favoriteDTOs);
    }

    // CREATE / UPDATE
    @CrossOrigin(origins = "https://codercultrera-filmfinder.netlify.app", allowCredentials = "true")
    @PostMapping("/addFavorite")
    public ResponseEntity<String> addFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieAddRequest addedMovie) {
        String response = favoriteService.addFavoriteMovie(user, addedMovie);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeFavorite")
    public ResponseEntity<String> removeFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieRemoveRequest movieToRemove) {
        String response = favoriteService.removeFavoriteMovie(user, movieToRemove.getImdbId());
        return ResponseEntity.ok(response);
    }

}
