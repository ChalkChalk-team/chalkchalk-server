package com.writingboard.server.domain.drawing.enums;

/**
 * 드로잉 메시지 타입
 * - ADD: 새로운 스트로크 추가
 * - REMOVE: 기존 스트로크 제거
 * - SNAPSHOT: 전체 드로잉 스냅샷
 */
public enum DrawingMessageType {
    ADD,
    REMOVE,
    SNAPSHOT
}
