package com.codercultrera.FilmFinder_Backend.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.codercultrera.FilmFinder_Backend.dto.OmdbMovie;
import com.codercultrera.FilmFinder_Backend.dto.OmdbMovieSearchResponseDto;
import com.codercultrera.FilmFinder_Backend.dto.OmdbSearchResponse;

@Service
public class OmdbApiService {

    @Value("${omdb.api.key}")
    private String apiKey;

    private RestTemplate restTemplate;

    public OmdbApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public OmdbMovieSearchResponseDto searchMoviesByTitle(String title, int page) {
        String url = String.format("http://www.omdbapi.com/?apikey=%s&s=%s&page=%d", apiKey, title, page);

        try {
            ResponseEntity<OmdbSearchResponse> response = restTemplate.getForEntity(url, OmdbSearchResponse.class);

            if (response.getBody() != null && response.getBody().getMovies() != null) {
                return new OmdbMovieSearchResponseDto(
                        response.getBody().getMovies(),
                        response.getBody().getTotalResults());
            }

            return new OmdbMovieSearchResponseDto(Collections.emptyList(), "0");
        } catch (Exception e) {
            throw new RuntimeException("Failed to search movies", e);
        }
    }

    public OmdbMovie searchMoviesById(String id) {
        String url = String.format("http://www.omdbapi.com/?apikey=%s&i=%s", apiKey, id);

        try {
            ResponseEntity<OmdbMovie> response = restTemplate.getForEntity(url, OmdbMovie.class);

            if (response.getBody().getTitle() != null) {
                return response.getBody();
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search movies", e);
        }

    }

}
