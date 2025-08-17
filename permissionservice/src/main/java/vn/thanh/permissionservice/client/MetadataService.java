package vn.thanh.permissionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.thanh.permissionservice.dto.ResponseData;
import vn.thanh.permissionservice.exception.ResourceNotFoundException;
import vn.thanh.permissionservice.interceptor.FeignConfig;

import java.util.UUID;

@FeignClient(name = "metadata-service", configuration = FeignConfig.class)

public interface MetadataService {
    @GetMapping("/api/v1/files/{userId}/is-owner/{docId}")
    @CircuitBreaker(name = "metadataService", fallbackMethod = "userIsOwnerDocumentFallBack")
    ResponseData<Boolean> userIsOwnerDocument(@PathVariable UUID userId, @PathVariable Long docId);

    @GetMapping("/api/v1/categories/{userId}/is-owner/{categoryId}")
    @CircuitBreaker(name = "metadataService", fallbackMethod = "userIsOwnerCategoryFallBack")
    ResponseData<Boolean> userIsOwnerCategory(@PathVariable UUID userId, @PathVariable Long categoryId);


    public default ResponseData<Boolean> userIsOwnerDocumentFallBack(UUID userId, Long docId, Throwable t) {
        // log hoặc xử lý degrade
        return new ResponseData<>(200, "Fallback triggered", false);
    }
    public default ResponseData<Boolean> userIsOwnerCategoryFallBack(UUID userId, Long categoryId, Throwable t) {
        // log hoặc xử lý degrade
        return new ResponseData<>(200, "Fallback triggered", false);
    }
}
