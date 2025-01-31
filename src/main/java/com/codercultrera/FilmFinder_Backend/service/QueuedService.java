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
public class QueuedService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public QueuedService(UserRepository userRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public List<Movie> getQueuedMovies(User user) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();
            log.info("Total queued movies: {}", theUser.getQueuedMovies().size());
            return theUser.getQueuedMovies();
        } catch (Exception e) {
            throw new RuntimeException("Error in getQueuedMovies: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String addQueuedMovie(User user, MovieAddRequest addedMovie) {
        try {
            user = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(addedMovie.getImdbId());
            Movie movie;

            if (optionalMovie.isPresent()) {
                movie = optionalMovie.get();
                if (user.getQueuedMovies() == null) {
                    user.setQueuedMovies(new ArrayList<>());
                }

                boolean isQueued = user.getQueuedMovies().stream()
                        .anyMatch(queuedMovie -> queuedMovie.getImdbId().equals(movie.getImdbId()));

                if (isQueued) {
                    return "Movie already in favorites.";
                }

                if (movie.getUsersWhoQueued() == null) {
                    movie.setUsersWhoQueued(new ArrayList<>());
                }

                movie.getUsersWhoQueued().add(user);
                user.getQueuedMovies().add(movie);
            } else {
                movie = new Movie();
                movie.setImdbId(addedMovie.getImdbId());
                movie.setTitle(addedMovie.getTitle());
                movie.setPosterUrl(addedMovie.getPosterUrl());
                movie.setYear(addedMovie.getYear());
                movie.setType(addedMovie.getType());
                movie.setUsersWhoQueued(new ArrayList<>());
                movie.getUsersWhoQueued().add(user);

                if (user.getQueuedMovies() == null) {
                    user.setQueuedMovies(new ArrayList<>());
                }
                user.getQueuedMovies().add(movie);
            }

            movieService.save(movie);
            userRepo.save(user);
            return "Movie added to queued list.";
        } catch (Exception e) {
            throw new RuntimeException("Error in getQueuedMovie: " + e.getMessage(), e);
        }
    }

    @Transactional
    public String removeQueuedMovie(User user, String imdbId) {
        try {
            User theUser = userRepo.findById(user.getUserId()).orElseThrow();

            Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);

            if (optionalMovie.isPresent()) {
                Movie movie = optionalMovie.get();
                log.debug("Found movie: {}", movie.getTitle());

                theUser.getQueuedMovies().removeIf(m -> m.getImdbId().equals(imdbId));

                movie.getUsersWhoQueued().removeIf(u -> u.getUserId().equals(theUser.getUserId()));

                userRepo.save(user);
                movieService.save(movie);

                return "Movie removed from queued list.";
            } else {
                return "Movie not found in queued list.";
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in removeQueuedMovie: " + e.getMessage(), e);
        }

    }
}
