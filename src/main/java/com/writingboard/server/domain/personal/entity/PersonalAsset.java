package com.writingboard.server.domain.personal.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "personal_asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personal_asset_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personal_asset_member"))
    private Member member;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AssetType type;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static PersonalAsset create(Member member, String name, AssetType type, String storageKey, Long sizeBytes, Integer totalPages) {
        PersonalAsset asset = new PersonalAsset();
        asset.member = member;
        asset.name = name;
        asset.type = type;
        asset.storageKey = storageKey;
        asset.sizeBytes = sizeBytes;
        asset.totalPages = totalPages;
        return asset;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }
}
