package vn.thanh.metadataservice.service;

import vn.thanh.metadataservice.dto.MetadataCreateMessage;

import java.util.List;

public interface IOutboxService {
    void addDeleteMetadataEvent(List<Long> metadataIds);

    void addCreateMetadataEvent(MetadataCreateMessage message);
}
