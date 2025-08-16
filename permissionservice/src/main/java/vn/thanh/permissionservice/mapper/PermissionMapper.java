package vn.thanh.permissionservice.mapper;

import org.mapstruct.Mapper;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.entity.Permission;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = IGNORE)
public interface PermissionMapper {
    Permission toEntity(PermissionRequest permissionRequest);

    PermissionDto toDto(Permission permission);
}
