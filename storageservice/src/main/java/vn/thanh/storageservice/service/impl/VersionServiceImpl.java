package vn.thanh.storageservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.client.MetadataService;
import vn.thanh.storageservice.dto.DeleteBlobsResult;
import vn.thanh.storageservice.dto.UploadSignRequest;
import vn.thanh.storageservice.dto.UploadUrlResponse;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.entity.VersionStatus;
import vn.thanh.storageservice.exception.ResourceAlreadyExistsException;
import vn.thanh.storageservice.exception.ResourceNotFoundException;
import vn.thanh.storageservice.mapper.VersionMapper;
import vn.thanh.storageservice.repository.VersionRepo;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.service.IOutboxService;
import vn.thanh.storageservice.service.IVersionService;
import vn.thanh.storageservice.utils.AuthUtils;
import vn.thanh.storageservice.utils.BlobNameUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j(topic = "VERSION_SERVICE")
public class VersionServiceImpl implements IVersionService {
    private final VersionRepo versionRepo;
    private final IAzureStorageService azureStorageService;
    private final MetadataService metadataService;
    private final VersionMapper versionMapper;
    private final IOutboxService outboxService;

    @Override
    // hàm upload tài liệu mới (tài liệu chưa tồn tại version nào )
    @Transactional
    public List<UploadUrlResponse> presignUpload(List<UploadSignRequest> uploadSignRequests) {
        log.info("presignUpload (new documents - batch)");

//        // 1. Kiểm tra metadata tồn tại cho tất cả
        List<Long> metadataIds = uploadSignRequests.stream().map(UploadSignRequest::getMetadataId).collect(Collectors.toList());
        metadataService.isOwnerAll(AuthUtils.getUserId(), metadataIds);

        // 2. Chuẩn bị list Version để insert
        List<Version> versionsToInsert = uploadSignRequests.stream().map(req -> {
            Version v = new Version();
            v.setMetadataId(req.getMetadataId());
            v.setVersionNumber(1);
            v.setStatus(VersionStatus.UPLOADING);
            return v;
        }).collect(Collectors.toList());

        // 3. Insert tất cả version 1 lần
        List<Version> savedVersions = versionRepo.saveAll(versionsToInsert);

        // 4. Map lại để generate blobName + SAS URL
        List<UploadUrlResponse> responses = new ArrayList<>();
        for (int i = 0; i < savedVersions.size(); i++) {
            Version version = savedVersions.get(i);
            UploadSignRequest request = uploadSignRequests.get(i);

            String blobName = BlobNameUtil.generateBlobName(request.getMetadataId(), version.getId(), request.getOriginalFilename());
            version.setBlobName(blobName);

            String uploadUrl = azureStorageService.getUrlUpload(blobName);

            responses.add(new UploadUrlResponse(request.getOriginalFilename(), blobName, uploadUrl));
        }

        // 5. Cập nhật blobName cho tất cả version 1 lần (batch update)
        versionRepo.saveAll(savedVersions);

        return responses;
    }


    @Override
    public void completeUpload(Long versionId, VersionDto versionDto) {
        log.info("update version by id: {}", versionId);
        Version versionExists = getVersionOrThrow(versionId);
        versionMapper.updateVersionIgnoreVNumber(versionExists, versionDto);
        versionExists.setStatus(VersionStatus.AVAILABLE);
        versionRepo.save(versionExists);
    }


    @Override
    public Version getVersionOrThrow(Long versionId) {
        return versionRepo.findById(versionId).orElseThrow(() -> {
            log.info("version not found by id: {}", versionId);
            return new ResourceAlreadyExistsException("Không tìm thấy phiên bản này");
        });
    }

    @Override
    public Version getVersionMaxByMetadataId(Long metadataId) {
        return versionRepo.findFirstByMetadataIdOrderByVersionNumberDesc(metadataId).orElseThrow(() -> {
            log.info("version not found by metadata id: {}", metadataId);
            return new ResourceNotFoundException("Tài liệu không tồn tại");
        });
    }

    @Override
    public InputStream downloadMaxVersionByMetadataId(Long metadataId) {
        log.info("download max version by metadata id: {}", metadataId);
        Version version = getVersionMaxByMetadataId(metadataId);
        return azureStorageService.downloadBlobInputStream(version.getBlobName());
    }

    /***
     *
     * @param metadataIds: danh sách metadata id để xóa blob trên azure
     */
    @KafkaListener(topics = "${app.kafka.metadata-delete-topic}", groupId = "${app.kafka.storage-group}")
    @Override
    public void deleteAllVersionByMetadata(List<Long> metadataIds) {
        log.info("received: delete all version by metadata id: {}", metadataIds.toString());
        List<Version> versions = versionRepo.findAllByMetadataIds(metadataIds);
        // tạo 1 map với key là metadata id và value là blob name
        Map<Long, List<String>> metadataToBlobs = versions.stream()
                .collect(Collectors.groupingBy(
                        Version::getMetadataId,
                        Collectors.mapping(Version::getBlobName, Collectors.toList())
                ));
        // đối tượng trả về chứa danh sách các id metadata đã xóa blob thành công và thất bại
        DeleteBlobsResult result = azureStorageService.deleteBlobs(metadataToBlobs);
        log.info("Deleted {} blobs successfully, {} blobs failed",
                result.getSuccessMetadataIds().size(),
                result.getFailedMetadataIds().size());
        if (!result.getFailedMetadataIds().isEmpty()) {
            // nếu 1 metadata xóa file thất bại thì sẽ gửi lại metadata đó về metadata service để đánh dấu status là DELETE_FAILD
            outboxService.addBlobDeleteFailEvent(result.getFailedMetadataIds());
        }
        if (!result.getSuccessMetadataIds().isEmpty()) {
            // nếu 1 metadata đã xóa thành công thì sẽ gửi lại id về cho metadata service để xóa vĩnh viễn luôn
            outboxService.addBlobDeleteSuccessEvent(result.getSuccessMetadataIds());
        }
        List<Version> successVersions = versions.stream()
                .filter(v -> result.getSuccessMetadataIds().contains(v.getMetadataId()))
                .toList();

        versionRepo.deleteAll(successVersions);
    }
}
