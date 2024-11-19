package com.codercultrera.FilmFinder_Backend.repository;

import com.codercultrera.FilmFinder_Backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    User findByUsername(String username);

    boolean existsUserByEmail(String email);

    User findByEmail(String email);
}
