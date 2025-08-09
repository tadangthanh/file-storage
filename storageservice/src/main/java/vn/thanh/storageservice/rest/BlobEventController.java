package vn.thanh.storageservice.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blob-events")
public class BlobEventController {

    @PostMapping
    public ResponseEntity<Map<String, String>> handleEvents(@RequestBody List<Map<String, Object>> events) {
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
                // TODO: Xử lý logic khi file được upload xong
            }
        }
        return ResponseEntity.ok().build();
    }
}