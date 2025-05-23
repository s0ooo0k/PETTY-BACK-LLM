package io.github.petty.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(nullable = false)
    private String provider;  // 인증 제공자 (local, github 등)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean used;

    // 토큰을 사용됨으로 표시
    public void markUsed() {
        this.used = true;
    }

    // 토큰이 만료되었는지 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }

}
