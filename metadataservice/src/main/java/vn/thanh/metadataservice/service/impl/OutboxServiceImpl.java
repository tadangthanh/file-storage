package vn.thanh.metadataservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thanh.metadataservice.entity.OutboxEvent;
import vn.thanh.metadataservice.entity.OutboxEventStatus;
import vn.thanh.metadataservice.exception.JsonSerializeException;
import vn.thanh.metadataservice.repository.OutboxEventRepository;
import vn.thanh.metadataservice.service.IOutboxService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements IOutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.metadata-delete-topic}")
    private String metadataDeleteTopic;


    @Override
    @Transactional
    public void addDeleteMetadataEvent(List<Long> metadataIds) {
        log.info("add metadata delete event to outbox");
        try {
            String payload = objectMapper.writeValueAsString(metadataIds);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(metadataDeleteTopic)
                    .payload(payload)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException("Lỗi serialize event");
        }
    }
}
