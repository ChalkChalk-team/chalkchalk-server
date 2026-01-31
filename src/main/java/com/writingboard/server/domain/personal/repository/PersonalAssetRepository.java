package com.writingboard.server.domain.personal.repository;

import com.writingboard.server.domain.personal.entity.PersonalAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalAssetRepository extends JpaRepository<PersonalAsset, Long> {

    Page<PersonalAsset> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    Optional<PersonalAsset> findByIdAndDeletedAtIsNull(Long id);

    Optional<PersonalAsset> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);
}
