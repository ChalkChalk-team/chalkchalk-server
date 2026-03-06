package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.MemberUpdateRequest;
import com.writingboard.server.domain.member.dto.response.MemberProfileResponse;
import com.writingboard.server.domain.member.dto.response.UserIdAvailabilityResponse;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.exception.ErrorCode;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private final MemberRepository memberRepository;

    /**
     * 내 정보 조회
     */
    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = getMemberById(memberId);
        return MemberProfileResponse.of(member);
    }

    /**
     * userId 가용성 확인 (온보딩 실시간 중복검사용)
     */
    public UserIdAvailabilityResponse checkUserIdAvailability(String userId) {
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            throw new MemberException(ErrorCode.USER_ID_INVALID_FORMAT);
        }

        if (memberRepository.existsByUserId(userId)) {
            return UserIdAvailabilityResponse.duplicate();
        }

        return UserIdAvailabilityResponse.available();
    }

    /**
     * 내 정보 수정
     * userId 대소문자 정책: 대소문자 구분(case-sensitive). DB 유니크 인덱스가 최종 보장.
     */
    @Transactional
    public MemberProfileResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getMemberById(memberId);

        validateUserIdIfPresent(request.getUserId(), member);

        try {
            member.updateProfile(request.getName(), request.getProfileImageUrl(), request.getUserId(), request.getNickname());
            memberRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new MemberException(ErrorCode.USER_ID_DUPLICATE, "userId");
        }

        return MemberProfileResponse.of(member);
    }

    private void validateUserIdIfPresent(String userId, Member member) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            throw new MemberException(ErrorCode.USER_ID_INVALID_FORMAT, "userId");
        }

        // 본인 기존 userId 유지는 중복으로 처리하지 않음
        if (userId.equals(member.getUserId())) {
            return;
        }

        if (memberRepository.existsByUserId(userId)) {
            throw new MemberException(ErrorCode.USER_ID_DUPLICATE, "userId");
        }
    }


    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMemberById(memberId);

        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.ALREADY_DELETED);
        }

        member.withdraw();
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