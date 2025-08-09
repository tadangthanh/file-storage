package vn.thanh.storageservice.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blob-events")
public class BlobEventController {

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
// parts[0] = "" (do bắt đầu bằng /)
// parts[1] = luu-tru-tai-lieu
// parts[2] = 4  (metadataId)
// parts[3] = 1  (versionId)
// parts[4] = queryElasticsearch.txt
                System.out.println("metadata id"+parts[2]);
                System.out.println("versionId "+parts[3]);
                System.out.println("name: "+parts[4]);
                // TODO: Xử lý logic khi file được upload xong
            }
        }
        return ResponseEntity.ok().build();
    }
}