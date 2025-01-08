package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.FavoriteRequest;
import com.codercultrera.FilmFinder_Backend.repository.MovieRepository;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;
import io.jsonwebtoken.io.IOException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepo;
    private final MovieService movieService;

    public UserService(UserRepository userRepo, MovieRepository movieRepo, MovieService movieService) {
        this.userRepo = userRepo;
        this.movieService = movieService;
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsUserByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<User> findById(String userId) {
        return userRepo.findById(Long.valueOf(userId));
    }

    public boolean updateProfilePhoto(String userId, MultipartFile file) {
        try {
            Optional<User> userOptional = findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                byte[] profilePhoto = file.getBytes();
                user.setProfilePhoto(profilePhoto);
                userRepo.save(user);
                return true;
            }
        } catch (IOException | java.io.IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] getProfilePhoto(Long userId) {
        Optional<User> userOptional = userRepo.findById(userId);
        return userOptional.map(User::getProfilePhoto).orElse(null);
    }

    public List<Movie> getFavoriteMovies(User user) {
        return user.getFavoriteMovies();
    }

    @Transactional
    public void addMovieToFavorites(User user, FavoriteRequest addedMovie) {
        Optional<Movie> optionalMovie = movieService.findByImdbId(addedMovie.getImdbId());
        Movie movie;

        if (optionalMovie.isPresent()) {
            movie = optionalMovie.get();
            boolean isFavorite = user.getFavoriteMovies().stream()
                    .anyMatch(favoriteMovie -> favoriteMovie.getImdbId().equals(movie.getImdbId()));

            if (isFavorite) {
                return;
            }
            movie.getUsersWhoFavorited().add(user);
            user.getFavoriteMovies().add(movie);
            userRepo.save(user);
            movieService.save(movie);
        } else {
            movie = new Movie();
            movie.setImdbId(addedMovie.getImdbId());
            movie.setTitle(addedMovie.getTitle());
            movie.setPoster(addedMovie.getPoster());
            movie.setYear(addedMovie.getYear());
            movie.setType(addedMovie.getType());
            movie.getUsersWhoFavorited().add(user);
            user.getFavoriteMovies().add(movie);
            movieService.save(movie);
            userRepo.save(user);
        }
    }

    @Transactional
    public void removeMovieFromFavorites(User user, String imdbId) {
        Optional<Movie> optionalMovie = movieService.findByImdbId(imdbId);
        Movie movie;
        if (optionalMovie.isPresent()) {
            movie = optionalMovie.get();
            if (!movie.getUsersWhoFavorited().contains(user)) {
                throw new RuntimeException("Movie not in this users favorites list.");
            }
            user.getFavoriteMovies().remove(movie);
            movie.getUsersWhoFavorited().remove(user);
            userRepo.save(user);
            movieService.save(movie);
        }
    }
}
