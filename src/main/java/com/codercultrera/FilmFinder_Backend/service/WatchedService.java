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
public class WatchedService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public WatchedService(UserRepository userRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public List<Movie> getWatchedMovies(User user) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();
            return theUser.getWatchedMovies();
        } catch (Exception e) {
            throw new RuntimeException("Error in getWatchedMovies: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String addWatchedMovie(User user, MovieAddRequest addedMovie) {
        try {
            user = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(addedMovie.getImdbId());
            Movie movie;

            if (optionalMovie.isPresent()) {
                movie = optionalMovie.get();
                if (user.getWatchedMovies() == null) {
                    user.setWatchedMovies(new ArrayList<>());
                }

                boolean isWatched = user.getWatchedMovies().stream()
                        .anyMatch(watchedMovie -> watchedMovie.getImdbId().equals(movie.getImdbId()));

                if (isWatched) {
                    return "Movie already in favorites.";
                }

                if (movie.getUsersWhoWatched() == null) {
                    movie.setUsersWhoWatched(new ArrayList<>());
                }

                movie.getUsersWhoWatched().add(user);
                user.getWatchedMovies().add(movie);
            } else {
                movie = new Movie();
                movie.setImdbId(addedMovie.getImdbId());
                movie.setTitle(addedMovie.getTitle());
                movie.setPosterUrl(addedMovie.getPosterUrl());
                movie.setYear(addedMovie.getYear());
                movie.setType(addedMovie.getType());
                movie.setUsersWhoWatched(new ArrayList<>());
                movie.getUsersWhoWatched().add(user);

                if (user.getWatchedMovies() == null) {
                    user.setWatchedMovies(new ArrayList<>());
                }
                user.getWatchedMovies().add(movie);
            }

            movieService.save(movie);
            userRepo.save(user);
            return "Movie added to watched list.";
        } catch (Exception e) {
            throw new RuntimeException("Error in getWatchedMovie: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String removeWatchedMovie(User user, String imdbId) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);

            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();
                log.debug("Found movie: {}", movie.getTitle());

                theUser.getWatchedMovies().removeIf(m -> m.getImdbId().equals(imdbId));

                movie.getUsersWhoWatched().removeIf(u -> u.getUserId().equals(theUser.getUserId()));

                userRepo.save(user);
                movieService.save(movie);

                return "Movie removed from watched list.";
            } else {
                return "Movie not found in watched list.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in removeWatchedMovie: " + e.getMessage(), e);
        }

    }
}
