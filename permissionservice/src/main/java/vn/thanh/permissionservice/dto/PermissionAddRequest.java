package vn.thanh.permissionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissionAddRequest {
    @NotNull(message = "user id is required")
    private UUID userId;
    private int permissionBit;
}
