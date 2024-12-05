package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;

@Getter
public class AuthResponse {
    private final String access_jwt;
    private final Long user_id;

    public AuthResponse(String access_jwt, Long id) {
        this.access_jwt = access_jwt;
        this.user_id = id;
    }


}
