package com.codercultrera.FilmFinder_Backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Movie {
    @Id
    private String imdbId;
    private String title;
    private String year;
    private String type;
    private String poster;

    @ManyToMany(mappedBy = "favoriteMovies")
    private List<User> usersWhoFavorited = new ArrayList<>();

    @ManyToMany(mappedBy = "reviewedMovies")
    private List<User> usersWhoReviewed = new ArrayList<>();

    @ManyToMany(mappedBy = "watchedMovies")
    private List<User> usersWhoWatched = new ArrayList<>();

    @ManyToMany(mappedBy = "queuedMovies")
    private List<User> usersWhoQueued = new ArrayList<>();

    private String rated;
    private String runtime;
    private String plot;
    private String releaseDate;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    @JoinTable(name = "movie_actors",
            joinColumns = @JoinColumn(name = "imdb_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id"))
    private List<Actor> actors;

    @OneToMany(mappedBy = "movie")
    private List<Review> reviews = new ArrayList<>();





}
