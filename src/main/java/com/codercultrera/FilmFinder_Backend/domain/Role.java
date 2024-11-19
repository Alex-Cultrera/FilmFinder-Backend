package com.codercultrera.FilmFinder_Backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    public Role(Long id, RoleType roleType) {
        this.id = id;
        this.roleType = roleType;
    }
}
