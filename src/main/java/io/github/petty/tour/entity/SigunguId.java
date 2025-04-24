package io.github.petty.tour.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Important for composite keys
public class SigunguId implements Serializable {
    private Integer areaCode;
    private Integer sigunguCode;
}