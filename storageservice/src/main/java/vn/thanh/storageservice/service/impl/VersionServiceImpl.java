package vn.thanh.storageservice.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.client.MetadataService;
import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.dto.VersionInitRequest;
import vn.thanh.storageservice.entity.Version;
import vn.thanh.storageservice.entity.VersionStatus;
import vn.thanh.storageservice.exception.ResourceAlreadyExistsException;
import vn.thanh.storageservice.mapper.VersionMapper;
import vn.thanh.storageservice.repository.VersionRepo;
import vn.thanh.storageservice.service.IAzureStorageService;
import vn.thanh.storageservice.service.IVersionService;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j(topic = "VERSION_SERVICE")
public class VersionServiceImpl implements IVersionService {
    private final VersionRepo versionRepo;
    private final IAzureStorageService azureStorageService;
    private final MetadataService metadataService;
    private final VersionMapper versionMapper;

    @Override
    public String initVersion(VersionInitRequest versionInitRequest) {
        log.info("init version: metadata id: {}", versionInitRequest.getMetadataId());
        // kiem tra su ton tai cua metadata file ben metadata service
        metadataService.getFileById(versionInitRequest.getMetadataId());
        // kiem tra xem da ton tai phien ban cho metadata nay chua
        Version versionExists = versionRepo.findFirstByMetadataIdOrderByVersionNumberDesc(versionInitRequest.getMetadataId()).orElse(null);
        Version version = new Version();
        //neu ton tai thi tao version moi bang cach tang version
        if (versionExists!= null) {
            int currentVersion = versionExists.getVersionNumber();
            version.setVersionNumber(currentVersion + 1);
        } else {
            version.setVersionNumber(1);
        }
        version.setMetadataId(versionInitRequest.getMetadataId());
        version.setStatus(VersionStatus.UPLOADING);
        versionRepo.save(version);
        return azureStorageService.getUrlUpload(versionInitRequest.getMetadataId() + "/" + version.getId() + "/" + versionInitRequest.getOriginalFilename());
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
}
