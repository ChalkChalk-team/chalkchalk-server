package com.writingboard.server.domain.chat.repository;

import com.writingboard.server.domain.chat.document.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * 커서 기반 페이징: 특정 시점 이전 메시지 조회 (무한 스크롤)
     */
    @Query("{ 'roomUuid': ?0, 'timestamp': { $lt: ?1 } }")
    List<ChatMessage> findByRoomUuidAndTimestampBefore(
            String roomUuid,
            Instant before,
            Pageable pageable
    );

    /**
     * 최신 N개 메시지 조회 (초기 로드)
     */
    List<ChatMessage> findByRoomUuidOrderByTimestampDesc(
            String roomUuid,
            Pageable pageable
    );

    /**
     * 특정 시점 이후 메시지 조회 (재연결 시 동기화)
     */
    @Query("{ 'roomUuid': ?0, 'timestamp': { $gt: ?1 } }")
    List<ChatMessage> findByRoomUuidAndTimestampAfter(
            String roomUuid,
            Instant after
    );
}
