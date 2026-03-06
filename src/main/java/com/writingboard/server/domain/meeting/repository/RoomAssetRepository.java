package com.writingboard.server.domain.meeting.repository;

import com.writingboard.server.domain.meeting.entity.RoomAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomAssetRepository extends JpaRepository<RoomAsset, Long> {

    List<RoomAsset> findByRoomId(Long roomId);

    @Query("SELECT ra FROM RoomAsset ra WHERE ra.room.roomUuid = :roomUuid")
    List<RoomAsset> findByRoomUuid(@Param("roomUuid") String roomUuid);

    Optional<RoomAsset> findByIdAndRoomId(Long id, Long roomId);

    @Query("SELECT ra FROM RoomAsset ra " +
           "JOIN FETCH ra.teamAsset " +
           "JOIN FETCH ra.addedBy " +
           "WHERE ra.id = :id AND ra.room.roomUuid = :roomUuid")
    Optional<RoomAsset> findByIdAndRoomUuid(@Param("id") Long id, @Param("roomUuid") String roomUuid);

    @Modifying
    @Query("UPDATE RoomAsset ra SET ra.isActive = false WHERE ra.room.id = :roomId")
    void deactivateAllByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Query("UPDATE RoomAsset ra SET ra.isActive = false WHERE ra.room.roomUuid = :roomUuid")
    void deactivateAllByRoomUuid(@Param("roomUuid") String roomUuid);

    @Query("SELECT ra FROM RoomAsset ra WHERE ra.room.roomUuid = :roomUuid AND ra.isActive = true")
    Optional<RoomAsset> findActiveByRoomUuid(@Param("roomUuid") String roomUuid);

    Optional<RoomAsset> findFirstByTeamAssetIdOrderByCreatedAtDesc(Long teamAssetId);
}
