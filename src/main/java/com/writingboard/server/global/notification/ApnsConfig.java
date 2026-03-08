package com.writingboard.server.global.notification;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
@EnableConfigurationProperties(ApnsProperties.class)
@RequiredArgsConstructor
public class ApnsConfig {

    private final ApnsProperties apnsProperties;

    @Bean
    public ApnsClient apnsClient() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        ApnsSigningKey signingKey = ApnsSigningKey.loadFromPkcs8File(
            new File(apnsProperties.getKeyPath()),
            apnsProperties.getTeamId(),
            apnsProperties.getKeyId()
        );

        ApnsClient client = new ApnsClientBuilder()
            .setApnsServer(apnsProperties.isProduction()
                ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
            .setSigningKey(signingKey)
            .build();

        log.info("APNs client initialized - environment: {}, topic: {}",
            apnsProperties.isProduction() ? "PRODUCTION" : "DEVELOPMENT",
            apnsProperties.getTopic());

        return client;
    }
}
