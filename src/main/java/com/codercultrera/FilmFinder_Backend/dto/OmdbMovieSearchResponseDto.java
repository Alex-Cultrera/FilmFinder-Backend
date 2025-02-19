package com.codercultrera.FilmFinder_Backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OmdbMovieSearchResponseDto {

    private List<OmdbMovie> movies;
    private String totalResults;

}