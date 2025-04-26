package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetailImageDto {

    private Long contentId;
    private String imgName;
    private String originImgUrl;
    private String serialNum;
    private String smallImageUrl;
    private String cpyrhtDivCd;
}