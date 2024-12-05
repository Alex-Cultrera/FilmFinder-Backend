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
            if (!roleService.existsByRoleType(RoleType.USER)) {
                roleService.save(new Role(RoleType.valueOf("USER")));
            }
            if (!roleService.existsByRoleType(RoleType.valueOf("ADMIN"))) {
                roleService.save(new Role(RoleType.ADMIN));
            }
        };
    }
}
