package vn.thanh.permissionservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.Perms;
import vn.thanh.permissionservice.exception.ResourceNotFoundException;
import vn.thanh.permissionservice.mapper.PermissionMapper;
import vn.thanh.permissionservice.repository.PermissionRepo;
import vn.thanh.permissionservice.service.IPermissionService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PermissionServiceImpl implements IPermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepo permissionRepo;

    @Override
    public PermissionDto assignPermission(PermissionRequest req) {
        log.info("assign permission: {}, resource id: {}, user id: {}", Perms.toList(req.getPermissionBit()),
                req.getResourceId(), req.getUserId());
        Permission permission = permissionRepo
                .findByUserIdAndResourceTypeAndResourceId(req.getUserId(), req.getResourceType(), req.getResourceId())
                .orElseGet(() -> permissionMapper.toEntity(req));

        permission.add(req.getPermissionBit());
        permission = permissionRepo.save(permission);

        return toDto(permission);
    }

    /***
     * add new permission for permission existed
     * @param permissionId: permission existed to add
     * @param permissionRequest: new permission to add
     * @return: permission added
     */
    @Override
    public PermissionDto addPermission(Long permissionId, PermissionAddRequest permissionRequest) {
        log.info("req add permission: {}, user id: {} to permission id:{}", Perms.toList(permissionRequest.getPermissionBit()),
               permissionRequest.getUserId(), permissionId);
        Permission permissionExists = getPermissionById(permissionId);
        permissionExists.add(permissionRequest.getPermissionBit());
        permissionExists = permissionRepo.save(permissionExists);
        return toDto(permissionExists);
    }

    @Override
    public boolean hasPermission(PermissionRequest req) {
        log.info("user has permission? : {}, resource id: {}, user id: {}", Perms.toList(req.getPermissionBit()),
                req.getResourceId(), req.getUserId());
        return permissionRepo
                .findByUserIdAndResourceTypeAndResourceId(req.getUserId(), req.getResourceType(), req.getResourceId())
                .map(p -> (p.getAllowMask() & req.getPermissionBit()) != 0)
                .orElse(false);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionRepo.findById(id).orElseThrow(() -> {
            log.info("permission id: {} not found", id);
            return new ResourceNotFoundException("không tìm thấy quyền này");
        });
    }

    @Override
    public PermissionDto toDto(Permission permission) {
        PermissionDto permissionDto = permissionMapper.toDto(permission);
        permissionDto.setPermissions(Perms.toList(permission.getAllowMask()));
        return permissionDto;
    }
}
