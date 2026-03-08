package com.writingboard.server.domain.member.repository;

import com.writingboard.server.domain.member.entity.DeviceToken;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.enums.DeviceTokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByMemberAndStatus(Member member, DeviceTokenStatus status);

    boolean existsByToken(String token);
}
