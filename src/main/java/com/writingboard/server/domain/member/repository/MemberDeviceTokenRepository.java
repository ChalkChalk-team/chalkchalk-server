package com.writingboard.server.domain.member.repository;

import com.writingboard.server.domain.member.entity.MemberDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberDeviceTokenRepository extends JpaRepository<MemberDeviceToken, Long> {

    Optional<MemberDeviceToken> findByApnsToken(String apnsToken);

    List<MemberDeviceToken> findByMemberIdAndEnabledTrue(Long memberId);

    List<MemberDeviceToken> findByMemberIdAndEnabledTrueOrderByLastSeenAtAsc(Long memberId);
}
