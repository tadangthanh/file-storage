package vn.thanh.storageservice.dto;

import lombok.Getter;
import lombok.Setter;
import vn.thanh.storageservice.entity.VersionStatus;

@Getter
@Setter
public class VersionDto extends BaseDto {
    private Long metadataId;
    private Integer versionNumber;
    private String blobName;
    private VersionStatus status;
    private Long size;
}
