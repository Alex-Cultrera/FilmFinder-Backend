package com.codercultrera.FilmFinder_Backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.FavoriteRequest;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FavoriteService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public FavoriteService(UserRepository userRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public List<Movie> getFavoriteMovies(User user) {
        if (user == null) {
            return null;
        }
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();
            log.info("User ID: {}", theUser.getUserId());
            log.info("Total favorite movies: {}", theUser.getFavoriteMovies().size());
            log.info("Retrieved favorite movies: {}",
                    theUser.getFavoriteMovies().stream().map(m -> String.format("%s (%s)", m.getTitle(), m.getImdbId()))
                            .toList());
            return theUser.getFavoriteMovies();
        } catch (Exception e) {
            throw new RuntimeException("Error in getFavoriteMovies: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String addFavoriteMovie(User user, FavoriteRequest addedMovie) {
        try {
            user = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(addedMovie.getImdbId());
            Movie movie;

            if (optionalMovie.isPresent()) {
                movie = optionalMovie.get();
                if (user.getFavoriteMovies() == null) {
                    user.setFavoriteMovies(new ArrayList<>());
                }

                boolean isFavorite = user.getFavoriteMovies().stream()
                        .anyMatch(favoriteMovie -> favoriteMovie.getImdbId().equals(movie.getImdbId()));

                if (isFavorite) {
                    return "Movie already in favorites.";
                }

                if (movie.getUsersWhoFavorited() == null) {
                    movie.setUsersWhoFavorited(new ArrayList<>());
                }

                movie.getUsersWhoFavorited().add(user);
                user.getFavoriteMovies().add(movie);
            } else {
                movie = new Movie();
                movie.setImdbId(addedMovie.getImdbId());
                movie.setTitle(addedMovie.getTitle());
                movie.setPosterUrl(addedMovie.getPosterUrl());
                movie.setYear(addedMovie.getYear());
                movie.setType(addedMovie.getType());
                movie.setUsersWhoFavorited(new ArrayList<>());
                movie.getUsersWhoFavorited().add(user);

                if (user.getFavoriteMovies() == null) {
                    user.setFavoriteMovies(new ArrayList<>());
                }
                user.getFavoriteMovies().add(movie);
            }

            movieService.save(movie);
            userRepo.save(user);
            return "Movie added to favorites.";
        } catch (Exception e) {
            throw new RuntimeException("Error in addFavoriteMovie: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String removeFavoriteMovie(User user, String imdbId) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();
            log.debug("Found user: {}", theUser.getUsername());

            Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);
            log.debug("Searching for movie with IMDB ID: {}", imdbId);

            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();
                log.debug("Found movie: {}", movie.getTitle());

                theUser.getFavoriteMovies().removeIf(m -> m.getImdbId().equals(imdbId));

                movie.getUsersWhoFavorited().removeIf(u -> u.getUserId().equals(theUser.getUserId()));

                userRepo.save(user);
                movieService.save(movie);

                log.info("Removed movie {} from user {}'s favorites", imdbId, user.getUserId());
                return "Movie removed from favorites.";
            } else {
                log.warn("Movie with imdbId {} not found", imdbId);
                return "Movie not found in favorites.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in removeFavoriteMovie: " + e.getMessage(), e);
        }

    }
}
