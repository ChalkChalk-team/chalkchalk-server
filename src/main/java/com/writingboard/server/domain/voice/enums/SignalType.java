package com.writingboard.server.domain.voice.enums;

public enum SignalType {
    OFFER,           // SDP offer from caller
    ANSWER,          // SDP answer from callee
    ICE_CANDIDATE,   // ICE candidate for NAT traversal
    JOIN,            // User joins voice channel
    LEAVE            // User leaves voice channel
}
