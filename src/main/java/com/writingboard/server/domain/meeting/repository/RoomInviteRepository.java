package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.RoomInvite;
import com.writingboard.server.domain.meeting.entity.enums.InviteStatus;
import com.writingboard.server.domain.meeting.entity.enums.InviteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomInviteRepository extends JpaRepository<RoomInvite, Long> {

    Optional<RoomInvite> findByInviteToken(String inviteToken);

    Optional<RoomInvite> findByRoomIdAndTypeAndStatus(Long roomId, InviteType type, InviteStatus status);

    List<RoomInvite> findByRoomId(Long roomId);

    boolean existsByInviteToken(String inviteToken);
}
