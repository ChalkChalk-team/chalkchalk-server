package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.DeactivateDeviceTokenRequest;
import com.writingboard.server.domain.member.dto.request.RegisterDeviceTokenRequest;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.MemberDeviceToken;
import com.writingboard.server.domain.member.exception.ErrorCode;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.MemberDeviceTokenRepository;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDeviceTokenService {

    private static final int MAX_ENABLED_TOKENS_PER_MEMBER = 10;
    private static final Pattern APNS_TOKEN_PATTERN = Pattern.compile("^[a-f0-9]{32,512}$");

    private final MemberRepository memberRepository;
    private final MemberDeviceTokenRepository memberDeviceTokenRepository;

    @Transactional
    public void registerOrUpdate(Long memberId, RegisterDeviceTokenRequest request) {
        Member member = getActiveMemberById(memberId);
        String normalizedToken = normalizeToken(request.getApnsToken());

        MemberDeviceToken existingToken = memberDeviceTokenRepository.findByApnsToken(normalizedToken)
                .orElse(null);

        if (existingToken != null) {
            if (!existingToken.isOwnedBy(memberId)) {
                log.warn("device-token-register conflict memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
                throw new MemberException(ErrorCode.DEVICE_TOKEN_CONFLICT);
            }

            existingToken.activate(Instant.now());
            log.info("device-token-register updated memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
            return;
        }

        enforceEnabledTokenCap(memberId);
        try {
            memberDeviceTokenRepository.save(MemberDeviceToken.create(member, normalizedToken, Instant.now()));
            log.info("device-token-register created memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
        } catch (DataIntegrityViolationException e) {

            MemberDeviceToken racedToken = memberDeviceTokenRepository.findByApnsToken(normalizedToken)
                    .orElseThrow(() -> e);

            if (!racedToken.isOwnedBy(memberId)) {
                log.warn("device-token-register conflict-after-race memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
                throw new MemberException(ErrorCode.DEVICE_TOKEN_CONFLICT);
            }

            racedToken.activate(Instant.now());
            log.info("device-token-register recovered-after-race memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
        }
    }

    @Transactional
    public void deactivate(Long memberId, DeactivateDeviceTokenRequest request) {
        getActiveMemberById(memberId);

        String normalizedToken = normalizeToken(request.getApnsToken());
        MemberDeviceToken existingToken = memberDeviceTokenRepository.findByApnsToken(normalizedToken)
                .orElseThrow(() -> new MemberException(ErrorCode.DEVICE_TOKEN_NOT_FOUND));

        if (!existingToken.isOwnedBy(memberId)) {
            log.warn("device-token-deactivate forbidden memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
            throw new MemberException(ErrorCode.DEVICE_TOKEN_NOT_OWNED);
        }

        existingToken.deactivate(Instant.now());
        log.info("device-token-deactivate success memberId={} tokenMasked={}", memberId, maskToken(normalizedToken));
    }

    private void enforceEnabledTokenCap(Long memberId) {
        List<MemberDeviceToken> enabledTokens = memberDeviceTokenRepository
                .findByMemberIdAndEnabledTrueOrderByLastSeenAtAsc(memberId);

        if (enabledTokens.size() < MAX_ENABLED_TOKENS_PER_MEMBER) {
            return;
        }

        int toDeactivateCount = enabledTokens.size() - MAX_ENABLED_TOKENS_PER_MEMBER + 1;
        Instant now = Instant.now();
        for (int i = 0; i < toDeactivateCount; i++) {
            MemberDeviceToken token = enabledTokens.get(i);
            token.deactivate(now);
            log.info("device-token-cap auto-deactivate memberId={} tokenMasked={}", memberId, maskToken(token.getApnsToken()));
        }
    }

    private Member getActiveMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.ALREADY_DELETED);
        }

        return member;
    }

    private String normalizeToken(String token) {
        if (token == null) {
            throw new MemberException(ErrorCode.DEVICE_TOKEN_INVALID);
        }

        String normalized = token.trim().toLowerCase(Locale.ROOT);
        if (!APNS_TOKEN_PATTERN.matcher(normalized).matches()) {
            throw new MemberException(ErrorCode.DEVICE_TOKEN_INVALID);
        }

        return normalized;
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "(empty)";
        }

        int prefixLength = Math.min(6, token.length());
        return token.substring(0, prefixLength) + "...(len=" + token.length() + ")";
    }
}
