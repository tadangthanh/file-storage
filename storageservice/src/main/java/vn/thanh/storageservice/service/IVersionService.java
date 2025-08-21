package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.DocumentReady;
import vn.thanh.storageservice.dto.UploadSignRequest;
import vn.thanh.storageservice.dto.UploadUrlResponse;
import vn.thanh.storageservice.entity.Version;

import java.io.InputStream;
import java.util.List;

public interface IVersionService {
    List<UploadUrlResponse> presignUpload(List<UploadSignRequest> uploadSignRequests);

    void completeUpload(DocumentReady documentReady);

    Version getVersionOrThrow(Long versionId);

    Version getVersionMaxByMetadataId(Long metadataId);

    InputStream downloadMaxVersionByMetadataId(Long metadataId);

    void deleteAllVersionByMetadata(List<Long> metadataIds);

}
