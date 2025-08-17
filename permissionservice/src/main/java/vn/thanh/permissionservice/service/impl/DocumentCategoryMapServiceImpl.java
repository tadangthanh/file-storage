package vn.thanh.permissionservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.permissionservice.dto.MetadataCreateMessage;
import vn.thanh.permissionservice.entity.DocumentCategoryMap;
import vn.thanh.permissionservice.entity.ResourceType;
import vn.thanh.permissionservice.repository.DocumentCategoryRepo;
import vn.thanh.permissionservice.repository.PermissionRepo;
import vn.thanh.permissionservice.service.IDocumentCategoryMapService;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DocumentCategoryMapServiceImpl implements IDocumentCategoryMapService {
    private final DocumentCategoryRepo documentCategoryRepo;
    private final PermissionRepo permissionRepo;

    @Override
    public void deleteAllByDocumentIds(List<Long> documentIds) {
        log.info("received: delete all document-category-map by metadata id: {}", documentIds.toString());
        documentCategoryRepo.deleteAllByDocumentIdIn(documentIds);
    }

    @Override
    public DocumentCategoryMap add(Long documentId, Long categoryId) {
        log.info("save document category map document ID: {},category ID:{}", documentId, categoryId);
        DocumentCategoryMap documentCategoryMap = new DocumentCategoryMap(documentId, categoryId);
        documentCategoryMap = documentCategoryRepo.save(documentCategoryMap);
        return documentCategoryMap;
    }

    @Override
    public Long getCategoryByDocumentId(Long documentId) {
        return documentCategoryRepo.findCategoryIdByDocumentId(documentId).orElse(null);
    }

    @RetryableTopic(
            attempts = "3", // Tổng số lần thử = 3 (lần gốc + 2 lần retry)
            backoff = @Backoff(delay = 1000, multiplier = 1.0),
            dltTopicSuffix = ".DLT", // Hậu tố DLT
            exclude = {NullPointerException.class, RuntimeException.class} // danh sach exclude ko retry ma day sang thang DLT
    )
    @KafkaListener(topics = "${app.kafka.metadata-create-topic}", groupId = "${app.kafka.permission-group}")
    public void listenMetadataCreate(MetadataCreateMessage message) {
        log.info("received kafka: metadata create category id: {}, document id: {}", message.getCategoryId(), message.getMetadataId());
        // kiểm tra xem category có permission hay ko, nếu có thì mới thêm document map category
        if (permissionRepo.existsByResourceIdAndResourceType(message.getCategoryId(), ResourceType.CATEGORY)) {
            this.add(message.getMetadataId(), message.getCategoryId());
        }
    }

    @RetryableTopic(
            attempts = "3", // Tổng số lần thử = 3 (lần gốc + 2 lần retry)
            backoff = @Backoff(delay = 1000, multiplier = 1.0),
            dltTopicSuffix = ".DLT", // Hậu tố DLT
            exclude = {NullPointerException.class, RuntimeException.class} // danh sach exclude ko retry ma day sang thang DLT
    )
    @KafkaListener(topics = "${app.kafka.category-delete-topic}", groupId = "${app.kafka.permission-group}")
    public void listenCategoryDelete(List<Long> categoryIds) {
        log.info("received kafka: category delete ids: {}", categoryIds.toString());
        documentCategoryRepo.deleteAllByCategoryIdIn(categoryIds);
    }

}
