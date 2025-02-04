package com.codercultrera.FilmFinder_Backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    private Long reviewId;
    private Long userId;
    private String reviewSubject;
    private String content;
    private Integer rating;
    private String movieId;
    private String title;
    private String posterUrl;
    private String year;
    private String type;
    private String firstName;
    private String profilePhotoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String rated;
    private String runtime;
    private String plot;
    private String releaseDate;
}
