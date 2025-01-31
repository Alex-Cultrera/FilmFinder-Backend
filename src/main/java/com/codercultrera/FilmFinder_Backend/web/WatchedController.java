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
import com.codercultrera.FilmFinder_Backend.dto.MovieAddRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieRemoveRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieResponseDTO;
import com.codercultrera.FilmFinder_Backend.service.WatchedService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class WatchedController {

    private final WatchedService watchedService;

    public WatchedController(WatchedService watchedService) {
        this.watchedService = watchedService;
    }

    // READ
    @GetMapping("/watched")
    public ResponseEntity<?> getWatchedMovies(@AuthenticationPrincipal User user) {
        if (user == null) {
            // Return empty list with 200 status for unauthenticated users
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Movie> watched = watchedService.getWatchedMovies(user);
        List<MovieResponseDTO> watchedDTOs = watched.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(watchedDTOs);
    }

    // CREATE / UPDATE
    @PostMapping("/addWatched")
    public ResponseEntity<String> addWatchedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieAddRequest addedMovie) {
        String response = watchedService.addWatchedMovie(user, addedMovie);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeWatched")
    public ResponseEntity<String> removeWatchedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieRemoveRequest movieToRemove) {
        String response = watchedService.removeWatchedMovie(user, movieToRemove.getImdbId());
        return ResponseEntity.ok(response);
    }

}
