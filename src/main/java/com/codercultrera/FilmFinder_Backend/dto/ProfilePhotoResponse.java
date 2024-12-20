package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePhotoResponse {

    private String photoUrl;

    public ProfilePhotoResponse(String photoUrl) {
        this.photoUrl = photoUrl;
    }

}
