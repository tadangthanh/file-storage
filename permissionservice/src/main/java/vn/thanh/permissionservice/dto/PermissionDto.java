package vn.thanh.permissionservice.dto;

import lombok.Getter;
import lombok.Setter;
import vn.thanh.permissionservice.entity.ResourceType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PermissionDto extends BaseDto {
    private ResourceType resourceType;
    private Long resourceId;
    private UUID userId;
    private List<String> permissions;
}
