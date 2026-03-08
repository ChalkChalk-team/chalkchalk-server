package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.DeviceTokenRegisterRequest;
import com.writingboard.server.domain.member.dto.response.DeviceTokenResponse;
import com.writingboard.server.domain.member.entity.DeviceToken;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.enums.DeviceTokenStatus;
import com.writingboard.server.domain.member.exception.ErrorCode;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.DeviceTokenRepository;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceTokenService {

    // APNs device token: 64 hexadecimal characters
    private static final Pattern DEVICE_TOKEN_PATTERN = Pattern.compile("^[0-9a-fA-F]{64}$");

    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;

    /**
     * 디바이스 토큰 등록 (Upsert)
     * - 토큰이 이미 존재하면 갱신 (다른 회원의 토큰이면 재할당)
     * - 토큰이 존재하지 않으면 신규 생성
     */
    @Transactional
    public DeviceTokenResponse registerDeviceToken(Long memberId, DeviceTokenRegisterRequest request) {
        Member member = getMemberById(memberId);

        validateTokenFormat(request.getToken());

        // 기존 토큰 조회
        DeviceToken deviceToken = deviceTokenRepository.findByToken(request.getToken())
                .map(existingToken -> {
                    // 다른 회원의 토큰이면 재할당
                    if (!existingToken.getMember().getId().equals(memberId)) {
                        existingToken.reassignTo(member, request.getDeviceModel(), request.getOsVersion());
                        return existingToken;
                    }
                    // 같은 회원의 토큰이면 갱신
                    existingToken.refresh(request.getDeviceModel(), request.getOsVersion());
                    return existingToken;
                })
                .orElseGet(() -> DeviceToken.create(
                        request.getToken(),
                        member,
                        request.getDeviceModel(),
                        request.getOsVersion()
                ));

        DeviceToken saved = deviceTokenRepository.save(deviceToken);
        return DeviceTokenResponse.of(saved);
    }

    /**
     * 특정 회원의 활성 토큰 조회 (푸시 발송용 - 내부 서비스 호출)
     */
    public List<String> getActiveTokensByMemberId(Long memberId) {
        Member member = getMemberById(memberId);

        return deviceTokenRepository.findByMemberAndStatus(member, DeviceTokenStatus.ACTIVE)
                .stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());
    }

    private void validateTokenFormat(String token) {
        if (token == null || token.isBlank()) {
            throw new MemberException(ErrorCode.DEVICE_TOKEN_INVALID_FORMAT, "token");
        }

        if (!DEVICE_TOKEN_PATTERN.matcher(token).matches()) {
            throw new MemberException(ErrorCode.DEVICE_TOKEN_INVALID_FORMAT, "token");
        }
    }

    private Member getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.ALREADY_DELETED);
        }
        return member;
    }
}
