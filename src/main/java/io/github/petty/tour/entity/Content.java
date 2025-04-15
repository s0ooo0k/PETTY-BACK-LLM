package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @Column(name = "contentid")
    private Long id;

    private Integer contenttypeid;

    private String title;

    private String addr1;
    private String addr2;

    private Integer areacode;
    private Integer sigungucode;

    private String cat1;
    private String cat2;
    private String cat3;

    private LocalDateTime createdtime;
    private LocalDateTime modifiedtime;

    private String firstimage;
    private String firstimage2;

    private String cpyrhtDivCd;

    private Double mapx;
    private Double mapy;

    private Integer mlevel;

    private String tel;
    private String telname;

    private String homepage;

    @Column(columnDefinition = "TEXT")
    private String overview;

    private String zipcode;

    // 연관 관계
    @OneToOne(mappedBy = "content", fetch = FetchType.LAZY)
    private PetTourInfo petTourInfo;
}