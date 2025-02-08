package com.codercultrera.FilmFinder_Backend.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/uploadProfilePhoto")
    public ResponseEntity<?> uploadProfilePhoto(@AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload) {
        try {
            String photoUrl = userService.updateProfilePhoto(user, payload.get("profilePhotoUrl"));
            return ResponseEntity.ok().body(Map.of("profilePhotoUrl", photoUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload photo: " + e.getMessage()));
        }
    }

    @PutMapping("/user/password/update")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload,
            @AuthenticationPrincipal User user) {
        try {
            String newPassword = payload.get("newPassword");
            userService.updatePassword(user, newPassword);
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update password"));
        }
    }

}
