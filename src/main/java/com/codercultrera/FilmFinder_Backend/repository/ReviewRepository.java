package com.codercultrera.FilmFinder_Backend.repository;

import com.codercultrera.FilmFinder_Backend.domain.Review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovie_ImdbId(String imdbId);

    boolean existsByReviewer_UserIdAndMovie_ImdbId(Long userId, String movieId);

    Optional<Review> findByReviewIdAndReviewer_UserId(Long reviewId, Long userId);

}
