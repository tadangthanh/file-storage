package vn.thanh.storageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DeleteBlobsResult {
    private List<Long> successMetadataIds;
    private List<Long> failedMetadataIds;
}
