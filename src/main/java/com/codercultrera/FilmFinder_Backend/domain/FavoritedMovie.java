package com.codercultrera.FilmFinder_Backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class FavoritedMovie extends Movie {

    @ManyToMany(mappedBy = "moviesFavorited")
    private List<User> users = new ArrayList<>();
}
