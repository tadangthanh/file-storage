package vn.thanh.metadataservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetadataRequest {
    private String name;
    private Long size;
    private String type;
    private Long currentVersionId;
}
