package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomUuid(String roomUuid);

    @Query("SELECT r FROM Room r WHERE r.host.id = :hostId AND r.status = :status")
    Page<Room> findByHostIdAndStatus(@Param("hostId") Long hostId,
                                      @Param("status") RoomStatus status,
                                      Pageable pageable);

    Page<Room> findByHostId(Long hostId, Pageable pageable);

    boolean existsByRoomUuid(String roomUuid);

    @Query("SELECT r FROM Room r JOIN FETCH r.host WHERE r.team.id = :teamId AND r.status = :status")
    Page<Room> findByTeamIdAndStatus(@Param("teamId") Long teamId,
                                      @Param("status") RoomStatus status,
                                      Pageable pageable);

    List<Room> findAllByTeamId(Long teamId);

    void deleteAllByTeamId(Long teamId);
}
