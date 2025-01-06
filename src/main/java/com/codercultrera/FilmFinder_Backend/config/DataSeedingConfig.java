package com.codercultrera.FilmFinder_Backend.config;

import com.codercultrera.FilmFinder_Backend.domain.Role;
import com.codercultrera.FilmFinder_Backend.domain.RoleType;
import com.codercultrera.FilmFinder_Backend.service.RoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeedingConfig {

    @Bean
    public CommandLineRunner seedRoles(RoleService roleService) {
        return args -> {
            if (!roleService.existsByRoleType(RoleType.ROLE_USER)) {
                roleService.save(new Role(RoleType.valueOf("ROLE_USER")));
            }
            if (!roleService.existsByRoleType(RoleType.valueOf("ROLE_ADMIN"))) {
                roleService.save(new Role(RoleType.ROLE_ADMIN));
            }
        };
    }
}
