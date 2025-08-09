package vn.thanh.storageservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.thanh.storageservice.interceptor.OAuth2FeignConfig;

@FeignClient(name = "metadata-service", configuration = OAuth2FeignConfig.class)
public interface MetadataService {
    @GetMapping("/api/v1/files/{metadataId}")
    @CircuitBreaker(name = "metadataService", fallbackMethod = "fallback")
    void getFileById(@PathVariable Long metadataId);

    default void fallback(Long metadataId,Throwable throwable) {
        // Fallback logic can be implemented here, e.g., logging the error or sending a default response
        System.out.println("------------có lỗi khi get File by ID: "+metadataId);
    }
}
