package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Content_Info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentid;

    private String infoname;

    @Column(columnDefinition = "TEXT")
    private String infotext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentid", insertable = false, updatable = false)
    private Content content;
}
