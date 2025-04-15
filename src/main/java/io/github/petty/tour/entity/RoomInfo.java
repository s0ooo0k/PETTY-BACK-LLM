package io.github.petty.tour.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Room_Info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long contentid;

    private String roomTitle;
    private String roomSize;
    private String roomCnt;
    private String roomBaseCnt;
    private String roomMaxCnt;
    private String roomBath;
    private String roomInfo;
    private String roomEtc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contentid", insertable = false, updatable = false)
    private Content content;
}
