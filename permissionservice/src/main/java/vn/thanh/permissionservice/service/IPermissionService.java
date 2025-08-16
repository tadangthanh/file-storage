package vn.thanh.permissionservice.service;

import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.entity.Permission;

public interface IPermissionService {
    PermissionDto assignPermission(PermissionRequest permissionRequest);

    PermissionDto addPermission(Long permissionId, PermissionAddRequest permissionRequest);

    boolean hasPermission(PermissionRequest permissionRequest);

    Permission getPermissionById(Long id);

    PermissionDto toDto(Permission permission);
}
