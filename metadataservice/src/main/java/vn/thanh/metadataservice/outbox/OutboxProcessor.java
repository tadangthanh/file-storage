package vn.thanh.metadataservice.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.thanh.metadataservice.entity.OutboxEvent;
import vn.thanh.metadataservice.entity.OutboxEventStatus;
import vn.thanh.metadataservice.repository.OutboxEventRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // 5 giay
    @Transactional
    // sau mỗi 5 giây là lấy event dưới csdl để gửi lên kafka
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findEventsToSend(5, 50);
        for (OutboxEvent event : events) {
            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayload())
                    .whenComplete((result, ex) -> {
                        event.setLastAttemptAt(LocalDateTime.now());
                        if (ex == null) {
                            event.setStatus(OutboxEventStatus.SUCCESS);
                            log.info("Gửi thành công event {} tới Kafka", event.getId());
                        } else {
                            event.setRetryCount(event.getRetryCount() + 1);
                            event.setStatus(OutboxEventStatus.FAILED);
                            log.error("❌ Gửi Kafka thất bại cho event {}: {}", event.getId(), ex.getMessage());
                        }
                        outboxEventRepository.save(event);
                    });
        }
    }

    // Chạy mỗi ngày lúc 2h sáng
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    // hàm dọn dẹp các event đã xử lý thành công sau 7 ngày
    public void cleanOldEvents() {
        // giữ 7 ngày
        Date threshold = Date.from(
                LocalDate.now().minusDays(7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        int deleted = outboxEventRepository.deleteOldSuccessEvents(threshold);
        if (deleted > 0) {
            log.info("Dọn dẹp {} outbox events thành công (trước ngày {})", deleted, threshold);
        }
    }
}
