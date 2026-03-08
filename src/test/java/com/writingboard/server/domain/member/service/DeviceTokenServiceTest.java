package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.DeviceTokenRegisterRequest;
import com.writingboard.server.domain.member.dto.response.DeviceTokenResponse;
import com.writingboard.server.domain.member.entity.DeviceToken;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.enums.DeviceTokenStatus;
import com.writingboard.server.domain.member.entity.enums.MemberStatus;
import com.writingboard.server.domain.member.exception.ErrorCode;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.DeviceTokenRepository;
import com.writingboard.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceTokenService 단위 테스트")
class DeviceTokenServiceTest {

    @InjectMocks
    private DeviceTokenService deviceTokenService;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private MemberRepository memberRepository;

    private static final Long MEMBER_ID_1 = 1L;
    private static final Long MEMBER_ID_2 = 2L;
    private static final String VALID_TOKEN = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
    private static final String ANOTHER_VALID_TOKEN = "fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321";
    private static final String INVALID_TOKEN = "invalid";
    private static final String DEVICE_MODEL = "iPhone 15 Pro";
    private static final String OS_VERSION = "iOS 17.2";
    private static final String UPDATED_DEVICE_MODEL = "iPhone 15 Pro Max";
    private static final String UPDATED_OS_VERSION = "iOS 17.3";

    private Member member1;
    private Member member2;
    private DeviceTokenRegisterRequest request;

    @BeforeEach
    void setUp() {
        member1 = mock(Member.class);
        member2 = mock(Member.class);
        lenient().when(member1.getId()).thenReturn(MEMBER_ID_1);
        lenient().when(member2.getId()).thenReturn(MEMBER_ID_2);
        lenient().when(member1.isDeleted()).thenReturn(false);
        lenient().when(member2.isDeleted()).thenReturn(false);

        request = new DeviceTokenRegisterRequest();
    }

    @Nested
    @DisplayName("registerDeviceToken")
    class RegisterDeviceToken {

        @Test
        @DisplayName("성공: 새 토큰 등록")
        void registerDeviceToken_성공_새토큰등록() {
            // given
            setRequestFields(VALID_TOKEN, DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));
            given(deviceTokenRepository.findByToken(VALID_TOKEN)).willReturn(Optional.empty());

            DeviceToken newToken = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            given(deviceTokenRepository.save(any(DeviceToken.class))).willReturn(newToken);

