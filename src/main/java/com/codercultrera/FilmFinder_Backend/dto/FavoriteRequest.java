package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {

    String imdbId;
    String title;
    String poster;
    String year;
    String type;
}
