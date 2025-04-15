package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Content_Image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentid;

    private String originimgurl;
    private String smallimageurl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentid", insertable = false, updatable = false)
    private Content content;
}
