package com.writingboard.server.domain.teamchat.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.repository.TeamMemberRepository;
import com.writingboard.server.domain.teamchat.document.TeamChatMessage;
import com.writingboard.server.domain.teamchat.dto.request.TeamChatMessageRequest;
import com.writingboard.server.domain.teamchat.dto.response.TeamChatMessageResponse;
import com.writingboard.server.domain.teamchat.exception.TeamChatErrorCode;
import com.writingboard.server.domain.teamchat.exception.TeamChatException;
import com.writingboard.server.domain.teamchat.repository.TeamChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamChatService {

    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final TeamChatMessageRepository teamChatMessageRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final TeamChatRedisPublisher redisPublisher;

    public Page<TeamChatMessageResponse> getMessages(Long memberId, Long teamId, Pageable pageable) {
        validateTeamMember(teamId, memberId);

        Page<TeamChatMessage> page = teamChatMessageRepository.findByTeamIdOrderByTimestampDesc(teamId, pageable);
        return page.map(TeamChatMessageResponse::from);
    }

    public TeamChatMessageResponse sendMessage(Long memberId, Long teamId, TeamChatMessageRequest request) {
        validateTeamMember(teamId, memberId);
        validateContent(request.getContent());

        String sanitizedContent = HtmlUtils.htmlEscape(request.getContent());

        Member sender = memberRepository.findById(memberId)
                .orElseThrow(() -> new TeamChatException(TeamChatErrorCode.MEMBER_NOT_FOUND));

        TeamChatMessage message = TeamChatMessage.createText(teamId, sender, sanitizedContent);
        teamChatMessageRepository.save(message);

        TeamChatMessageResponse response = TeamChatMessageResponse.from(message);

        redisPublisher.publishNewMessage(teamId, response);

        log.debug("팀 채팅 메시지 전송 완료: teamId={}, senderId={}, messageId={}",
                teamId, memberId, message.getId());

        return response;
    }

    public void deleteMessage(Long memberId, Long teamId, String messageId) {
        validateTeamMember(teamId, memberId);

        TeamChatMessage message = teamChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new TeamChatException(TeamChatErrorCode.MESSAGE_NOT_FOUND));

        if (!message.getTeamId().equals(teamId)) {
            throw new TeamChatException(TeamChatErrorCode.TEAM_MISMATCH);
        }

        if (!message.getSenderId().equals(memberId)) {
            throw new TeamChatException(TeamChatErrorCode.NOT_MESSAGE_SENDER);
        }

        teamChatMessageRepository.delete(message);

        redisPublisher.publishDeleteMessage(teamId, messageId);

        log.debug("팀 채팅 메시지 삭제 완료: teamId={}, memberId={}, messageId={}",
                teamId, memberId, messageId);
    }

    private void validateTeamMember(Long teamId, Long memberId) {
        boolean isMember = teamMemberRepository.existsByTeamIdAndMemberIdAndStatus(
                teamId, memberId, TeamMemberStatus.ACTIVE);
        if (!isMember) {
            throw new TeamChatException(TeamChatErrorCode.NOT_TEAM_MEMBER);
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new TeamChatException(TeamChatErrorCode.EMPTY_MESSAGE);
        }
        if (content.length() > MAX_MESSAGE_LENGTH) {
            throw new TeamChatException(TeamChatErrorCode.MESSAGE_TOO_LONG);
        }
    }
}
