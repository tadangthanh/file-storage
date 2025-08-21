package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.DocumentReady;

import java.util.List;

public interface IOutboxService {
    void addUploadCompletedEvent(DocumentReady update);

    void addMetadataCleanUpEvent(List<Long> metadataIds);

    void addBlobDeleteFailEvent(List<Long> metadataIds);

    void addBlobDeleteSuccessEvent(List<Long> metadataIds);
}
