package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.MetadataUpdate;

public interface IOutboxService {
    void saveMetadataEvent(MetadataUpdate update);
}
