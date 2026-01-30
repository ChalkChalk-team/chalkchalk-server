package com.writingboard.server.domain.team.repository;

import com.writingboard.server.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByIdAndCreatedById(Long teamId, Long memberId);

    boolean existsByName(String name);
}
