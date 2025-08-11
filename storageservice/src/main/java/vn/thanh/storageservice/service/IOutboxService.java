package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.MetadataUpdate;

import java.util.List;

public interface IOutboxService {
    void addUpdateMetadataEvent(MetadataUpdate update);

    void addMetadataCleanUpEvent(List<Long> metadataIds);
}
