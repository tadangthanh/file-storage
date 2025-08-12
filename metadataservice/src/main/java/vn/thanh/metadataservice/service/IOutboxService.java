package vn.thanh.metadataservice.service;

import java.util.List;

public interface IOutboxService {
    void addDeleteMetadataEvent(List<Long> metadataIds);
}
