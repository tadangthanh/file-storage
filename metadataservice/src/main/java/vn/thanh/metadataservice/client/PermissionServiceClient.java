package vn.thanh.metadataservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.thanh.metadataservice.dto.ResourceType;
import vn.thanh.metadataservice.dto.ResponseData;
import vn.thanh.metadataservice.interceptor.FeignConfig;

import java.util.UUID;

@FeignClient(name = "permission-service",configuration = FeignConfig.class)
public interface PermissionServiceClient {

    @GetMapping("/api/v1/permissions")
//    @CircuitBreaker(name = "permissionService", fallbackMethod = "userHasPermissionFallback")
    ResponseData<Boolean> hasPermission(@RequestParam("userId") UUID userId,
                                        @RequestParam("resourceId") Long resourceId,
                                        @RequestParam("resourceType") ResourceType resourceType,
                                        @RequestParam("permissionBit") int permissionBit);

    default ResponseData<Boolean> userHasPermissionFallback(UUID userId,
                                                            Long resourceId,
                                                            ResourceType resourceType,
                                                            int permissionBit,
                                                            Throwable t) {
        // log fallback
        return new ResponseData<>(200, "Fallback triggered", false);
    }
}
