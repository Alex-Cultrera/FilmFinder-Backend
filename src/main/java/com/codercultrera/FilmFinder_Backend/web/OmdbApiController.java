package com.codercultrera.FilmFinder_Backend.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.dto.OmdbMovie;
import com.codercultrera.FilmFinder_Backend.dto.OmdbMovieSearchResponseDto;
import com.codercultrera.FilmFinder_Backend.service.OmdbApiService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/omdb/api")
public class OmdbApiController {

    private OmdbApiService omdbApiService;

    public OmdbApiController(OmdbApiService omdbApiService) {
        this.omdbApiService = omdbApiService;
    }

    @GetMapping("/search-by-title")
    public ResponseEntity<OmdbMovieSearchResponseDto> searchMoviesByTitle(@RequestParam String title,
            @RequestParam(defaultValue = "1") int page) {
        try {
            OmdbMovieSearchResponseDto response = omdbApiService.searchMoviesByTitle(title, page);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search-by-id")
    public ResponseEntity<OmdbMovie> searchMoviesById(@RequestParam String id) {
        try {
            OmdbMovie response = omdbApiService.searchMoviesById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
