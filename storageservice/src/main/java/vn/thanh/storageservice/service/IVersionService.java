package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.VersionDto;
import vn.thanh.storageservice.dto.VersionInitRequest;

public interface IVersionService {
    String initVersion(VersionInitRequest versionInitRequest);

    void completeUpload(Long versionId, VersionDto versionDto);
}
