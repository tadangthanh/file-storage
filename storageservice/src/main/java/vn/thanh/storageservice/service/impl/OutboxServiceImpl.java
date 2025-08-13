package vn.thanh.storageservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.dto.MetadataUpdate;
import vn.thanh.storageservice.entity.OutboxEvent;
import vn.thanh.storageservice.entity.OutboxEventStatus;
import vn.thanh.storageservice.exception.JsonSerializeException;
import vn.thanh.storageservice.repository.OutboxEventRepository;
import vn.thanh.storageservice.service.IOutboxService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements IOutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.metadata-update-topic}")
    private String metadataUpdateTopic;
    @Value("${app.kafka.metadata-cleanup-topic}")
    private String metadataCleanupTopic;
    @Value("${app.kafka.blob-delete-fail-topic}")
    private String blobDeleteFailTopic;
    @Value("${app.kafka.blob-delete-success-topic}")
    private String blobDeleteSuccessTopic;

    @Override
    @Transactional
    public void addUpdateMetadataEvent(MetadataUpdate update) {
        try {
            String payload = objectMapper.writeValueAsString(update);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(metadataUpdateTopic)
                    .payload(payload)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException("Lỗi serialize event");
        }
    }

    @Override
    public void addMetadataCleanUpEvent(List<Long> metadataIds) {
        try {
            String payload = objectMapper.writeValueAsString(metadataIds);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(metadataCleanupTopic)
                    .payload(payload)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException("Lỗi serialize event");
        }
    }

    @Override
    public void addBlobDeleteFailEvent(List<Long> metadataIds) {
        try {
            String payload = objectMapper.writeValueAsString(metadataIds);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(blobDeleteFailTopic)
                    .payload(payload)
                    .status(OutboxEventStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new JsonSerializeException("Lỗi serialize event");
        }
    }

    @Override
    public void addBlobDeleteSuccessEvent(List<Long> metadataIds) {
        try {
            String payload = objectMapper.writeValueAsString(metadataIds);
            OutboxEvent event = OutboxEvent.builder()
                    .topic(blobDeleteSuccessTopic)
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
