package com.writingboard.server.global.websocket;

import java.security.Principal;

public class StompPrincipal implements Principal {

    private final Long memberId;

    public StompPrincipal(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }

    public Long getMemberId() {
        return memberId;
    }
}
