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
import com.codercultrera.FilmFinder_Backend.service.QueuedService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class QueuedController {

    private final QueuedService queuedService;

    public QueuedController(QueuedService queuedService) {
        this.queuedService = queuedService;
    }

    // READ
    @GetMapping("/queued")
    public ResponseEntity<?> getQueuedMovies(@AuthenticationPrincipal User user) {
        if (user == null) {
            // Return empty list with 200 status for unauthenticated users
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<Movie> queued = queuedService.getQueuedMovies(user);
        List<MovieResponseDTO> queuedDTOs = queued.stream()
                .map(MovieResponseDTO::new)
                .toList();
        return ResponseEntity.ok(queuedDTOs);
    }

    // CREATE / UPDATE
    @PostMapping("/addQueued")
    public ResponseEntity<String> addQueuedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieAddRequest addedMovie) {
        String response = queuedService.addQueuedMovie(user, addedMovie);
        return ResponseEntity.ok(response);
    }

    // DELETE
    @PostMapping("/removeQueued")
    public ResponseEntity<String> removeQueuedMovie(@AuthenticationPrincipal User user,
            @RequestBody MovieRemoveRequest movieToRemove) {
        String response = queuedService.removeQueuedMovie(user, movieToRemove.getImdbId());
        return ResponseEntity.ok(response);
    }

}
