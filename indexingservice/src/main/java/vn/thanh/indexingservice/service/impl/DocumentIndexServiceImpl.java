package vn.thanh.indexingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.thanh.indexingservice.dto.DocumentIndexMessage;
import vn.thanh.indexingservice.entity.DocumentIndex;
import vn.thanh.indexingservice.repository.DocumentIndexRepository;
import vn.thanh.indexingservice.service.IDocumentIndexService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIndexServiceImpl implements IDocumentIndexService {
    private final DocumentIndexRepository repository;

    @KafkaListener(topics = "${app.kafka.document-extracted-topic}", groupId = "${app.kafka.indexing-group}")
    public void listen(DocumentIndexMessage message) {
        log.info("Received file for indexing: documentId={}, blobName={}",
                message.getDocumentId(), message.getBlobName());

        try {
            // Convert DTO -> entity
            DocumentIndex entity = new DocumentIndex();
            entity.setId(UUID.randomUUID().toString());
            entity.setDocumentId(message.getDocumentId());
            entity.setVersion(message.getVersion());
            entity.setChunkIndex(message.getChunkIndex());
            entity.setText(message.getText());
            entity.setOwnerId(message.getOwnerId());
            entity.setVisibility(message.getVisibility());
            entity.setCategoryId(message.getCategoryId());
            entity.setAllowedUserIds(message.getAllowedUserIds());
            entity.setAllowedGroupIds(message.getAllowedGroupIds());
            entity.setCreatedAt(message.getCreatedAt());

            // Insert vào Elasticsearch
            repository.save(entity);

            log.info("Indexed documentId={} successfully", message.getDocumentId());

        } catch (Exception e) {
            log.error("Failed to index documentId={} due to {}", message.getDocumentId(), e.getMessage(), e);
        }
    }
}
