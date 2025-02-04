package com.codercultrera.FilmFinder_Backend.web;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.domain.Movie;
import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.MovieRemoveRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieAddRequest;
import com.codercultrera.FilmFinder_Backend.dto.MovieResponseDTO;
import com.codercultrera.FilmFinder_Backend.dto.ReviewDto;
import com.codercultrera.FilmFinder_Backend.service.FavoriteService;
import com.codercultrera.FilmFinder_Backend.service.ReviewService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/movies/{movieId}/reviews")
    public ResponseEntity<List<ReviewDto>> getMovieReviews(@PathVariable String movieId) {
        return ResponseEntity.ok(reviewService.getMovieReviews(movieId));
    }

    @PostMapping("/movies/{movieId}/review")
    public ResponseEntity<ReviewDto> createReview(@AuthenticationPrincipal User user, @PathVariable String movieId,
            @RequestBody ReviewDto reviewDto) {
        reviewDto.setMovieId(movieId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(user, reviewDto));
    }

    // @PutMapping("/reviews/{reviewId}")
    // public ResponseEntity<ReviewDto> updateReview(
    // @PathVariable Long reviewId,
    // @RequestBody ReviewDto reviewDto,
    // @AuthenticationPrincipal UserDetails userDetails) {
    // Long userId = ((CustomUserDetails) userDetails).getId();
    // return ResponseEntity.ok(reviewService.updateReview(reviewId, reviewDto,
    // userId));
    // }

    // @DeleteMapping("/reviews/{reviewId}")
    // public ResponseEntity<Void> deleteReview(
    // @PathVariable Long reviewId,
    // @AuthenticationPrincipal UserDetails userDetails) {
    // Long userId = ((CustomUserDetails) userDetails).getId();
    // reviewService.deleteReview(reviewId, userId);
    // return ResponseEntity.noContent().build();
    // }

}
