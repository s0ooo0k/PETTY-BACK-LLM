package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "Pet_Tour_Info")
public class PetTourInfo {
    @Id
    @Column(name = "contentid", nullable = false)
    private Long contentId;


    @Lob
    @Column(name = "rela_acdnt_risk_mtr")
    private String relaAcdntRiskMtr;

    @Column(name = "acmpy_type_cd", length = 30)
    private String acmpyTypeCd;

    @Lob
    @Column(name = "rela_poses_fclty")
    private String relaPosesFclty;

    @Lob
    @Column(name = "rela_frnsh_prdlst")
    private String relaFrnshPrdlst;

    @Lob
    @Column(name = "etc_acmpy_info")
    private String etcAcmpyInfo;

    @Lob
    @Column(name = "rela_purc_prdlst")
    private String relaPurcPrdlst;

    @Lob
    @Column(name = "acmpy_psbl_cpam")
    private String acmpyPsblCpam;

    @Lob
    @Column(name = "rela_rntl_prdlst")
    private String relaRntlPrdlst;

    @Lob
    @Column(name = "acmpy_need_mtr")
    private String acmpyNeedMtr;

}