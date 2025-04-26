package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sigungu")
@IdClass(SigunguId.class) // Specify the composite key class
@Getter
@Setter
public class Sigungu {

    @Id
    @Column(name = "areacode")
    private Integer areaCode;

    @Id
    @Column(name = "sigungucode")
    private Integer sigunguCode;

    @Column(name = "sigunguname", nullable = false, length = 20)
    private String sigunguName;


}