package io.github.petty.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post_images")
@ToString(exclude = "post")
@EqualsAndHashCode(of = "id")
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 게시글 연관관계 (필수)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @NotBlank(message = "이미지 URL은 필수입니다")
    @Size(max = 500, message = "이미지 URL은 500자 이내여야 합니다")
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    // 이미지 순서 (0~4)
    @Min(value = 0, message = "이미지 순서는 0 이상이어야 합니다")
    @Max(value = 4, message = "이미지 순서는 4 이하여야 합니다")
    @Builder.Default
    @Column(nullable = false)
    private Integer ordering = 0;

    // 유틸 메서드 (양방향 연관관계 설정)
    public void setPost(Post post) {
        this.post = post;
        if (!post.getImages().contains(this)) {
            post.getImages().add(this);
        }
    }
}
