package vn.thanh.storageservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.entity.VersionStatus;
import vn.thanh.storageservice.repository.VersionRepo;
import vn.thanh.storageservice.service.IOutboxService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

// Service xử lý
@Service
@RequiredArgsConstructor
public class VersionCleanupService {
    private final VersionRepo versionRepo;
    private final IOutboxService outboxService;

    @Transactional
    public void sendOldUploadingMetadataIds() {
        Date cutoffTime = Date.from(Instant.now().minus(1, ChronoUnit.SECONDS));

        List<Version> oldUploadingVersions = versionRepo.findUploadingVersionsBefore(VersionStatus.UPLOADING, cutoffTime);

        List<Long> metadataIds = oldUploadingVersions.stream().map(Version::getMetadataId).distinct().toList();
        versionRepo.deleteAll(oldUploadingVersions);
        if (!metadataIds.isEmpty()) {
            outboxService.addMetadataCleanUpEvent(metadataIds);
        }
    }
}
