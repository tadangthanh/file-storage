package vn.thanh.storageservice.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.thanh.storageservice.client.MetadataService;
import vn.thanh.storageservice.dto.DeleteBlobsResult;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.entity.VersionStatus;
import vn.thanh.storageservice.exception.ResourceNotFoundException;
import vn.thanh.storageservice.mapper.VersionMapper;
import vn.thanh.storageservice.repository.VersionRepo;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.service.IOutboxService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceImplTest {

    @Mock
    private VersionRepo versionRepo;

    @Mock
    private IAzureStorageService azureStorageService;

    @Mock
    private MetadataService metadataService;

    @Mock
    private VersionMapper versionMapper;

    @Mock
    private IOutboxService outboxService;

    @InjectMocks
    private VersionServiceImpl versionService;

    @Test
    @DisplayName("completeUpload should update version status to AVAILABLE and save it")
    void completeUpload_HappyPath() {
        // Arrange
        Long versionId = 1L;
        VersionDto dto = new VersionDto();

        Version existingVersion = new Version();
        existingVersion.setStatus(VersionStatus.UPLOADING);

        given(versionRepo.findById(versionId)).willReturn(Optional.of(existingVersion));

        // Act
        versionService.completeUpload(versionId, dto);

        // Assert
        assertEquals(VersionStatus.AVAILABLE, existingVersion.getStatus());
        then(versionMapper).should().updateVersionIgnoreVNumber(existingVersion, dto);
        then(versionRepo).should().save(existingVersion);

        then(azureStorageService).shouldHaveNoInteractions();
        then(metadataService).shouldHaveNoInteractions();
        then(outboxService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("completeUpload should throw exception if version not found")
    void completeUpload_VersionNotFound() {
        // Arrange
        Long versionId = 2L;
        VersionDto dto = new VersionDto();

        given(versionRepo.findById(versionId)).willReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> versionService.completeUpload(versionId, dto));

        then(versionMapper).shouldHaveNoInteractions();
        then(versionRepo).shouldHaveNoMoreInteractions();
        then(azureStorageService).shouldHaveNoInteractions();
        then(metadataService).shouldHaveNoInteractions();
        then(outboxService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("completeUpload should not update if version is not UPLOADING")
    void completeUpload_VersionNotUploading() {
        // Arrange
        Long versionId = 3L;
        VersionDto dto = new VersionDto();

        Version existingVersion = new Version();
        existingVersion.setStatus(VersionStatus.AVAILABLE); // đã sẵn AVAILABLE

        given(versionRepo.findById(versionId)).willReturn(Optional.of(existingVersion));

        // Act
        versionService.completeUpload(versionId, dto);

        // Assert
        // Status vẫn giữ nguyên
        assertEquals(VersionStatus.AVAILABLE, existingVersion.getStatus());

        // Mapper và save vẫn được gọi, tùy logic bạn có thể bỏ qua
        then(versionMapper).should().updateVersionIgnoreVNumber(existingVersion, dto);
        then(versionRepo).should().save(existingVersion);

        then(azureStorageService).shouldHaveNoInteractions();
        then(metadataService).shouldHaveNoInteractions();
        then(outboxService).shouldHaveNoInteractions();
    }
    @Test
    @DisplayName("Should delete blobs and handle success/failure correctly")
    void deleteAllVersionByMetadata_HappyPath() {
        // Arrange
        List<Long> metadataIds = List.of(1L, 2L);

        Version v1 = new Version();
        v1.setMetadataId(1L);
        v1.setBlobName("blob1");

        Version v2 = new Version();
        v2.setMetadataId(2L);
        v2.setBlobName("blob2");

        List<Version> versions = List.of(v1, v2);

        // mock repo
        when(versionRepo.findAllByMetadataIds(metadataIds)).thenReturn(versions);

        // mock storage service result
        DeleteBlobsResult result = new DeleteBlobsResult(
                List.of(1L),  // success
                List.of(2L)   // failed
        );
        when(azureStorageService.deleteBlobs(anyMap())).thenReturn(result);

        // Act
        versionService.deleteAllVersionByMetadata(metadataIds);

        // Assert
        // 1️⃣ check gọi storage service với đúng map
        Map<Long, List<String>> expectedMap = Map.of(
                1L, List.of("blob1"),
                2L, List.of("blob2")
        );
        verify(azureStorageService).deleteBlobs(expectedMap);

        // 2️⃣ check gửi event failed
        verify(outboxService).addBlobDeleteFailEvent(List.of(2L));

        // 3️⃣ check gửi event success
        verify(outboxService).addBlobDeleteSuccessEvent(List.of(1L));

        // 4️⃣ check xóa version thành công
        verify(versionRepo).deleteAll(List.of(v1));

        // 5️⃣ đảm bảo không có gọi thừa
        verifyNoMoreInteractions(azureStorageService, outboxService, versionRepo);
    }
}
