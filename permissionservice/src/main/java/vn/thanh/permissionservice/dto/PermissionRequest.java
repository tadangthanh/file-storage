package vn.thanh.permissionservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.thanh.permissionservice.entity.ResourceType;

import java.util.UUID;

@Getter
@Setter
public class PermissionRequest {
    @NotNull(message = "user id is required")
    private UUID userId;
    @NotNull(message = "resource id is required")
    private Long resourceId;
    @NotNull(message = "resource type id is required")
    private ResourceType resourceType;
    @Min(1)
    private int permissionBit;
}
