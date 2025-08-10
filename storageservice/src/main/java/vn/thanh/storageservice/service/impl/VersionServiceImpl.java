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
        metadataService.getFileById(versionInitRequest.getMetadataId());
        Version version = new Version();
        version.setStatus(VersionStatus.UPLOADING);
        version.setMetadataId(versionInitRequest.getMetadataId());
        version.setVersionNumber(1);
        versionRepo.save(version);
        return azureStorageService.getUrlUpload(versionInitRequest.getMetadataId() + "/" + version.getId() + "/" + versionInitRequest.getOriginalFilename());
    }

    @Override
    public void completeUpload(Long versionId, VersionDto versionDto) {
        log.info("update version by id: {}", versionId);
        Version versionExists = versionRepo.findById(versionId).orElseThrow(() -> {
            log.info("version not found by id: {}", versionId);
            return new ResourceAlreadyExistsException("Không tìm thấy phiên bản này");
        });
        versionMapper.updateVersionIgnoreVNumber(versionExists, versionDto);
        versionExists.setStatus(VersionStatus.AVAILABLE);
        versionRepo.save(versionExists);
    }
}
