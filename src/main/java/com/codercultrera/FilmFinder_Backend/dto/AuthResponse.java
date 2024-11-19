package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;

@Getter
public class AuthResponse {
    private final String jwt;
    private final Long _id;

    public AuthResponse(String jwt, Long id) {
        this.jwt = jwt;
        this._id = id;
    }


}
