package vn.thanh.storageservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.dto.MetadataUpdate;
import vn.thanh.storageservice.entity.OutboxEventStatus;
import vn.thanh.storageservice.entity.OutboxEvent;
import vn.thanh.storageservice.exception.JsonSerializeException;
import vn.thanh.storageservice.repository.OutboxEventRepository;
import vn.thanh.storageservice.service.IOutboxService;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxServiceImpl implements IOutboxService {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void saveMetadataEvent(MetadataUpdate update) {
        try {
            String payload = objectMapper.writeValueAsString(update);

            OutboxEvent event = OutboxEvent.builder()
                    .topic("metadata")
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
