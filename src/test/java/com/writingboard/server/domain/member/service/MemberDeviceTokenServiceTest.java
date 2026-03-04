package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.DeactivateDeviceTokenRequest;
import com.writingboard.server.domain.member.dto.request.RegisterDeviceTokenRequest;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.MemberDeviceToken;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.MemberDeviceTokenRepository;
import com.writingboard.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberDeviceTokenService 단위 테스트")
class MemberDeviceTokenServiceTest {

    @InjectMocks
    private MemberDeviceTokenService memberDeviceTokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberDeviceTokenRepository memberDeviceTokenRepository;

    private static final Long MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final String TOKEN = "abcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcdefabcd";

    private Member member;

    @BeforeEach
    void setUp() {
        member = org.mockito.Mockito.mock(Member.class);
        lenient().when(member.getId()).thenReturn(MEMBER_ID);
        lenient().when(member.isDeleted()).thenReturn(false);
    }

    @Test
    @DisplayName("registerOrUpdate: 신규 토큰이면 생성한다")
    void registerOrUpdate_create() {
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest();
        ReflectionTestUtils.setField(request, "apnsToken", TOKEN);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(memberDeviceTokenRepository.findByApnsToken(TOKEN)).willReturn(Optional.empty());
        given(memberDeviceTokenRepository.findByMemberIdAndEnabledTrueOrderByLastSeenAtAsc(MEMBER_ID)).willReturn(List.of());

        memberDeviceTokenService.registerOrUpdate(MEMBER_ID, request);

        ArgumentCaptor<MemberDeviceToken> captor = ArgumentCaptor.forClass(MemberDeviceToken.class);
        verify(memberDeviceTokenRepository).save(captor.capture());
        MemberDeviceToken saved = captor.getValue();
        assertThat(saved.getApnsToken()).isEqualTo(TOKEN);
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getLastSeenAt()).isNotNull();
    }

    @Test
    @DisplayName("registerOrUpdate: 동일 회원의 기존 토큰이면 활성화/갱신한다")
    void registerOrUpdate_upsertSameMember() {
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest();
        ReflectionTestUtils.setField(request, "apnsToken", TOKEN);

        MemberDeviceToken existingToken = MemberDeviceToken.create(member, TOKEN, Instant.now().minusSeconds(3600));
        existingToken.deactivate(Instant.now().minusSeconds(1800));

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(memberDeviceTokenRepository.findByApnsToken(TOKEN)).willReturn(Optional.of(existingToken));

        memberDeviceTokenService.registerOrUpdate(MEMBER_ID, request);

        verify(memberDeviceTokenRepository, never()).save(any(MemberDeviceToken.class));
        assertThat(existingToken.isEnabled()).isTrue();
        assertThat(existingToken.getLastSeenAt()).isNotNull();
    }

    @Test
    @DisplayName("registerOrUpdate: 다른 회원 소유 토큰이면 conflict 예외")
    void registerOrUpdate_conflict() {
        RegisterDeviceTokenRequest request = new RegisterDeviceTokenRequest();
        ReflectionTestUtils.setField(request, "apnsToken", TOKEN);

        Member otherMember = org.mockito.Mockito.mock(Member.class);
        given(otherMember.getId()).willReturn(OTHER_MEMBER_ID);

        MemberDeviceToken existingToken = MemberDeviceToken.create(otherMember, TOKEN, Instant.now());

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(memberDeviceTokenRepository.findByApnsToken(TOKEN)).willReturn(Optional.of(existingToken));

        assertThatThrownBy(() -> memberDeviceTokenService.registerOrUpdate(MEMBER_ID, request))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("다른 사용자");
    }

    @Test
    @DisplayName("deactivate: 본인 토큰이면 비활성화한다")
    void deactivate_success() {
        DeactivateDeviceTokenRequest request = new DeactivateDeviceTokenRequest();
        ReflectionTestUtils.setField(request, "apnsToken", TOKEN);

        MemberDeviceToken existingToken = MemberDeviceToken.create(member, TOKEN, Instant.now());

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(memberDeviceTokenRepository.findByApnsToken(TOKEN)).willReturn(Optional.of(existingToken));

        memberDeviceTokenService.deactivate(MEMBER_ID, request);

        assertThat(existingToken.isEnabled()).isFalse();
        assertThat(existingToken.getLastSeenAt()).isNotNull();
    }
}
