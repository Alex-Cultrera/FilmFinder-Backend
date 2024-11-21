package com.codercultrera.FilmFinder_Backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reviewer;
    @Column(length = 50)
    private String reviewSubject;
    @Column(length = 625)
    private String reviewBody;
    @ManyToOne
    @JoinColumn(name="movie_id")
    private Movie movie;


}
