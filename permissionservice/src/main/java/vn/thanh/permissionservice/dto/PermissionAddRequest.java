package vn.thanh.permissionservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PermissionAddRequest {
    @NotNull(message = "user id is required")
    private UUID userId;
    @Min(value = 1, message = "permission bit must be greater than 0")
    private int permissionBit;
}
