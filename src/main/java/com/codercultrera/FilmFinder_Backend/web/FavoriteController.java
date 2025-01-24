package com.codercultrera.FilmFinder_Backend.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.FavoriteRemovalRequest;
import com.codercultrera.FilmFinder_Backend.dto.FavoriteRequest;
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
        String response = favoriteService.addFavoriteMovie(user, addedMovie);
        log.info("Add favorite response: {}", response);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeFavorite")
    public ResponseEntity<String> removeFavoriteMovie(@AuthenticationPrincipal User user,
            @RequestBody FavoriteRemovalRequest movieToRemove) {
        String response = favoriteService.removeFavoriteMovie(user, movieToRemove.getImdbId());
        log.info("Remove favorite response: {}", response);
        return ResponseEntity.ok(response);
    }

}
