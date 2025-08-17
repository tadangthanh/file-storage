package vn.thanh.permissionservice.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import vn.thanh.permissionservice.client.MetadataService;
import vn.thanh.permissionservice.dto.PermissionAddRequest;
import vn.thanh.permissionservice.dto.PermissionDto;
import vn.thanh.permissionservice.dto.PermissionRequest;
import vn.thanh.permissionservice.dto.ResponseData;
import vn.thanh.permissionservice.entity.Permission;
import vn.thanh.permissionservice.entity.ResourceType;
import vn.thanh.permissionservice.exception.AccessDeniedException;
import vn.thanh.permissionservice.exception.ResourceNotFoundException;
import vn.thanh.permissionservice.mapper.PermissionMapper;
import vn.thanh.permissionservice.repository.PermissionRepo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class PermissionServiceImplTest {
    @Mock
    PermissionMapper permissionMapper;
    @Mock
    PermissionRepo permissionRepo;
    @Mock
    DocumentCategoryMapServiceImpl documentCategoryMapService;
    @Mock
    MetadataService metadataService;
    @InjectMocks
    private PermissionServiceImpl permissionService;


    @Test
    void assignPermission_owner_shouldSaveAndReturnDto() {
        PermissionRequest request = new PermissionRequest();
        UUID userId = UUID.randomUUID();
        request.setPermissionBit(1);
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

        // fake SecurityContext với JWT
        Jwt jwt = Jwt.withTokenValue("token")
                .subject(userId.toString())
                .header("alg", "none")
                .claim("scope", "USER")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // mock metadataService trả về true (là owner)
        given(metadataService.userIsOwnerCategory(userId, request.getResourceId()))
                .willReturn(new ResponseData<>(200, "Thanh cong", true));

        // mock mapper + repo
        given(permissionMapper.toEntity(request)).willReturn(permissionEntity);
        given(permissionRepo.findByUserIdAndResourceTypeAndResourceId(userId, ResourceType.CATEGORY, 1L))
                .willReturn(Optional.empty());
        given(permissionRepo.save(permissionEntity)).willReturn(permissionEntity);
        given(permissionMapper.toDto(permissionEntity)).willReturn(dtoMock);

        // when
        PermissionDto result = permissionService.assignPermission(request);

        // then
        assertThat(result.getId()).isEqualTo(dtoMock.getId());
        assertThat(result.getPermissions()).containsExactly("READ");
    }

    @Test
    void assignPermission_notOwner_shouldThrowAccessDenied() {
        PermissionRequest request = new PermissionRequest();
        UUID userId = UUID.randomUUID();
        request.setPermissionBit(1);
        request.setResourceId(1L);
        request.setUserId(userId);
        request.setResourceType(ResourceType.CATEGORY);

        // fake SecurityContext với JWT
        Jwt jwt = Jwt.withTokenValue("token")
                .subject(userId.toString()) // subject chính là userId
                .header("alg", "none")
                .claim("scope", "USER")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // mock metadataService trả về false (không phải owner)
        given(metadataService.userIsOwnerCategory(userId, request.getResourceId()))
                .willReturn(new ResponseData<>(200, "Thanh cong", false));

        // when + then
        assertThatThrownBy(() -> permissionService.assignPermission(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Bạn không có quyền với tài nguyên này");
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
