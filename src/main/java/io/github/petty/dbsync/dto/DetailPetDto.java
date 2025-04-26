package io.github.petty.dbsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

// 5. detailPetTour 용 Item DTO
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class DetailPetDto {

    @JsonProperty("contentid")
    private Long contentId;
    private String relaAcdntRiskMtr; // Text
    private String acmpyTypeCd;
    private String relaPosesFclty; // Text
    private String relaFrnshPrdlst; // Text
    private String etcAcmpyInfo; // Text
    private String relaPurcPrdlst; // Text
    private String acmpyPsblCpam; // Text
    private String relaRntlPrdlst; // Text
    private String acmpyNeedMtr; // Text
}
