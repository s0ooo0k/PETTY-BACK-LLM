package io.github.petty.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username; // email

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String displayName;
    private String phone;

    @Column(nullable = false)
    private String role;

    // Oauth2 관련
    @Column(nullable = false)
    private String provider; // oauth2 provider
    private String providerId; // oauth2 provider id

    @CreationTimestamp
    private LocalDateTime createdAt;
}
