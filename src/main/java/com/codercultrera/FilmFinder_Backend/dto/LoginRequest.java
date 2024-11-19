package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String firstName, String lastName, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
    }



}
