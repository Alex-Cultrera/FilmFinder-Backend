package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepo;

    public MovieService(MovieRepository movieRepo) {
        this.movieRepo = movieRepo;
    }

    public boolean existsByImdbId(String imdbId) {
        return movieRepo.existsByImdbId(imdbId);
    }

    public Optional<Movie> findByImdbId(String imdbId) {
        return movieRepo.findById(imdbId);
    }

    public void save(Movie movie) {
        movieRepo.save(movie);
    }


}
