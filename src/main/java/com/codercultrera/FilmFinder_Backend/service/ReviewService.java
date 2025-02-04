package com.codercultrera.FilmFinder_Backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.Review;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.ReviewDto;
import com.codercultrera.FilmFinder_Backend.repository.ReviewRepository;
import com.codercultrera.FilmFinder_Backend.service.UserService;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final MovieService movieService;

    public ReviewService(ReviewRepository reviewRepository,
            UserService userService,
            MovieService movieService) {
        this.reviewRepository = reviewRepository;
        this.userService = userService;
        this.movieService = movieService;
    }

    public List<ReviewDto> getMovieReviews(String movieId) {
        return reviewRepository.findByMovie_ImdbId(movieId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDto createReview(User user, ReviewDto reviewDTO) {
        // Check if user already reviewed this movie
        if (reviewRepository.existsByReviewer_UserIdAndMovie_ImdbId(user.getUserId(), reviewDTO.getMovieId())) {
            throw new IllegalStateException("User has already reviewed this movie");
        }

        try {
            User reviewer = userService.findById(user.getUserId()).orElseThrow();
            Optional<Movie> movieReviewed = movieService.findByImdbId(reviewDTO.getMovieId());
            Movie movie;

            if (movieReviewed.isPresent()) {
                movie = movieReviewed.get();
            } else {
                movie = new Movie();
                movie.setImdbId(reviewDTO.getMovieId());
                movie.setTitle(reviewDTO.getTitle());
                movie.setPosterUrl(reviewDTO.getPosterUrl());
                movie.setYear(reviewDTO.getYear());
                movie.setType(reviewDTO.getType());
            }

            Review review = new Review();
            review.setReviewSubject(reviewDTO.getReviewSubject());
            review.setContent(reviewDTO.getContent());
            review.setRating(reviewDTO.getRating());
            review.setReviewer(reviewer);
            review.setMovie(movie);
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
            userService.save(reviewer);
            movieService.save(movie);
            return convertToDTO(reviewRepository.save(review));
        } catch (Exception e) {
            throw new RuntimeException("Error in createReview: " + e.getMessage(), e);
        }

    }

    // public ReviewDto updateReview(Long reviewId, ReviewDto reviewDTO, Long
    // userId) {
    // Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
    // .orElseThrow(() -> new ResourceNotFoundException("Review not found or
    // unauthorized"));

    // review.setContent(reviewDTO.getContent());
    // review.setRating(reviewDTO.getRating());
    // review.setUpdatedAt(LocalDateTime.now());

    // return convertToDTO(reviewRepository.save(review));
    // }

    // public void deleteReview(Long reviewId, Long userId) {
    // Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
    // .orElseThrow(() -> new ResourceNotFoundException("Review not found or
    // unauthorized"));
    // reviewRepository.delete(review);
    // }

    private ReviewDto convertToDTO(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        dto.setReviewSubject(review.getReviewSubject());
        dto.setContent(review.getContent());
        dto.setRating(review.getRating());
        dto.setUserId(review.getReviewer().getUserId());
        dto.setFirstName(review.getReviewer().getFirstName());
        dto.setProfilePhotoUrl(review.getReviewer().getPhoto());
        dto.setMovieId(review.getMovie().getImdbId());
        dto.setTitle(review.getMovie().getTitle());
        dto.setYear(review.getMovie().getYear());
        dto.setType(review.getMovie().getType());
        dto.setPosterUrl(review.getMovie().getPosterUrl());
        return dto;
    }
}