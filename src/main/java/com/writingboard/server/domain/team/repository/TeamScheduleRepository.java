package com.writingboard.server.domain.team.repository;

import com.writingboard.server.domain.team.entity.TeamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamScheduleRepository extends JpaRepository<TeamSchedule, Long> {

    Optional<TeamSchedule> findByIdAndTeamId(Long id, Long teamId);

    @Query("""
            SELECT s FROM TeamSchedule s
            WHERE s.team.id = :teamId
            AND (
                (s.recurrenceRule.frequency IS NULL AND s.startTime >= :from AND s.startTime <= :to)
                OR
                (s.recurrenceRule.frequency IS NOT NULL AND s.startTime <= :to
                    AND (s.recurrenceRule.endDate IS NULL OR s.recurrenceRule.endDate >= CAST(:fromDate AS LocalDate)))
            )
            ORDER BY s.startTime ASC
            """)
    List<TeamSchedule> findByTeamIdAndDateRange(
            @Param("teamId") Long teamId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("fromDate") OffsetDateTime fromDate);

    void deleteAllByTeamId(Long teamId);
}
