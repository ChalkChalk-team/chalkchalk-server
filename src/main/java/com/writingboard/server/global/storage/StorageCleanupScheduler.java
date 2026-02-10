package com.writingboard.server.global.storage;

import com.writingboard.server.domain.personal.entity.PersonalAsset;
import com.writingboard.server.domain.personal.repository.PersonalAssetRepository;
import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetStatus;
import com.writingboard.server.domain.team.repository.TeamAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageCleanupScheduler {

    private static final int RETENTION_DAYS = 30;

    private final TeamAssetRepository teamAssetRepository;
    private final PersonalAssetRepository personalAssetRepository;
    private final StorageService storageService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredAssets() {
        Instant cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS);
        log.info("Starting R2 storage cleanup for assets deleted before {}", cutoff);

        int teamCleaned = cleanupTeamAssets(cutoff);
        int personalCleaned = cleanupPersonalAssets(cutoff);

        log.info("R2 storage cleanup completed: {} team assets, {} personal assets cleaned", teamCleaned, personalCleaned);
    }

    private int cleanupTeamAssets(Instant cutoff) {
        List<TeamAsset> expiredAssets = teamAssetRepository
                .findByStatusAndDeletedAtBeforeAndStorageKeyIsNotNull(AssetStatus.DELETED, cutoff);

        int count = 0;
        for (TeamAsset asset : expiredAssets) {
            try {
                storageService.deleteObject(asset.getStorageKey());
                asset.clearStorageKey();
                count++;
            } catch (Exception e) {
                log.warn("Failed to delete R2 object for team asset {}: {}", asset.getId(), asset.getStorageKey(), e);
            }
        }
        return count;
    }

    private int cleanupPersonalAssets(Instant cutoff) {
        List<PersonalAsset> expiredAssets = personalAssetRepository
                .findByDeletedAtBeforeAndStorageKeyIsNotNull(cutoff);

        int count = 0;
        for (PersonalAsset asset : expiredAssets) {
            try {
                storageService.deleteObject(asset.getStorageKey());
                asset.clearStorageKey();
                count++;
            } catch (Exception e) {
                log.warn("Failed to delete R2 object for personal asset {}: {}", asset.getId(), asset.getStorageKey(), e);
            }
        }
        return count;
    }
}
