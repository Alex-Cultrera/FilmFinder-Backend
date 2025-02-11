package com.codercultrera.FilmFinder_Backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.MovieAddRequest;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RecommendedService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public RecommendedService(UserRepository userRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public List<Movie> getRecommendedMovies() {
        try {
            return movieService.getRecommendedMovies();
        } catch (Exception e) {
            throw new RuntimeException("Error in getRecommendedMovies: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String addRecommendedMovie(User user, MovieAddRequest addedMovie) {
        try {
            user = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(addedMovie.getImdbId());
            Movie movie;

            if (optionalMovie.isPresent()) {
                movie = optionalMovie.get();
                if (user.getRecommendedMovies() == null) {
                    user.setRecommendedMovies(new ArrayList<>());
                }

                boolean isRecommended = user.getRecommendedMovies().stream()
                        .anyMatch(recommendedMovie -> recommendedMovie.getImdbId().equals(movie.getImdbId()));

                if (isRecommended) {
                    return "Movie already in recommended.";
                }

                if (movie.getUsersWhoRecommended() == null) {
                    movie.setUsersWhoRecommended(new ArrayList<>());
                }

                movie.getUsersWhoRecommended().add(user);
                user.getRecommendedMovies().add(movie);
            } else {
                movie = new Movie();
                movie.setImdbId(addedMovie.getImdbId());
                movie.setTitle(addedMovie.getTitle());
                movie.setPosterUrl(addedMovie.getPosterUrl());
                movie.setYear(addedMovie.getYear());
                movie.setType(addedMovie.getType());
                movie.setUsersWhoRecommended(new ArrayList<>());
                movie.getUsersWhoRecommended().add(user);

                if (user.getRecommendedMovies() == null) {
                    user.setRecommendedMovies(new ArrayList<>());
                }
                user.getRecommendedMovies().add(movie);
            }

            movieService.save(movie);
            userRepo.save(user);
            return "Movie added to recommended.";
        } catch (Exception e) {
            throw new RuntimeException("Error in addRecommendedMovie: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String removeRecommendedMovie(User user, String imdbId) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);

            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();

                theUser.getRecommendedMovies().removeIf(m -> m.getImdbId().equals(imdbId));

                movie.getUsersWhoRecommended().removeIf(u -> u.getUserId().equals(theUser.getUserId()));

                userRepo.save(user);
                movieService.save(movie);

                return "Movie removed from recommended.";
            } else {
                return "Movie not found in recommended.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in removeRecommendedMovie: " + e.getMessage(), e);
        }

    }
}
