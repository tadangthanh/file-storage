package vn.thanh.storageservice.sheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.thanh.storageservice.service.impl.VersionCleanupService;

@Component
@RequiredArgsConstructor
public class VersionCleanupScheduler {

    private final VersionCleanupService cleanupService;

    @Scheduled(fixedRate = 60_000) // 1 phút chạy 1 lần
    public void run() {
        cleanupService.sendOldUploadingMetadataIds();
    }
}
