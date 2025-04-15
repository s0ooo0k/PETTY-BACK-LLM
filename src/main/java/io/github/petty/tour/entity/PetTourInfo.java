package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Pet_Tour_Info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetTourInfo {

    @Id
    @Column(name = "contentid")
    private Long id;

    @OneToOne
    @JoinColumn(name = "contentid")
    @MapsId
    private Content content;

    @Column(columnDefinition = "TEXT")
    private String rela_acmd_rstr_mtr;

    private String acmpy_type;
    private String rela_poss_cfrly;
    private String rela_frmsh_prdlst;
    private String etc_acmpy_info;
    private String rela_purc_prdlst;
    private String acmpy_psbl_cpm;
    private String rela_mnl_prdlst;
    private String acmpy_need_mtrl;
}
