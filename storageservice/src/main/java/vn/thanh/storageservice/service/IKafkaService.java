package vn.thanh.storageservice.service;

import vn.thanh.storageservice.dto.MetadataUpdate;

public interface IKafkaService {
    void eventUpdateMetadata(MetadataUpdate metadataUpdate);
}
