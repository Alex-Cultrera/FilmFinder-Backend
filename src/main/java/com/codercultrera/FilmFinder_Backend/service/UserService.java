package com.codercultrera.FilmFinder_Backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codercultrera.FilmFinder_Backend.domain.User;
import com.codercultrera.FilmFinder_Backend.repository.UserRepository;

import io.jsonwebtoken.io.IOException;
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

    public boolean updateProfilePhoto(String userId, MultipartFile file) {
        try {
            Optional<User> userOptional = findById(Long.valueOf(userId));
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                byte[] profilePhoto = file.getBytes();
                user.setProfilePhoto(profilePhoto);
                userRepo.save(user);
                return true;
            }
        } catch (IOException | java.io.IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public byte[] getProfilePhoto(Long userId) {
        Optional<User> userOptional = userRepo.findById(userId);
        return userOptional.map(User::getProfilePhoto).orElse(null);
    }

}
