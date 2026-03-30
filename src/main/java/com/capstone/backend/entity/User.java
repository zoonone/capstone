package com.capstone.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    @Column(name = "baekjoon_id", length = 50)
    private String baekjoonId;

    @Column(name = "experience_level", nullable = false, length = 20)
    @Builder.Default
    private String experienceLevel = "BEGINNER";

    @Column(name = "github_url")
    private String githubUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(unique = true)
    private String providerId;

}
