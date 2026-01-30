package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "team_profile_img_url", length = 255)
    private String teamProfileImgUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_creator"))
    private Member createdBy;

    public static Team create(Member creator, String name, String description) {
        Team team = new Team();
        team.createdBy = creator;
        team.name = name;
        team.description = description;
        return team;
    }

    public void updateInfo(String name, String description, String teamProfileImgUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (teamProfileImgUrl != null) {
            this.teamProfileImgUrl = teamProfileImgUrl;
        }
    }
}
