package com.codercultrera.FilmFinder_Backend.service;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User findByUsername (String username) {
        return userRepo.findByUsername(username);
    }

    public void save (User user) {
        userRepo.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepo.existsUserByEmail(email);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public void updateProfilePhoto(String userId, String photoUrl) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPhoto(photoUrl);
        userRepo.save(user);
    }

    public Optional<User> findById(String userId) {
        return userRepo.findById(Long.valueOf(userId));
    }
}
