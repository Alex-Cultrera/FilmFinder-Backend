package com.codercultrera.FilmFinder_Backend.web;

import java.util.Collections;
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
import com.codercultrera.FilmFinder_Backend.dto.MovieRemoveRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieAddRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieResponseDTO;
import com.codercultrera.FilmFinder_Backend.service.RecommendedService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class RecommendedController {

    private final RecommendedService recommendedService;

    public RecommendedController(RecommendedService recommendedService) {
        this.recommendedService = recommendedService;
    }

    // READ
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedMovies(@AuthenticationPrincipal User user) {
        if (user == null) {
            // Return empty list with 200 status for unauthenticated users
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Movie> recommended = recommendedService.getRecommendedMovies(user);
        List<MovieResponseDTO> recommendedDTOs = recommended.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(recommendedDTOs);
    }

    // CREATE / UPDATE
    @PostMapping("/addRecommended")
    public ResponseEntity<String> addRecommendedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieAddRequest addedMovie) {
        String response = recommendedService.addRecommendedMovie(user, addedMovie);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeRecommended")
    public ResponseEntity<String> removeRecommendedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieRemoveRequest movieToRemove) {
        String response = recommendedService.removeRecommendedMovie(user, movieToRemove.getImdbId());
        return ResponseEntity.ok(response);
    }

}
