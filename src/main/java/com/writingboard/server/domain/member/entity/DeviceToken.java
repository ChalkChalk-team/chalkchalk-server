package com.writingboard.server.domain.member.entity;

import com.writingboard.server.domain.member.entity.enums.DeviceTokenStatus;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_device_token", columnNames = "token"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Long id;

    @Column(name = "token", length = 255, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DeviceTokenStatus status = DeviceTokenStatus.ACTIVE;

    @Column(name = "device_model", length = 50)
    private String deviceModel;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    /**
     * 새 디바이스 토큰 생성
     */
    public static DeviceToken create(String token, Member member, String deviceModel, String osVersion) {
        DeviceToken deviceToken = new DeviceToken();
        deviceToken.token = token;
        deviceToken.member = member;
        deviceToken.status = DeviceTokenStatus.ACTIVE;
        deviceToken.deviceModel = deviceModel;
        deviceToken.osVersion = osVersion;
        return deviceToken;
    }

    /**
     * 토큰 갱신
     */
    public void refresh(String deviceModel, String osVersion) {
        if (this.status == DeviceTokenStatus.DELETED) {
            this.status = DeviceTokenStatus.ACTIVE;
        }
        if (deviceModel != null && !deviceModel.isBlank()) {
            this.deviceModel = deviceModel;
        }
        if (osVersion != null && !osVersion.isBlank()) {
            this.osVersion = osVersion;
        }
    }

    /**
     * 토큰 재할당 (다른 회원에게 토큰 소유권 이전)
     */
    public void reassignTo(Member member, String deviceModel, String osVersion) {
        this.member = member;
        this.status = DeviceTokenStatus.ACTIVE;
        if (deviceModel != null && !deviceModel.isBlank()) {
            this.deviceModel = deviceModel;
        }
        if (osVersion != null && !osVersion.isBlank()) {
            this.osVersion = osVersion;
        }
    }

    public void delete() {
        this.status = DeviceTokenStatus.DELETED;
    }


    public boolean isDeleted() {
        return this.status == DeviceTokenStatus.DELETED;
    }


    public boolean isActive() {
        return this.status == DeviceTokenStatus.ACTIVE;
    }
}
