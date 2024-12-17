package com.codercultrera.FilmFinder_Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleApiRequest {

    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhoto;

}
