package vn.thanh.textextractionservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thanh.textextractionservice.dto.DocumentIndexMessage;
import vn.thanh.textextractionservice.entity.OutboxEvent;
import vn.thanh.textextractionservice.entity.OutboxEventStatus;
import vn.thanh.textextractionservice.exception.JsonSerializeException;
import vn.thanh.textextractionservice.repository.OutboxEventRepository;
import vn.thanh.textextractionservice.service.IOutboxService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements IOutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.document-extracted-topic}")
    private String documentExtractedTopic;

    @Transactional
    @Override
    public void addEventTextExtracted(DocumentIndexMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(documentExtractedTopic)
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
