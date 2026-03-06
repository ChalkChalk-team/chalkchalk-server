package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    Optional<RoomParticipant> findByRoomIdAndMemberId(Long roomId, Long memberId);

    List<RoomParticipant> findByRoomIdAndState(Long roomId, ParticipantState state);

    List<RoomParticipant> findByRoomId(Long roomId);

    @Query("SELECT rp FROM RoomParticipant rp " +
           "JOIN FETCH rp.room r " +
           "WHERE rp.member.id = :memberId " +
           "AND rp.state = :state " +
           "AND r.status = 'OPEN'")
    List<RoomParticipant> findMyActiveRooms(@Param("memberId") Long memberId,
                                             @Param("state") ParticipantState state);

    boolean existsByRoomIdAndMemberIdAndState(Long roomId, Long memberId, ParticipantState state);

    @Query("SELECT rp FROM RoomParticipant rp " +
           "JOIN FETCH rp.room r " +
           "JOIN FETCH rp.member m " +
           "WHERE r.roomUuid = :roomUuid " +
           "AND rp.member.id = :memberId")
    Optional<RoomParticipant> findWithRoomAndMemberByRoomUuidAndMemberId(
            @Param("roomUuid") String roomUuid,
            @Param("memberId") Long memberId);
}
