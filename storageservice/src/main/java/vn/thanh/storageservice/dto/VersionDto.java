package vn.thanh.storageservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionDto extends BaseDto {
    private Long metadataId;
    private Integer versionNumber;
    private String blobName;
    private Long size;
}
