package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;

@Entity
public class RoomAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_asset_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_asset_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_asset_id", foreignKey = @ForeignKey(name = "fk_room_asset_team_asset"))
    private TeamAsset teamAsset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_asset_member"))
    private Member member; // 회의실에 업로드한 멤버

    @Column(name = "current_page_number")
    private Integer currentPageNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "save_policy", nullable = false)
    private SavePolicy savePolicy;

}
