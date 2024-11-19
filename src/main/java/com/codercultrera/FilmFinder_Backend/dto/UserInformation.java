package com.codercultrera.FilmFinder_Backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInformation {
    private String userId;
    private String email;

    public UserInformation(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserInformation{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
