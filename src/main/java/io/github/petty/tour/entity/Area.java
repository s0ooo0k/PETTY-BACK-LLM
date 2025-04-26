package io.github.petty.tour.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "area")
@Getter
@Setter // Or make fields final and use constructor if immutable
public class Area {

    @Id
    @Column(name = "areacode")
    private Integer areaCode;

    @Column(name = "areaname", nullable = false, length = 20)
    private String areaName;

    // Default constructor, equals, hashCode, toString (Lombok can generate)
    // Consider adding @OneToMany relationship to Sigungu if needed elsewhere,
    // but not strictly required for this code lookup functionality.
}