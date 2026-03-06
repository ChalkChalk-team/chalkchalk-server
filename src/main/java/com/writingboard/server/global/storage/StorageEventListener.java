package com.writingboard.server.global.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageEventListener {

    private final StorageService storageService;

    @Async("storageTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStorageDeleteEvent(StorageDeleteEvent event) {
        try {
            storageService.deleteObject(event.storageKey());
        } catch (Exception e) {
            log.warn("Failed to delete old R2 object: {}", event.storageKey(), e);
        }
    }
}
