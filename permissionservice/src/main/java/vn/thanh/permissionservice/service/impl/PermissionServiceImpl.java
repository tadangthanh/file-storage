package vn.thanh.permissionservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.Perms;
import vn.thanh.permissionservice.entity.ResourceType;
import vn.thanh.permissionservice.exception.ResourceNotFoundException;
import vn.thanh.permissionservice.mapper.PermissionMapper;
import vn.thanh.permissionservice.repository.PermissionRepo;
import vn.thanh.permissionservice.service.IDocumentCategoryMapService;
import vn.thanh.permissionservice.service.IPermissionService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PermissionServiceImpl implements IPermissionService {
    private final PermissionMapper permissionMapper;
    private final PermissionRepo permissionRepo;
    private final IDocumentCategoryMapService documentCategoryMapService;

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

    /***
     *
     * @param req: resource id, permission bit, user id, resource Type
     * @return
     */
    @Override
    public boolean hasPermission(PermissionRequest req) {
        log.info("user has permission? : {}, resource id: {}, user id: {}",
                Perms.toList(req.getPermissionBit()),
                req.getResourceId(),
                req.getUserId());

        if (req.getResourceType() == ResourceType.DOCUMENT) {
            // 1. Check trực tiếp document
            if (checkPermission(req.getUserId(), ResourceType.DOCUMENT, req.getResourceId(), req.getPermissionBit())) {
                return true;
            }

            // 2. Fallback sang category
            Long categoryId = documentCategoryMapService.getCategoryByDocumentId(req.getResourceId());
            return categoryId != null &&
                   checkPermission(req.getUserId(), ResourceType.CATEGORY, categoryId, req.getPermissionBit());
        }

        // 3. Các loại resource khác (CATEGORY, PROJECT,...)
        return checkPermission(req.getUserId(), req.getResourceType(), req.getResourceId(), req.getPermissionBit());
    }

    private boolean checkPermission(UUID userId, ResourceType type, Long resourceId, int permissionBit) {
        return permissionRepo
                .findByUserIdAndResourceTypeAndResourceId(userId, type, resourceId)
                .map(p -> (p.getAllowMask() & permissionBit) != 0)
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
    public void deletePermissionById(Long permissionId) {
        log.info("delete permission by id: {}", permissionId);
        permissionRepo.deleteById(permissionId);
    }

    @Override
    public PermissionDto toDto(Permission permission) {
        PermissionDto permissionDto = permissionMapper.toDto(permission);
        permissionDto.setPermissions(Perms.toList(permission.getAllowMask()));
        return permissionDto;
    }

    @RetryableTopic(
            attempts = "3", // Tổng số lần thử = 3 (lần gốc + 2 lần retry)
            backoff = @Backoff(delay = 1000, multiplier = 1.0),
            dltTopicSuffix = ".DLT", // Hậu tố DLT
            exclude = {NullPointerException.class, RuntimeException.class} // danh sach exclude ko retry ma day sang thang DLT
    )
    @KafkaListener(topics = "${app.kafka.metadata-delete-topic}", groupId = "${app.kafka.permission-group}")
    public void deleteAllByMetadataIds(List<Long> metadataIds) {
        log.info("received kafka: delete all by metadata ids: {}", metadataIds.toString());
        documentCategoryMapService.deleteAllByDocumentIds(metadataIds);
        permissionRepo.deleteAllByResourceIdInAndResourceType(metadataIds, ResourceType.DOCUMENT);
    }

}
