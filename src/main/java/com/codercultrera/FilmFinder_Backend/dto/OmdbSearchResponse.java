package com.codercultrera.FilmFinder_Backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OmdbSearchResponse {
    @JsonProperty("Search")
    private List<OmdbMovie> movies;

    @JsonProperty("totalResults")
    private String totalResults;

    @JsonProperty("Response")
    private String response;
}