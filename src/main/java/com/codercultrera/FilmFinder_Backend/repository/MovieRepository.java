package com.codercultrera.FilmFinder_Backend.repository;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository <Movie, String> {

    boolean existsByImdbId(String imdbId);
}
