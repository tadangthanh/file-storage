package vn.thanh.storageservice.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.thanh.storageservice.dto.DocumentReady;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.service.IOutboxService;
import vn.thanh.storageservice.service.IVersionService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blob-events")
@RequiredArgsConstructor
@Slf4j
public class BlobEventController {
//    private final IKafkaService kafkaService;
    private final IVersionService versionService;
    private final IOutboxService outboxService;

    @PostMapping
    public ResponseEntity<Map<String, String>> handleEvents(@RequestBody List<Map<String, Object>> events) throws URISyntaxException {
        if (events != null && !events.isEmpty()) {
            Map<String, Object> firstEvent = events.get(0);
            String eventType = (String) firstEvent.get("eventType");

            // Bước 1: Nếu là sự kiện xác thực
            if ("Microsoft.EventGrid.SubscriptionValidationEvent".equals(eventType)) {
                Map<String, Object> data = (Map<String, Object>) firstEvent.get("data");
                String validationCode = (String) data.get("validationCode");

                Map<String, String> response = new HashMap<>();
                response.put("validationResponse", validationCode);
                return ResponseEntity.ok(response);
            }

            // Bước 2: Nếu là BlobCreated
            if ("Microsoft.Storage.BlobCreated".equals(eventType)) {
                Map<String, Object> data = (Map<String, Object>) firstEvent.get("data");
                System.out.println("Blob created: " + data);
                String url = data.get("url").toString();
                URI uri = new URI(url);
                String path = uri.getPath(); // /luu-tru-tai-lieu/4/1/queryElasticsearch.txt
                String[] parts = path.split("/");
                Long metadataId = Long.parseLong(parts[2]);
                Long currentVersionId = Long.parseLong(parts[3]);
                String originalFileName = parts[4].split("_")[1];
                String contentType = data.get("contentType").toString();
                Number contentLengthNum = (Number) data.get("contentLength");
                String blobName = String.join("/", Arrays.copyOfRange(parts, 2, parts.length));
                long size = contentLengthNum.longValue();

                DocumentReady documentReady = new DocumentReady();
                documentReady.setDocumentId(metadataId);
                documentReady.setName(originalFileName);
                documentReady.setType(contentType);
                documentReady.setBlobName(blobName);
                documentReady.setSize(size);
                documentReady.setCurrentVersionId(currentVersionId);
                log.info("Blob created event: metadata id: {}, version id: {}, originalFileName: {}, contentType: {}, size: {}, blobName: {}", parts[2], parts[3], parts[4], contentType, size, blobName);

                versionService.completeUpload(documentReady);
                // TODO: Xử lý logic khi file được upload xong
            }
        }
        return ResponseEntity.ok().build();
    }
}