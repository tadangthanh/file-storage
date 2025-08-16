package vn.thanh.permissionservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.ResourceType;
import vn.thanh.permissionservice.exception.ResourceNotFoundException;
import vn.thanh.permissionservice.mapper.PermissionMapper;
import vn.thanh.permissionservice.repository.PermissionRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class PermissionServiceImplTest {
    @Mock
    PermissionMapper permissionMapper;
    @Mock
    PermissionRepo permissionRepo;
    @Mock
    DocumentCategoryMapServiceImpl documentCategoryMapService;
    @InjectMocks
    private PermissionServiceImpl permissionService;


    @Test
    void assignPermission() {
        PermissionRequest request = new PermissionRequest();
        UUID userId = UUID.randomUUID();
        request.setPermissionBit(1); // READ
        request.setResourceId(1L);
        request.setUserId(userId);
        request.setResourceType(ResourceType.CATEGORY);

        // fake entity
        Permission permissionEntity = new Permission();
        permissionEntity.setId(100L);
        permissionEntity.setUserId(userId);
        permissionEntity.setResourceId(1L);
        permissionEntity.setResourceType(ResourceType.CATEGORY);
        permissionEntity.setAllowMask(1);

        // fake dto
        PermissionDto dtoMock = new PermissionDto();
        dtoMock.setId(100L);
        dtoMock.setUserId(userId);
        dtoMock.setResourceId(1L);
        dtoMock.setResourceType(ResourceType.CATEGORY);
        dtoMock.setPermissions(List.of("READ"));

        // mock mapper + repo
        given(permissionMapper.toEntity(request)).willReturn(permissionEntity);
        given(permissionRepo.findByUserIdAndResourceTypeAndResourceId(userId, ResourceType.CATEGORY, 1L))
                .willReturn(Optional.empty()); // chưa có thì tạo mới
        given(permissionRepo.save(permissionEntity)).willReturn(permissionEntity);
        given(permissionMapper.toDto(permissionEntity)).willReturn(dtoMock);

        // act
        PermissionDto dto = permissionService.assignPermission(request);

        // assert
        Assertions.assertEquals(request.getResourceId(), dto.getResourceId());
        Assertions.assertEquals(request.getUserId(), dto.getUserId());
        Assertions.assertEquals(request.getResourceType(), dto.getResourceType());
        Assertions.assertIterableEquals(List.of("READ"), dto.getPermissions());
        Assertions.assertNotNull(dto.getId());
    }

    @Test
    void getPermissionById_notFound_shouldThrowException() {
        Long permissionId = 999L;

        // mock repo trả về empty
        given(permissionRepo.findById(permissionId)).willReturn(Optional.empty());

        // assert exception
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> permissionService.getPermissionById(permissionId));
    }

    @Test
    void assignPermission_alreadyExists_shouldUpdateMask() {
        UUID userId = UUID.randomUUID();
        PermissionRequest request = new PermissionRequest();
        request.setPermissionBit(1); // READ
        request.setResourceId(1L);
        request.setUserId(userId);
        request.setResourceType(ResourceType.CATEGORY);

        Permission existing = new Permission();
        existing.setId(1L);
        existing.setUserId(userId);
        existing.setResourceId(1L);
        existing.setResourceType(ResourceType.CATEGORY);
        existing.setAllowMask(0);

        PermissionDto dtoMock = new PermissionDto();
        dtoMock.setId(1L);
        dtoMock.setUserId(userId);
        dtoMock.setResourceId(1L);
        dtoMock.setResourceType(ResourceType.CATEGORY);
        dtoMock.setPermissions(List.of("READ"));

        // mock: đã tồn tại
        given(permissionRepo.findByUserIdAndResourceTypeAndResourceId(userId, ResourceType.CATEGORY, 1L))
                .willReturn(Optional.of(existing));
        given(permissionRepo.save(existing)).willReturn(existing);
        given(permissionMapper.toDto(existing)).willReturn(dtoMock);

        // act
        PermissionDto dto = permissionService.assignPermission(request);

        // assert
        Assertions.assertIterableEquals(List.of("READ"), dto.getPermissions());
    }

    @Test
    void addPermission_Happy_path() {
        Long permissionId = 10L;
        UUID userId = UUID.randomUUID();
        PermissionAddRequest request = new PermissionAddRequest();
        request.setPermissionBit(2); // WRITE
        request.setUserId(userId);

        Permission existing = new Permission();
        existing.setId(1L);
        existing.setUserId(userId);
        existing.setResourceId(1L);
        existing.setResourceType(ResourceType.CATEGORY);
        existing.setAllowMask(1);

        // fake dto
        PermissionDto dtoMock = new PermissionDto();
        dtoMock.setId(100L);
        dtoMock.setUserId(userId);
        dtoMock.setResourceId(1L);
        dtoMock.setResourceType(ResourceType.CATEGORY);
        dtoMock.setPermissions(List.of("READ", "WRITE"));

        given(permissionRepo.findById(permissionId)).willReturn(Optional.of(existing));
        given(permissionRepo.save(existing)).willReturn(existing);
        given(permissionMapper.toDto(existing)).willReturn(dtoMock);

        //act
        PermissionDto result = permissionService.addPermission(permissionId, request);


        Assertions.assertNotNull(dtoMock.getId());
        Assertions.assertIterableEquals(result.getPermissions(), List.of("READ", "WRITE"));
        Assertions.assertEquals(3, existing.getAllowMask());

    }

    @Test
    void hasPermission_READ() {
        UUID userId = UUID.randomUUID();
        PermissionRequest request = new PermissionRequest();
        request.setPermissionBit(1); // READ
        request.setResourceId(1L);
        request.setUserId(userId);
        request.setResourceType(ResourceType.DOCUMENT);

        Long categoryId = 5L;

        Permission existing = new Permission();
        existing.setId(1L);
        existing.setUserId(userId);
        existing.setResourceId(categoryId);
        existing.setResourceType(ResourceType.CATEGORY);
        existing.setAllowMask(1);


        given(permissionRepo.findByUserIdAndResourceTypeAndResourceId(userId, request.getResourceType(), request.getResourceId())).willReturn(Optional.empty());
        given(documentCategoryMapService.getCategoryByDocumentId(request.getResourceId())).willReturn(categoryId);
        given(permissionRepo.findByUserIdAndResourceTypeAndResourceId(userId, ResourceType.CATEGORY, categoryId)).willReturn(Optional.of(existing));
        Boolean hasPermission = permissionService.hasPermission(request);

        Assertions.assertEquals(true, hasPermission);

    }


}
