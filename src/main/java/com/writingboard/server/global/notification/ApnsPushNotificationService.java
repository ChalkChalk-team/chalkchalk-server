package com.writingboard.server.global.notification;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApnsPushNotificationService {

    private final ApnsClient apnsClient;
    private final ApnsProperties apnsProperties;

    /**
     * 동기 방식 푸시 알림 전송 (테스트용)
     */
    public void sendNotificationSync(String deviceToken, String title, String body) {
        log.info("Sending push notification to token: {}", maskToken(deviceToken));

        String payload = buildAlertPayload(title, body);
        SimpleApnsPushNotification notification = new SimpleApnsPushNotification(
            deviceToken,
            apnsProperties.getTopic(),
            payload
        );

        PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>>
            future = apnsClient.sendNotification(notification);

        try {
            PushNotificationResponse<SimpleApnsPushNotification> response = future.get();

            if (response.isAccepted()) {
                log.info("Push notification accepted by APNs - token: {}", maskToken(deviceToken));
            } else {
                String rejectionReason = response.getRejectionReason().orElse("Unknown");
                Instant invalidationTime = response.getTokenInvalidationTimestamp().orElse(null);

                log.error("Push notification rejected by APNs - token: {}, reason: {}, invalidationTime: {}",
                    maskToken(deviceToken), rejectionReason, invalidationTime);

                // TODO: Mark token as inactive in DeviceTokenService if rejection reason is Unregistered/BadDeviceToken

                throw new RuntimeException("APNs rejected notification: " + rejectionReason);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Push notification interrupted - token: {}", maskToken(deviceToken), e);
            throw new RuntimeException("Push notification interrupted", e);
        } catch (ExecutionException e) {
            log.error("Push notification execution failed - token: {}", maskToken(deviceToken), e);
            throw new RuntimeException("Push notification execution failed", e);
        }
    }

    private String buildAlertPayload(String title, String body) {
        SimpleApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        payloadBuilder.setAlertTitle(title);
        payloadBuilder.setAlertBody(body);
        payloadBuilder.setSound("default");
        return payloadBuilder.build();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
