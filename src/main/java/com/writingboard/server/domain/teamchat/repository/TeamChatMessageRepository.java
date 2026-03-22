package com.writingboard.server.domain.teamchat.repository;

import com.writingboard.server.domain.teamchat.document.TeamChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TeamChatMessageRepository extends MongoRepository<TeamChatMessage, String> {

    Page<TeamChatMessage> findByTeamIdOrderByTimestampDesc(Long teamId, Pageable pageable);
}
