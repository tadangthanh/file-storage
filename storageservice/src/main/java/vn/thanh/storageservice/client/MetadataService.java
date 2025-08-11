package vn.thanh.storageservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vn.thanh.storageservice.exception.ResourceNotFoundException;
import vn.thanh.storageservice.interceptor.FeignConfig;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "metadata-service", configuration = FeignConfig.class)
public interface MetadataService {
    @GetMapping("/api/v1/files/{metadataId}")
    @CircuitBreaker(name = "metadataService", fallbackMethod = "fallback")
    void getFileById(@PathVariable Long metadataId);

    @PostMapping("/api/v1/files/is-owner-all-file/{userId}")
    @CircuitBreaker(name = "metadataService", fallbackMethod = "isOwnerAllFallback")
    void isOwnerAll(@PathVariable UUID userId, @RequestBody List<Long> fileIds);

    default void fallback(Long metadataId, Throwable throwable) {
        // Fallback logic can be implemented here, e.g., logging the error or sending a default response
        System.out.println("------------có lỗi khi get File by ID: " + metadataId);
    }
    default void isOwnerAllFallback(UUID userId, List<Long> fileIds, Throwable throwable) {
        System.out.println("------------có lỗi khi kiểm tra quyền sở hữu files của user " + userId);
        if (throwable instanceof ResourceNotFoundException) {
            throw (ResourceNotFoundException) throwable;
        }
        if (throwable instanceof AccessDeniedException) {
            throw (AccessDeniedException) throwable;
        }
        throw new RuntimeException(throwable);
    }
}
