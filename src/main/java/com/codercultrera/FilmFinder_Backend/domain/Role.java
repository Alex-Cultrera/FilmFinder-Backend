package com.codercultrera.FilmFinder_Backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    @ManyToMany(mappedBy = "roles")
    private List<User> users = new ArrayList<>();


    public Role(RoleType roleType) {
        this.roleType = roleType;
    }
}