            // when
            DeviceTokenResponse response = deviceTokenService.registerDeviceToken(MEMBER_ID_1, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
            assertThat(response.getDeviceModel()).isEqualTo(DEVICE_MODEL);
            assertThat(response.getOsVersion()).isEqualTo(OS_VERSION);
            verify(deviceTokenRepository).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("성공: 같은 member + 같은 token 재등록 시 메타데이터 갱신")
        void registerDeviceToken_성공_같은멤버_같은토큰_재등록() {
            // given
            setRequestFields(VALID_TOKEN, UPDATED_DEVICE_MODEL, UPDATED_OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            DeviceToken existingToken = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            given(deviceTokenRepository.findByToken(VALID_TOKEN)).willReturn(Optional.of(existingToken));
            given(deviceTokenRepository.save(existingToken)).willReturn(existingToken);

            // when
            DeviceTokenResponse response = deviceTokenService.registerDeviceToken(MEMBER_ID_1, request);

            // then
            assertThat(response).isNotNull();
            assertThat(existingToken.getDeviceModel()).isEqualTo(UPDATED_DEVICE_MODEL);
            assertThat(existingToken.getOsVersion()).isEqualTo(UPDATED_OS_VERSION);
            assertThat(existingToken.isActive()).isTrue();
            verify(deviceTokenRepository).save(existingToken);
        }

        @Test
        @DisplayName("성공: 같은 member + 다른 token 등록 시 새 토큰 생성")
        void registerDeviceToken_성공_같은멤버_다른토큰() {
            // given
            setRequestFields(ANOTHER_VALID_TOKEN, DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));
            given(deviceTokenRepository.findByToken(ANOTHER_VALID_TOKEN)).willReturn(Optional.empty());

            DeviceToken newToken = DeviceToken.create(ANOTHER_VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            given(deviceTokenRepository.save(any(DeviceToken.class))).willReturn(newToken);

            // when
            DeviceTokenResponse response = deviceTokenService.registerDeviceToken(MEMBER_ID_1, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(ANOTHER_VALID_TOKEN);
            verify(deviceTokenRepository).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("성공: 다른 member + 같은 token 등록 시 기존 row 재할당")
        void registerDeviceToken_성공_다른멤버_같은토큰_재할당() {
            // given
            setRequestFields(VALID_TOKEN, UPDATED_DEVICE_MODEL, UPDATED_OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_2)).willReturn(Optional.of(member2));

            DeviceToken existingToken = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            given(deviceTokenRepository.findByToken(VALID_TOKEN)).willReturn(Optional.of(existingToken));
            given(deviceTokenRepository.save(existingToken)).willReturn(existingToken);

            // when
            DeviceTokenResponse response = deviceTokenService.registerDeviceToken(MEMBER_ID_2, request);

            // then
            assertThat(response).isNotNull();
            assertThat(existingToken.getMember()).isEqualTo(member2);
            assertThat(existingToken.getDeviceModel()).isEqualTo(UPDATED_DEVICE_MODEL);
            assertThat(existingToken.getOsVersion()).isEqualTo(UPDATED_OS_VERSION);
            assertThat(existingToken.isActive()).isTrue();
            verify(deviceTokenRepository).save(existingToken);
            verify(deviceTokenRepository, never()).flush();
        }

        @Test
        @DisplayName("성공: DELETED 토큰 재등록 시 ACTIVE 복구")
        void registerDeviceToken_성공_DELETED토큰_재등록() {
            // given
            setRequestFields(VALID_TOKEN, UPDATED_DEVICE_MODEL, UPDATED_OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            DeviceToken deletedToken = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            deletedToken.delete();
            given(deviceTokenRepository.findByToken(VALID_TOKEN)).willReturn(Optional.of(deletedToken));
            given(deviceTokenRepository.save(deletedToken)).willReturn(deletedToken);

            // when
            DeviceTokenResponse response = deviceTokenService.registerDeviceToken(MEMBER_ID_1, request);

            // then
            assertThat(response).isNotNull();
            assertThat(deletedToken.isActive()).isTrue();
            assertThat(deletedToken.isDeleted()).isFalse();
            assertThat(deletedToken.getDeviceModel()).isEqualTo(UPDATED_DEVICE_MODEL);
            assertThat(deletedToken.getOsVersion()).isEqualTo(UPDATED_OS_VERSION);
            verify(deviceTokenRepository).save(deletedToken);
        }

        @Test
        @DisplayName("실패: 유효하지 않은 토큰 형식")
        void registerDeviceToken_실패_유효하지않은토큰형식() {
            // given
            setRequestFields(INVALID_TOKEN, DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            // when & then
            assertThatThrownBy(() -> deviceTokenService.registerDeviceToken(MEMBER_ID_1, request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEVICE_TOKEN_INVALID_FORMAT);

            verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("실패: null 토큰")
        void registerDeviceToken_실패_null토큰() {
            // given
            setRequestFields(null, DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            // when & then
            assertThatThrownBy(() -> deviceTokenService.registerDeviceToken(MEMBER_ID_1, request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEVICE_TOKEN_INVALID_FORMAT);

            verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("실패: 빈 문자열 토큰")
        void registerDeviceToken_실패_빈문자열토큰() {
            // given
            setRequestFields("", DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            // when & then
            assertThatThrownBy(() -> deviceTokenService.registerDeviceToken(MEMBER_ID_1, request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEVICE_TOKEN_INVALID_FORMAT);

            verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("실패: Member가 존재하지 않음")
        void registerDeviceToken_실패_Member없음() {
            // given
            setRequestFields(VALID_TOKEN, DEVICE_MODEL, OS_VERSION);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deviceTokenService.registerDeviceToken(MEMBER_ID_1, request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
        }

        @Test
        @DisplayName("실패: 이미 탈퇴한 Member")
        void registerDeviceToken_실패_탈퇴한Member() {
            // given
            setRequestFields(VALID_TOKEN, DEVICE_MODEL, OS_VERSION);
            when(member1.isDeleted()).thenReturn(true);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            // when & then
            assertThatThrownBy(() -> deviceTokenService.registerDeviceToken(MEMBER_ID_1, request))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED);

            verify(deviceTokenRepository, never()).save(any(DeviceToken.class));
        }
    }

    @Nested
    @DisplayName("getActiveTokensByMemberId")
    class GetActiveTokensByMemberId {

        @Test
        @DisplayName("성공: ACTIVE 토큰만 조회")
        void getActiveTokensByMemberId_성공_ACTIVE토큰만조회() {
            // given
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            DeviceToken activeToken1 = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);
            DeviceToken activeToken2 = DeviceToken.create(ANOTHER_VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);

            given(deviceTokenRepository.findByMemberAndStatus(member1, DeviceTokenStatus.ACTIVE))
                    .willReturn(List.of(activeToken1, activeToken2));

            // when
            List<String> tokens = deviceTokenService.getActiveTokensByMemberId(MEMBER_ID_1);

            // then
            assertThat(tokens).hasSize(2);
            assertThat(tokens).contains(VALID_TOKEN, ANOTHER_VALID_TOKEN);
        }

        @Test
        @DisplayName("성공: DELETED 토큰은 조회되지 않음")
        void getActiveTokensByMemberId_성공_DELETED토큰제외() {
            // given
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            DeviceToken activeToken = DeviceToken.create(VALID_TOKEN, member1, DEVICE_MODEL, OS_VERSION);

            given(deviceTokenRepository.findByMemberAndStatus(member1, DeviceTokenStatus.ACTIVE))
                    .willReturn(List.of(activeToken));

            // when
            List<String> tokens = deviceTokenService.getActiveTokensByMemberId(MEMBER_ID_1);

            // then
            assertThat(tokens).hasSize(1);
            assertThat(tokens).contains(VALID_TOKEN);
            assertThat(tokens).doesNotContain(ANOTHER_VALID_TOKEN);
        }

        @Test
        @DisplayName("성공: 활성 토큰이 없으면 빈 리스트 반환")
        void getActiveTokensByMemberId_성공_빈리스트() {
            // given
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));
            given(deviceTokenRepository.findByMemberAndStatus(member1, DeviceTokenStatus.ACTIVE))
                    .willReturn(List.of());

            // when
            List<String> tokens = deviceTokenService.getActiveTokensByMemberId(MEMBER_ID_1);

            // then
            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("실패: Member가 존재하지 않음")
        void getActiveTokensByMemberId_실패_Member없음() {
            // given
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> deviceTokenService.getActiveTokensByMemberId(MEMBER_ID_1))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);

            verify(deviceTokenRepository, never()).findByMemberAndStatus(any(), any());
        }

        @Test
        @DisplayName("실패: 이미 탈퇴한 Member")
        void getActiveTokensByMemberId_실패_탈퇴한Member() {
            // given
            when(member1.isDeleted()).thenReturn(true);
            given(memberRepository.findById(MEMBER_ID_1)).willReturn(Optional.of(member1));

            // when & then
            assertThatThrownBy(() -> deviceTokenService.getActiveTokensByMemberId(MEMBER_ID_1))
                    .isInstanceOf(MemberException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED);

            verify(deviceTokenRepository, never()).findByMemberAndStatus(any(), any());
        }
    }

    private void setRequestFields(String token, String deviceModel, String osVersion) {
        try {
            var tokenField = DeviceTokenRegisterRequest.class.getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(request, token);

            var deviceModelField = DeviceTokenRegisterRequest.class.getDeclaredField("deviceModel");
            deviceModelField.setAccessible(true);
            deviceModelField.set(request, deviceModel);

            var osVersionField = DeviceTokenRegisterRequest.class.getDeclaredField("osVersion");
            osVersionField.setAccessible(true);
            osVersionField.set(request, osVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
