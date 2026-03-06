package com.writingboard.server.domain.member.controller;

import com.writingboard.server.domain.member.dto.request.MemberUpdateRequest;
import com.writingboard.server.domain.member.dto.response.MemberProfileResponse;
import com.writingboard.server.domain.member.dto.response.UserIdAvailabilityResponse;
import com.writingboard.server.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * userId 가용성 확인
     */
    @GetMapping("/user-id/availability")
    public ResponseEntity<UserIdAvailabilityResponse> checkUserIdAvailability(
            @RequestParam String userId) {
        return ResponseEntity.ok(memberService.checkUserIdAvailability(userId));
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(memberService.getMyProfile(memberId));
    }

    /**
     * 내 정보 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<MemberProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid MemberUpdateRequest request) {
        return ResponseEntity.ok(memberService.updateProfile(memberId, request));
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal Long memberId) {
        memberService.withdraw(memberId);
        return ResponseEntity.noContent().build();
    }
}