package vn.thanh.storageservice.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.thanh.storageservice.entity.OutboxEventStatus;
import vn.thanh.storageservice.entity.OutboxEvent;
import vn.thanh.storageservice.repository.OutboxEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000) // 5 giay
    @Transactional
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
}
