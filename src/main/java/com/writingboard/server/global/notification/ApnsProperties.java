package com.writingboard.server.global.notification;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "apns")
public class ApnsProperties {
    private String teamId;
    private String keyId;
    private String keyPath;  // File path to .p8 key
    private String topic;    // Bundle ID
    private boolean production = false;
}
