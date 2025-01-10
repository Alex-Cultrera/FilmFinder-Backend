package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;

@Getter
public class AuthResponse {
    private final Long user_id;
    private final String first_name;

    public AuthResponse(Long id, String first_name) {
        this.user_id = id;
        this.first_name = first_name;
    }


}
