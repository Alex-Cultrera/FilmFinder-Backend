package com.codercultrera.FilmFinder_Backend.repository;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

    Optional<Role> findByRoleType(RoleType roleType);

    boolean existsByRoleType(RoleType roleType);
}
