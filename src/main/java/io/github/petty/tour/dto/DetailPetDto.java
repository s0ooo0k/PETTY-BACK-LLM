package io.github.petty.tour.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DetailPetDto {

    private Long contentId;
    private String relaAcdntRiskMtr;
    private String acmpyTypeCd;
    private String relaPosesFclty;
    private String relaFrnshPrdlst;
    private String etcAcmpyInfo;
    private String relaPurcPrdlst;
    private String acmpyPsblCpam;
    private String relaRntlPrdlst;
    private String acmpyNeedMtr;
}
