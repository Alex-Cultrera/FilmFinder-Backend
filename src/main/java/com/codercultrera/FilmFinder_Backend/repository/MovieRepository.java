package com.codercultrera.FilmFinder_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codercultrera.FilmFinder_Backend.domain.Movie;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("SELECT m FROM Movie m JOIN m.usersWhoRecommended uwr")
    List<Movie> findAllRecommendedMovies();

    boolean existsByImdbId(String imdbId);

    Optional<Movie> findByImdbId(String imdbId);
}
