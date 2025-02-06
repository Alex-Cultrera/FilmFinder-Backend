package com.codercultrera.FilmFinder_Backend.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.dto.ReviewDto;
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

    @GetMapping("/review/all")
    public ResponseEntity<List<ReviewDto>> getAllReviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.getReviews());
    }

    @GetMapping("/review/user/all")
    public ResponseEntity<List<ReviewDto>> getAllUserReviews(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.getUserReviews(user));
    }

    @GetMapping("/review/movies/{movieId}/all")
    public ResponseEntity<List<ReviewDto>> getMovieReviews(@PathVariable String movieId) {
        return ResponseEntity.ok(reviewService.getMovieReviews(movieId));
    }

    @PostMapping("/review/movies/{movieId}/new")
    public ResponseEntity<ReviewDto> createReview(@AuthenticationPrincipal User user, @PathVariable String movieId,
            @RequestBody ReviewDto reviewDto) {
        reviewDto.setMovieId(movieId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(user, reviewDto));
    }

    @PutMapping("/review/{reviewId}/update")
    public ResponseEntity<ReviewDto> updateReview(@AuthenticationPrincipal User user,
            @PathVariable Long reviewId, @RequestBody ReviewDto reviewDto) {
        return ResponseEntity.ok(reviewService.updateReview(user, reviewId, reviewDto));
    }

    @DeleteMapping("/review/{reviewId}/delete")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal User user, @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, user);
        return ResponseEntity.noContent().build();
    }

}
