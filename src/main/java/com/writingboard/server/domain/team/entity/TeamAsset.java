package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_asset_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_asset_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_asset_uploader"))
    private Member uploader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_asset_id", foreignKey = @ForeignKey(name = "fk_team_asset_parent"))
    private TeamAsset parentAsset;

    @Column(name = "origin_personal_asset_id")
    private Long originPersonalAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 20, nullable = false)
    private AssetSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AssetType type;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AssetStatus status;

    public static TeamAsset create(Team team, Member uploader, AssetType type, String name, AssetSourceType sourceType) {
        TeamAsset asset = new TeamAsset();
        asset.team = team;
        asset.uploader = uploader;
        asset.type = type;
        asset.name = name;
        asset.sourceType = sourceType;
        asset.version = 1;
        asset.isLatest = true;
        asset.status = AssetStatus.ACTIVE;
        return asset;
    }

    public static TeamAsset createImported(Team team, Member uploader, AssetType type, String name, Long originPersonalAssetId) {
        TeamAsset asset = create(team, uploader, type, name, AssetSourceType.IMPORTED);
        asset.originPersonalAssetId = originPersonalAssetId;
        return asset;
    }

    public TeamAsset createNewVersion(Member uploader) {
        TeamAsset newVersion = new TeamAsset();
        newVersion.team = this.team;
        newVersion.uploader = uploader;
        newVersion.parentAsset = this.parentAsset != null ? this.parentAsset : this;
        newVersion.originPersonalAssetId = this.originPersonalAssetId;
        newVersion.sourceType = this.sourceType;
        newVersion.type = this.type;
        newVersion.name = this.name;
        newVersion.version = this.version + 1;
        newVersion.isLatest = true;
        newVersion.status = AssetStatus.ACTIVE;
        return newVersion;
    }

    public void markAsNotLatest() {
        this.isLatest = false;
    }

    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public void delete() {
        this.status = AssetStatus.DELETED;
    }

    public boolean isDeleted() {
        return this.status == AssetStatus.DELETED;
    }

    public boolean isUploader(Long memberId) {
        return this.uploader.getId().equals(memberId);
    }
}
