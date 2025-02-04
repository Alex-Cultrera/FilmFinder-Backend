package com.codercultrera.FilmFinder_Backend.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    private Long reviewId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reviewSubject;
    private String content;
    private Integer rating;
    private Long userId;
    private String firstName;
    private String profilePhotoUrl;
    private String movieId;
    private String title;
    private String year;
    private String type;
    private String posterUrl;
}
