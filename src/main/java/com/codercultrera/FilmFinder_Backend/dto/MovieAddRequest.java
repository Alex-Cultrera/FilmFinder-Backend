package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieAddRequest {

    String imdbId;
    String title;
    String posterUrl;
    String year;
    String type;
}
