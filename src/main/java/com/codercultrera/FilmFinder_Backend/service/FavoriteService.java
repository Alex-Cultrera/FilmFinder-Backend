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
public class FavoriteService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public FavoriteService(UserRepository userRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public List<Movie> getFavoriteMovies(User user) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();
            return theUser.getFavoriteMovies();
        } catch (Exception e) {
            throw new RuntimeException("Error in getFavoriteMovies: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String addFavoriteMovie(User user, MovieAddRequest addedMovie) {
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

            Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);

            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();

                theUser.getFavoriteMovies().removeIf(m -> m.getImdbId().equals(imdbId));

                movie.getUsersWhoFavorited().removeIf(u -> u.getUserId().equals(theUser.getUserId()));

                userRepo.save(user);
                movieService.save(movie);

                return "Movie removed from favorites.";
            } else {
                return "Movie not found in favorites.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in removeFavoriteMovie: " + e.getMessage(), e);
        }

    }
}
