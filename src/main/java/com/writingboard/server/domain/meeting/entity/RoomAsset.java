package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_asset_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_asset_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_asset_id", foreignKey = @ForeignKey(name = "fk_room_asset_team_asset"))
    private TeamAsset teamAsset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_asset_added_by"))
    private Member addedBy;

    @Column(name = "current_page_number", nullable = false)
    private Integer currentPageNumber = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "save_policy", nullable = false)
    private SavePolicy savePolicy;

    @OneToMany(mappedBy = "roomAsset", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CanvasPage> canvasPages = new ArrayList<>();

    public static RoomAsset createFromTeamAsset(Room room, TeamAsset teamAsset, Member addedBy, SavePolicy savePolicy) {
        RoomAsset asset = new RoomAsset();
        asset.room = room;
        asset.teamAsset = teamAsset;
        asset.addedBy = addedBy;
        asset.savePolicy = savePolicy;
        asset.currentPageNumber = 0;
        asset.isActive = false;
        return asset;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changePage(int pageNumber) {
        if (pageNumber >= 0) {
            this.currentPageNumber = pageNumber;
        }
    }

    public boolean isAddedBy(Long memberId) {
        return this.addedBy.getId().equals(memberId);
    }
}
