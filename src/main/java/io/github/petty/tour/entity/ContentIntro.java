package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Content_Intro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentIntro {

    @Id
    private Long contentid;

    @OneToOne
    @JoinColumn(name = "contentid")
    @MapsId
    private Content content;

    private String chkPetYn;
    private String infoCenter;
    private String openDate;
    private String parking;
    private String restDate;
    private String useSeason;
    private String useTime;
    private String accomCount;
    private String petFacility;
    private String petEtc;
}
