package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.dto.VersionInitRequest;
import vn.thanh.storageservice.entity.Version;

public interface IVersionService {
    String initVersion(VersionInitRequest versionInitRequest);

    void completeUpload(Long versionId, VersionDto versionDto);

    Version getVersionOrThrow(Long versionId);
}
