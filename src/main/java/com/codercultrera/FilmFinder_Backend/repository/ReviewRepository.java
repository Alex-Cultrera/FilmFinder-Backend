package com.codercultrera.FilmFinder_Backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codercultrera.FilmFinder_Backend.domain.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovie_ImdbId(String imdbId);

    boolean existsByReviewer_UserIdAndMovie_ImdbId(Long userId, String movieId);

    Optional<Review> findByReviewIdAndReviewer_UserId(Long reviewId, Long userId);

    Optional<Review> findByReviewId(Long reviewId);

    List<Review> findByReviewer_UserId(Long userId);

    List<Review> findAll();

}
