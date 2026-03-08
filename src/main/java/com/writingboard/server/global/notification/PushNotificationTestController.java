package com.writingboard.server.global.notification;

import com.writingboard.server.global.notification.dto.PushNotificationTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
@Tag(name = "Push Notification (Dev Only)", description = "개발 환경 전용 푸시 알림 테스트 API")
@Profile("dev")  // DEV 환경에서만 활성화
public class PushNotificationTestController {

    private final ApnsPushNotificationService pushNotificationService;

    @PostMapping("/test")
    @Operation(
        summary = "푸시 알림 테스트 전송",
        description = "APNs로 테스트 푸시 알림을 전송합니다. (개발 환경 전용)"
    )
    public ResponseEntity<String> sendTestNotification(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid PushNotificationTestRequest request) {

        log.info("Member {} requested test push notification", memberId);

        pushNotificationService.sendNotificationSync(
            request.deviceToken(),
            request.title(),
            request.body()
        );

        return ResponseEntity.ok("푸시 알림이 성공적으로 전송되었습니다");
    }
}
