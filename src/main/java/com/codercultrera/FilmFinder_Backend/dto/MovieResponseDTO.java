package com.codercultrera.FilmFinder_Backend.dto;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MovieResponseDTO {
    private String imdbId;
    private String title;
    private String posterUrl;
    private String year;

    public MovieResponseDTO(Movie movie) {
        this.imdbId = movie.getImdbId();
        this.title = movie.getTitle();
        this.posterUrl = movie.getPosterUrl();
        this.year = movie.getYear();
    }
}