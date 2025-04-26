package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class DetailInfoDto {

    private Long contentId;
    private Integer contentTypeId;

    // ContentInfo 필드 (contentTypeId != 32)
    private String fldGubun;
    private String infoName;
    private String infoText;
    private String serialNum;

}