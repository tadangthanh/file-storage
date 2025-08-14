package vn.thanh.metadataservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.metadataservice.entity.File;
import vn.thanh.metadataservice.entity.Status;
import vn.thanh.metadataservice.repository.FileRepo;
import vn.thanh.metadataservice.service.IOutboxService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileScheduler {

    private static final int MAX_RETRY = 4; // 4 lần: 5m, 15m, 1h, 1d
    private static final long[] BACKOFF_MINUTES = {5, 15, 60, 1440}; // phút

    private final FileRepo fileRepo;
    private final IOutboxService outboxService;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailedDeletions() {
        for (int retryIndex = 0; retryIndex < MAX_RETRY; retryIndex++) {
            long waitMinutes = BACKOFF_MINUTES[retryIndex];

            List<File> readyList = fileRepo.findReadyForRetry(
                    Status.DELETE_FAILED,
                    waitMinutes,
                    PageRequest.of(0, 50) // batch size
            );

            if (readyList.isEmpty()) {
                continue;
            }

            List<Long> metadataIdsToRetry = new ArrayList<>();
            for (File file : readyList) {
                if (file.getRetryCount() >= MAX_RETRY) {
                    file.setStatus(Status.MANUAL_INTERVENTION);
                } else {
                    metadataIdsToRetry.add(file.getId());
                    file.setRetryCount(file.getRetryCount() + 1);
                    file.setLastRetryTime(LocalDateTime.now());
                }
            }
            fileRepo.saveAll(readyList);

            if (!metadataIdsToRetry.isEmpty()) {
                log.info("Retry deletion for {} metadata IDs", metadataIdsToRetry.size());
                outboxService.addDeleteMetadataEvent(metadataIdsToRetry);
            }
        }
    }

}
