package com.codercultrera.FilmFinder_Backend.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsUserByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<User> findById(Long userId) {
        return userRepo.findById(Long.valueOf(userId));
    }

    public String updateProfilePhoto(User user, String photoUrl) {
        if (photoUrl == null || photoUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Photo URL cannot be empty");
        }

        user.setPhoto(photoUrl);
        userRepo.save(user);

        return photoUrl;
    }

    public void updatePassword(User user, String newPassword) {
        String newEncryptedPassword = new BCryptPasswordEncoder(12).encode(newPassword);
        user.setPassword(newEncryptedPassword);
        userRepo.save(user);
    }

}
